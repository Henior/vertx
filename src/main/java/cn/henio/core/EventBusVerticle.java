package cn.henio.core;

import cn.henio.bootstrap.AutoRegisterVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/7 14:39].
 */
@Component
public class EventBusVerticle extends AutoRegisterVerticle{

  @Autowired
  private Router router;

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    router.route("/publish").blockingHandler(context ->{
      vertx.eventBus().send("bus.key", context.request().getParam("key"),res -> {
        if(res.succeeded()){
          System.out.println("reply:" + res.result().body().toString());
        }
      });
      context.response().end("OK");
    });
  }

}
