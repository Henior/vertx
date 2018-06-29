package cn.henio.bootstrap;

import cn.henio.AutoRegisterVerticle;
import cn.henio.http.service.DbServiceVerticle;
import cn.henio.http.service.DbServiceVerticle.SqlQuery;
import cn.henio.mqtt.MqttFunction;
import cn.henio.tcp.MessageDispatcher;
import cn.henio.tcp.SessionHolder;
import cn.henio.tcp.SimpleProtocol;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SelfSignedCertificate;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/29 15:01].
 */
@Component
public class BootstrapVerticle extends AutoRegisterVerticle {

  @Autowired
  private Vertx vertx;
  @Autowired
  private JDBCClient dbClient;
  @Autowired
  private Router router;
  @Autowired
  private CookieHandler CookieHandler;
  @Autowired
  private SessionHandler sessionHandler;
  @Autowired
  private UserSessionHandler userSessionHandler;
  @Autowired
  private BodyHandler bodyHandler;
  @Autowired
  private StaticHandler staticHandler;
  @Autowired
  private MessageDispatcher messageDispatcher;
  @Autowired
  private SessionHolder tcpHolder;
  @Autowired
  @Qualifier("mqttSessionHolder")
  private cn.henio.mqtt.SessionHolder mqttHolder;
  @Autowired
  private MqttFunction mqttFunction;

  private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapVerticle.class);

  /*@Override
  protected DeploymentOptions deploymentOptions() {
    return super.deploymentOptions().setInstances(4);
  }*/

  private AtomicBoolean mak = new AtomicBoolean(true);

  @Override
  public void start() throws Exception {
    CompositeFuture.all(startHttpServer(), startNetServer(), startMqttServer(), initDb()).setHandler(res -> {
      if(res.succeeded()){
        LOGGER.info("ALL SERVER STARTED!");
      }else {
        LOGGER.error("SERVER STARTED ERROR!", res.cause());
      }
    });
  }

  /**
   * 启动http服务器
   * @return
   */
  private Future<HttpServer> startHttpServer(){
    SelfSignedCertificate certificate = SelfSignedCertificate.create();
    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setLogActivity(true).setIdleTimeout(5).setSsl(true).setKeyCertOptions(certificate.keyCertOptions())
        .setTrustOptions(certificate.trustOptions());
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

    // 支持cookie、session、授权、文件上传
    router.route().order(-4).handler(CookieHandler);
    router.route().order(-3).handler(sessionHandler);
    router.route().order(-2).handler(userSessionHandler);
    router.route().order(-1).handler(bodyHandler);
    // 静态资源
    router.route("/static/*").handler(rc -> {
      // 多实例时，处理线程名称
      //System.err.println(Thread.currentThread().getName());
      staticHandler.handle(rc);
    });
    Future<HttpServer> http = Future.future();
    httpServer.requestHandler(router::accept).listen(8888, http);
    return http;
  }

  /**
   * 启动tcp服务器
   * @return
   */
  private Future<NetServer> startNetServer(){
    SelfSignedCertificate certificate = SelfSignedCertificate.create();
    NetServer server = vertx.createNetServer(new NetServerOptions().setIdleTimeout(5000).setAcceptBacklog(128).setTcpKeepAlive(true).setLogActivity(true).setSsl(true).setKeyCertOptions(certificate.keyCertOptions())
        .setTrustOptions(certificate.trustOptions()));
    Future<NetServer> net = Future.future();
    server.connectHandler(socket -> {
      socket.handler(buffer -> {
        if(null != buffer && buffer.length() > 0){
          tcpHolder.createSession(Json.decodeValue(buffer, SimpleProtocol.class).getIdentifier(), socket);
          socket.handler(messageDispatcher::dispatch);
        }
      }).closeHandler(v->{
        LOGGER.info("TCP CONNECTION CLOSED NORMALLY!");
        tcpHolder.removeSession(socket);
      }).exceptionHandler(e -> {
        LOGGER.error("TCP CONNECTION ERROR!", e);
        tcpHolder.closeSession(socket);
      });
    }).listen(14321, "localhost", net);
    return net;
  }

  /**
   * 启动mqtt服务器
   * @return
   */
  private Future<MqttServer> startMqttServer(){
    MqttServer mqttServer = MqttServer.create(vertx);
    Future<MqttServer> mqtt = Future.future();
    mqttServer.endpointHandler(endpoint -> {
      // 创建连接
      mqttHolder.createSession("mouzhanpeng","12345", endpoint);
      String id = endpoint.clientIdentifier();
      // 自动响应ACK
      mqttFunction.autoAck(endpoint);
      // 设置各种处理器
      endpoint.disconnectHandler(v -> mqttFunction.disconnectHandler(id));
      endpoint.subscribeHandler(msg -> mqttFunction.subscribeHandler(msg, id));
      endpoint.unsubscribeHandler(msg -> mqttFunction.unsubscribeHandler(msg, id));
      endpoint.publishHandler(msg -> mqttFunction.publishHandler(msg, id));
      endpoint.pingHandler(v -> mqttFunction.pingHandler(id));

      endpoint.closeHandler(v -> {
        LOGGER.warn("MQTT CONNECTION CLOSED NORMALLY!");
        mqttHolder.removeSession(id);
      }).exceptionHandler(e -> {
        LOGGER.error("MQTT CONNECTION ERROR!", e);
        mqttHolder.closeSession(id);
      });
    }).listen(mqtt);
    return mqtt;
  }

  /**
   * 初始化数据库
   * @return
   */
  private Future<Void> initDb(){
    Future<Void> db = Future.future();
    dbClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause());
        db.fail(ar.cause());
      } else {
        SQLConnection connection = ar.result();
        connection.execute(DbServiceVerticle.sqlQueries.get(SqlQuery.CREATE_PAGES_TABLE), create -> {   // <2>
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            db.fail(create.cause());
          } else {
            db.complete();
          }
        });
      }
    });
    return db;
  }
}
