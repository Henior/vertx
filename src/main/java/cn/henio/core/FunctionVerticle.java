package cn.henio.core;

import cn.henio.bootstrap.AutoRegisterVerticle;
import io.vertx.core.Vertx;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/4 17:05].
 */
@Component
public class FunctionVerticle extends AutoRegisterVerticle{

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    // 延迟执行
    //vertx.setTimer(5000, System.out::println);
    // 周期执行
    //vertx.setPeriodic(60000, System.out::println);
  }
}
