package cn.henio.web;

import cn.henio.bootstrap.AutoRegisterVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/18 14:25].
 */
@Component
public class WebVerticle extends AutoRegisterVerticle {

  @Autowired
  private Router router;
  @Autowired
  private TemplateHandler templateHandler;

  private String temp = "default!";

  @Override
  public void start() throws Exception {
    Vertx vertx = getVertx();
    router.route("/xx1").blockingHandler(context ->{
      System.out.println("sessionId:" + context.session().id());
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      context.response().end(Thread.currentThread().getName() + context.toString());
    });

    MessageConsumer<Object> consumer = vertx.eventBus().consumer("bus.key", msg -> {
      temp = msg.body().toString();
      msg.reply(temp);
    });
    router.route("/consume").blockingHandler(context ->{
      context.response().end(temp);
    });
    // 文件上传
    router.route("/upload").handler(routingContext -> {
      System.err.println("upload-");
      Set<FileUpload> uploads = routingContext.fileUploads();
      // 执行上传处理
      System.err.println(uploads);
      for(FileUpload fu : uploads){
        System.err.println(fu.uploadedFileName());
      }
      routingContext.response().end("OK!");
    });

    // 页面
    router.route("/view/*").produces("text/html").last().handler(rc ->{
      rc.put("hello", "world");
      templateHandler.handle(rc);
    });
  }

}
