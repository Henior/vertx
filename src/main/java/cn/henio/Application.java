package cn.henio;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/30 16:13].
 */

@ComponentScan
public class Application {

  /**
   * spring上下文
   */
  private static ApplicationContext applicationContext;

  /**
   * 获取spring上下文
   * @return
   */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }


  public static void main(String[] args) {
    applicationContext = new AnnotationConfigApplicationContext(Application.class);
  }
}
