package cn.henio;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/6/4 15:32].
 */
@Configuration
public class Configurations {

  private final Vertx vertx = Vertx.vertx(new VertxOptions().setFileResolverCachingEnabled(false));

  @Autowired
  private SessionStore sessionStore;
  @Autowired
  private JDBCClient jdbcClient;
  @Autowired
  private JDBCAuth jdbcAuth;

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
  public SessionStore sessionStore(){
    return LocalSessionStore.create(vertx);
  }

  @Bean
  public SessionHandler sessionHandler(){
    return SessionHandler.create(sessionStore).setCookieHttpOnlyFlag(true);
  }

  @Bean
  public UserSessionHandler userSessionHandler(){
    return UserSessionHandler.create(jdbcAuth);
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

  @Bean
  public JDBCClient jdbcClient(){
    return JDBCClient.createShared(vertx, new JsonObject()
        .put("url", "jdbc:hsqldb:file:db/wiki")
        .put("driver_class", "org.hsqldb.jdbcDriver")
        .put("max_pool_size", 30));
  }

  @Bean
  public RedisClient redisClient(){
    return RedisClient.create(vertx, new RedisOptions()
        .setHost("192.168.20.30").setSelect(8));
  }

  @Bean
  public JDBCAuth jdbcAuth(){
    return JDBCAuth.create(vertx, jdbcClient);
  }
}
