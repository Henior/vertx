package cn.henio.core;

import cn.henio.AutoRegisterVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.ext.web.Router;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/7 14:39].
 */
//@Component
public class EventBusVerticle extends AutoRegisterVerticle{

  @Autowired
  private Router router;

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    router.route("/publish").blockingHandler(context ->{
      vertx.eventBus().send("bus.key", context.request().getParam("key"), new DeliveryOptions().addHeader("childKey","xxx"),res -> {
        if(res.succeeded()){
          System.out.println("reply:" + res.result().body().toString());
        }
      });
      context.response().end("OK");
    });
  }

}
