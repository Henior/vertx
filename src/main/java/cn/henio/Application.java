package cn.henio;

import com.hazelcast.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
  /**
   * Spring上下文
   */
  private static ApplicationContext applicationContext;

  /**
   * Vertx上下文
   */
  private static Vertx vertx;

  /**
   * 获取Spring上下文
   * @return
   */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * 获取Vertx上下文
   * @return
   */
  public static Vertx getVertx() {
    return vertx;
  }

  public static void main(String[] args) {
    ClusterManager mgr = new HazelcastClusterManager(new Config());//创建ClusterManger对象
    VertxOptions options = new VertxOptions().setClusterManager(mgr).setFileResolverCachingEnabled(false);//设置到Vertx启动参数中
    Vertx.clusteredVertx(options, res -> {
      if (res.succeeded()) {
        Application.vertx = res.result();
        Application.applicationContext = new AnnotationConfigApplicationContext(Application.class);
        LOGGER.info("SPRING & VERTX INITIALISED SUCCESSFULLY!");
      } else {
        LOGGER.error("SPRING & VERTX INITIALISED FAILURE!", res.cause());
      }
    });
  }
}
