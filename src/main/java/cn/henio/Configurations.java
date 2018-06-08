package cn.henio;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/4 15:32].
 */
@Configuration
public class Configurations {

  private final Vertx vertx = Vertx.vertx(new VertxOptions().setFileResolverCachingEnabled(false));

  private final SessionStore sessionStore = LocalSessionStore.create(vertx);
  /**
   * 全局唯一Vertx
   * @return
   */
  @Bean
  public Vertx vertx(){
    return vertx;
  }

  /**
   * 全局唯一Router
   * @return
   */
  @Bean
  public Router router(){
    return Router.router(vertx);
  }

  @Bean
  public CookieHandler cookieHandler(){
    return CookieHandler.create();
  }

  @Bean
  public SessionHandler sessionHandler(){
    return SessionHandler.create(sessionStore).setCookieHttpOnlyFlag(true);
  }

  @Bean
  public TemplateHandler templateHandler(){
    return TemplateHandler.create(FreeMarkerTemplateEngine.create().setExtension("html"), "view", TemplateHandler.DEFAULT_CONTENT_TYPE);
  }

  @Bean
  public StaticHandler staticHandler(){
    return StaticHandler.create("static");
  }

  @Bean
  public BodyHandler bodyHandler(){
    return BodyHandler.create();
  }
}
