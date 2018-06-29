package cn.henio.core;

import cn.henio.AutoRegisterVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.SelfSignedCertificate;
import java.util.Date;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/4 17:05].
 */
//@Component
public class FunctionVerticle extends AutoRegisterVerticle{

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    SelfSignedCertificate certificate = SelfSignedCertificate.create();
    NetServer server = vertx.createNetServer(new NetServerOptions().setIdleTimeout(5000).setAcceptBacklog(128).setTcpKeepAlive(true).setLogActivity(true).setSsl(true).setKeyCertOptions(certificate.keyCertOptions())
        .setTrustOptions(certificate.trustOptions()));
    Future<NetServer> net = Future.future();
    server.connectHandler(socket -> {
      socket.handler(buffer -> {
       /* if(mak.get()){
          JsonObject entries = buffer.toJsonObject();
          String id = entries.getJsonObject("header").getString("id");
          connections.putIfAbsent(id, socket);
          System.out.println(entries);
          mak.set(false);
        }*/

        System.out.println("I received some bytes: " + buffer.length());
        socket.write("[" + new Date() + "]--hi!");
        socket.end(Buffer.buffer(""));
        System.err.println(buffer.toString());
        System.out.println("completed");
      }).closeHandler(v->{
        System.out.println("[" + System.currentTimeMillis() + "]server closed!");
      }).exceptionHandler(e -> {
        e.printStackTrace();
      });
      socket.end(Buffer.buffer("helll"));
    }).listen(14321, "localhost");
  }
}
