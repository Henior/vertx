package cn.henio.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.NetServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/29 15:01].
 */
@Component
public class BootstrapVerticle extends AutoRegisterVerticle{

  @Autowired
  private Router router;
  @Autowired
  private CookieHandler CookieHandler;
  @Autowired
  private SessionHandler sessionHandler;
  @Autowired
  private BodyHandler bodyHandler;
  @Autowired
  private StaticHandler staticHandler;

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    HttpServerOptions httpServerOptions = new HttpServerOptions();
    httpServerOptions.setLogActivity(true).setIdleTimeout(5);
    HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

    // 支持cookie、session、文件上传
    router.route().order(-1).handler(CookieHandler).handler(sessionHandler).handler(bodyHandler);
    // 静态资源
    router.route("/static/*").handler(staticHandler);



    Future<HttpServer> http = Future.future();
    httpServer.requestHandler(router::accept).listen(8888, http.completer());

    //#############################################TCP SERVER $$$$$$$$$$$$$$
    NetServer server = vertx.createNetServer();
    Future<NetServer> net = Future.future();
    server.connectHandler(socket -> {
      socket.handler(buffer -> {
        System.out.println("I received some bytes: " + buffer.length());
      });
    }).listen(14321, "localhost", net.completer());
    CompositeFuture.all(http, net).setHandler(result -> {
      if (result.succeeded()) {
        System.out.println("server is started!");
      } else {
        System.out.println("server is not started!");
      }
    });
  }
}
