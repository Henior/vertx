package cn.henio.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author: mouzhanpeng.
 * @date: created in [2018/5/30 15:24].
 */
public abstract class AutoRegisterVerticle extends AbstractVerticle {

  @Autowired
  private Vertx vertx;

  @PostConstruct
  private void prepare() throws Exception {
    vertx.deployVerticle(this, deploymentOptions().setInstances(1), completionHandler());
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
        System.out.println("[" + this.getClass().getTypeName() + "]-Deployment id is: " + result.result());
      } else {
        System.out.println("[" + this.getClass().getTypeName() + "]-Deployment failed!");
      }
    };
  }

  @Override
  public void stop() throws Exception {
    System.err.println(this.deploymentID() + "was undeployed!");
  }
}
