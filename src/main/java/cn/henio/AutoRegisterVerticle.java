package cn.henio;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author: mouzhanpeng.
 * @date: created in [2018/5/30 15:24].
 */
public abstract class AutoRegisterVerticle extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(AutoRegisterVerticle.class);

  @Autowired
  private Vertx vertx;

  @PostConstruct
  private void prepare() throws Exception {
    for (int i = 1; i <= deploymentOptions().getInstances(); i++) {
      vertx.deployVerticle(this, deploymentOptions().setInstances(1), completionHandler());
    }
  }

  /**
   *  配置项
   */
  protected DeploymentOptions deploymentOptions(){
    return new DeploymentOptions().setHa(true);
  }

  /**
   * 部署实例后，回调
   * @return
   */
  protected Handler<AsyncResult<String>> completionHandler(){
    return result -> {
      if (result.succeeded()) {
        logger.info("[" + this.getClass().getTypeName() + "]-Deployment id is: " + result.result());
      } else {
        result.cause().printStackTrace();
        logger.warn("[" + this.getClass().getTypeName() + "]-Deployment failed!");
      }
    };
  }

  @Override
  public void stop() throws Exception {
    System.err.println(this.deploymentID() + "was undeployed!");
  }
}
