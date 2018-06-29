package cn.henio.http;

import cn.henio.AutoRegisterVerticle;
import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: mouzhanpeng.
 * @date: created in [2018/5/18 14:25].
 * @description @see https://github.com/vert-x3/vertx-guide-for-java-devs  [官方web例子，内引用的jquery库被墙了，手动替换一下]
 */
@Component
public class RouteVerticle extends AutoRegisterVerticle {

  private final Logger LOGGER = LoggerFactory.getLogger(RouteVerticle.class);

  @Autowired
  private Vertx vertx;
  @Autowired
  private Router router;
  @Autowired
  private TemplateHandler templateHandler;
  @Autowired
  private JDBCClient dbClient;
  @Autowired
  private JDBCAuth jdbcAuth;

  private String temp = "default!";

  @Override
  public void start() throws Exception {
    //=======================================自测例子=====================================
    router.route("/xx1").blockingHandler(context -> {
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
    router.route("/consume").blockingHandler(context -> {
      context.response().end(temp);
    });
    // 文件上传
    router.route("/upload").handler(routingContext -> {
      System.err.println("upload-");
      Set<FileUpload> uploads = routingContext.fileUploads();
      // 执行上传处理
      System.err.println(uploads);
      for (FileUpload fu : uploads) {
        System.err.println(fu.uploadedFileName());
      }
      routingContext.response().end("OK!");
    });

    // 页面
    router.route("/view/*").produces("text/html").last().handler(rc -> {
      rc.put("hello", "world");
      templateHandler.handle(rc);
    });
    //=======================================自测例子=====================================
    //=======================================官方例子=====================================
    /*
    与jdbcProvider基本等价
    JsonObject config = new JsonObject().put("properties_path", "classpath:test-auth.properties");
    AuthProvider provider = ShiroAuth.create(vertx, ShiroAuthRealmType.PROPERTIES, config);*/



    AuthHandler authHandler = RedirectAuthHandler.create(jdbcAuth, "/login"); // <2>
    router.route("/").handler(authHandler);  // <3>
    router.route("/wiki/*").handler(authHandler);
    router.route("/action/*").handler(authHandler);

    router.get("/").handler(this::indexHandler);
    router.get("/wiki/:page").handler(this::pageRenderingHandler);
    router.post("/action/save").handler(this::pageUpdateHandler);
    router.post("/action/create").handler(this::pageCreateHandler);
    router.post("/action/delete").handler(this::pageDeletionHandler);

    // tag::auth-login[]
    router.get("/login").handler(this::loginHandler);
    router.post("/login-auth").handler(FormLoginHandler.create(jdbcAuth));  // <1>

    router.get("/logout").handler(context -> {
      context.clearUser();  // <2>
      context.response().setStatusCode(302).putHeader("Location", "/").end();
    });
    // end::auth-login[]

    // tag::jwtAuth[]
    Router apiRouter = Router.router(vertx);

    // =========鉴权通过pricipal()============
    JWTAuth jwtAuth = JWTAuth.create(vertx,
        new JsonObject().put("keyStore", new JsonObject().put("path", "keystore.jceks").put("type", "jceks").put("password", "secret")));

    apiRouter.route().handler(JWTAuthHandler.create(jwtAuth, "/api/token"));
    // end::jwtAuth[]

    // tag::issue-jwt[]
    apiRouter.get("/token").handler(context -> {

      JsonObject creds = new JsonObject().put("username", context.request().getHeader("login"))
          .put("password", context.request().getHeader("password"));
      jdbcAuth.authenticate(creds, authResult -> {  // <1>

        if (authResult.succeeded()) {
          User user = authResult.result();
          user.isAuthorised("create", canCreate -> {  // <2>
            user.isAuthorised("delete", canDelete -> {
              user.isAuthorised("update", canUpdate -> {
                String token = jwtAuth.generateToken( // <3>
                    new JsonObject().put("username", context.request().getHeader("login"))
                        .put("canCreate", canCreate.succeeded() && canCreate.result()).put("canDelete", canDelete.succeeded() && canDelete.result())
                        .put("canUpdate", canUpdate.succeeded() && canUpdate.result()), new JWTOptions().setSubject("Wiki API").setIssuer("Vert.x"));
                context.response().putHeader("Content-Type", "text/plain").end(token);
              });
            });
          });
        } else {
          context.fail(401);
        }
      });
    });
    // end::issue-jwt[]

    apiRouter.get("/pages").handler(this::apiRoot);
    apiRouter.get("/pages/:id").handler(this::apiGetPage);
    apiRouter.post().handler(BodyHandler.create());
    apiRouter.post("/pages").handler(this::apiCreatePage);
    apiRouter.put().handler(BodyHandler.create());
    apiRouter.put("/pages/:id").handler(this::apiUpdatePage);
    apiRouter.delete("/pages/:id").handler(this::apiDeletePage);
    router.mountSubRouter("/api", apiRouter);


  }

  private String wikiDbQueue = "wikidb.queue";

  private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();

  // tag::loginHandler[]
  private void loginHandler(RoutingContext context) {
    context.put("title", "Login");
    templateEngine.render(context, "templates", "/login.ftl", ar -> {
      if (ar.succeeded()) {
        context.response().putHeader("Content-Type", "text/html");
        context.response().end(ar.result());
      } else {
        context.fail(ar.cause());
      }
    });
  }

  // tag::indexHandler[]
  private void indexHandler(RoutingContext context) {
    context.user().isAuthorised("create", res -> {  // <1>
      boolean canCreatePage = res.succeeded() && res.result();  // <2>
      DeliveryOptions options = new DeliveryOptions().addHeader("action", "all-pages"); // <2>
      vertx.eventBus().send(wikiDbQueue, null, options, reply -> {  // <1>
        if (reply.succeeded()) {
          JsonObject body = (JsonObject) reply.result().body();   // <3>
          context.put("title", "Wiki home");
          context.put("pages", body.getJsonArray("pages").getList());
          context.put("canCreatePage", canCreatePage);  // <3>
          context.put("username", context.user().principal().getString("username"));  // <4>
          templateEngine.render(context, "templates", "/index.ftl", ar -> {
            if (ar.succeeded()) {
              context.response().putHeader("Content-Type", "text/html");
              context.response().end(ar.result());
            } else {
              context.fail(ar.cause());
            }
          });
        } else {
          context.fail(reply.cause());
        }
      });
    });
  }
  // end::indexHandler[]

  // tag::rest[]
  private static final String EMPTY_PAGE_MARKDOWN = "# A new page\n" + "\n" + "Feel-free to write in Markdown!\n";

  private void pageRenderingHandler(RoutingContext context) {
    context.user().isAuthorised("update", updateResponse -> {
      boolean canSavePage = updateResponse.succeeded() && updateResponse.result();
      context.user().isAuthorised("delete", deleteResponse -> {
        boolean canDeletePage = deleteResponse.succeeded() && deleteResponse.result();
        String requestedPage = context.request().getParam("page");
        JsonObject request = new JsonObject().put("page", requestedPage);

        DeliveryOptions options = new DeliveryOptions().addHeader("action", "get-page");
        vertx.eventBus().send(wikiDbQueue, request, options, reply -> {

          if (reply.succeeded()) {
            JsonObject body = (JsonObject) reply.result().body();

            boolean found = body.getBoolean("found");
            String rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN);
            context.put("title", requestedPage);
            context.put("id", body.getInteger("id", -1));
            context.put("newPage", found ? "no" : "yes");
            context.put("rawContent", rawContent);
            context.put("content", Processor.process(rawContent));
            context.put("timestamp", new Date().toString());
            context.put("username", context.user().principal().getString("username"));
            context.put("canSavePage", canSavePage);
            context.put("canDeletePage", canDeletePage);
            templateEngine.render(context, "templates", "/page.ftl", ar -> {
              if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html");
                context.response().end(ar.result());
              } else {
                context.fail(ar.cause());
              }
            });

          } else {
            context.fail(reply.cause());
          }
        });

      });
    });
  }

  private void pageUpdateHandler(RoutingContext context) {
    boolean pageCreation = "yes".equals(context.request().getParam("newPage"));
    context.user().isAuthorised(pageCreation ? "create" : "update", res -> {
      if (res.succeeded() && res.result()) {
        String title = context.request().getParam("title");
        JsonObject request = new JsonObject().put("id", context.request().getParam("id")).put("title", title)
            .put("markdown", context.request().getParam("markdown"));

        DeliveryOptions options = new DeliveryOptions();
        if ("yes".equals(context.request().getParam("newPage"))) {
          options.addHeader("action", "create-page");
        } else {
          options.addHeader("action", "save-page");
        }

        vertx.eventBus().send(wikiDbQueue, request, options, reply -> {
          if (reply.succeeded()) {
            context.response().setStatusCode(303);
            context.response().putHeader("Location", "/wiki/" + title);
            context.response().end();
          } else {
            context.fail(reply.cause());
          }
        });
      } else {
        context.response().setStatusCode(403).end();
      }
    });
  }

  private void pageCreateHandler(RoutingContext context) {
    String pageName = context.request().getParam("name");
    String location = "/wiki/" + pageName;
    if (pageName == null || pageName.isEmpty()) {
      location = "/";
    }
    context.response().setStatusCode(303);
    context.response().putHeader("Location", location);
    context.response().end();
  }

  private void pageDeletionHandler(RoutingContext context) {
    context.user().isAuthorised("delete", res -> {
      if (res.succeeded() && res.result()) {
        String id = context.request().getParam("id");
        JsonObject request = new JsonObject().put("id", id);
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "delete-page");
        vertx.eventBus().send(wikiDbQueue, request, options, reply -> {
          if (reply.succeeded()) {
            context.response().setStatusCode(303);
            context.response().putHeader("Location", "/");
            context.response().end();
          } else {
            context.fail(reply.cause());
          }
        });
      } else {
        context.response().setStatusCode(403).end();
      }
    });
  }
  // end::rest[]




  // tag::apiDeletePage[]
  private void apiDeletePage(RoutingContext context) {
    if (context.user().principal().getBoolean("canDelete", false)) {
      int id = Integer.valueOf(context.request().getParam("id"));
      JsonObject request = new JsonObject().put("id", id);
      DeliveryOptions options = new DeliveryOptions().addHeader("action", "delete-page");
      vertx.eventBus().send(wikiDbQueue, request, options, reply -> {
        handleSimpleDbReply(context, reply);
      });
    } else {
      context.fail(401);
    }
  }
  // end::apiDeletePage[]

  private void handleSimpleDbReply(RoutingContext context, AsyncResult<Message<Object>> reply) {
    if (reply.succeeded()) {
      context.response().setStatusCode(200);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject().put("success", true).encode());
    } else {
      context.response().setStatusCode(500);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject().put("success", false).put("error", reply.cause().getMessage()).encode());
    }
  }

  private void apiUpdatePage(RoutingContext context) {
    if (context.user().principal().getBoolean("canUpdate", false)) {
      JsonObject page = context.getBodyAsJson();
      if (!validateJsonPageDocument(context, page, "id", "markdown")) {
        return;
      }
      DeliveryOptions options = new DeliveryOptions();
      options.addHeader("action", "save-page");

      vertx.eventBus().send(wikiDbQueue, page, options, reply -> {
        handleSimpleDbReply(context, reply);
      });
    } else {
      context.fail(401);
    }
  }

  private boolean validateJsonPageDocument(RoutingContext context, JsonObject page, String... expectedKeys) {
    if (!Arrays.stream(expectedKeys).allMatch(page::containsKey)) {
      LOGGER.error("Bad page creation JSON payload: " + page.encodePrettily() + " from " + context.request().remoteAddress());
      context.response().setStatusCode(400);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject().put("success", false).put("error", "Bad request payload").encode());
      return false;
    }
    return true;
  }

  private void apiCreatePage(RoutingContext context) {
    if (context.user().principal().getBoolean("canCreate", false)) {
      JsonObject page = context.getBodyAsJson();
      if (!validateJsonPageDocument(context, page, "title", "markdown")) {
        return;
      }
      DeliveryOptions options = new DeliveryOptions();
      options.addHeader("action", "create-page");

      vertx.eventBus().send(wikiDbQueue, page, options, reply -> {
        if (reply.succeeded()) {
          context.response().setStatusCode(201);
          context.response().putHeader("Content-Type", "application/json");
          context.response().end(new JsonObject().put("success", true).encode());
        } else {
          context.response().setStatusCode(500);
          context.response().putHeader("Content-Type", "application/json");
          context.response().end(new JsonObject().put("success", false).put("error", reply.cause().getMessage()).encode());
        }
      });
    } else {
      context.fail(401);
    }
  }

  private void apiGetPage(RoutingContext context) {
    int id = Integer.valueOf(context.request().getParam("id"));
    JsonObject request = new JsonObject().put("id", id);
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "get-page-by-id");
    vertx.eventBus().send(wikiDbQueue, request, options, reply -> {
      JsonObject response = new JsonObject();
      if (reply.succeeded()) {
        JsonObject dbObject = (JsonObject)reply.result().body();
        if (dbObject.getBoolean("found")) {
          JsonObject payload = new JsonObject().put("name", dbObject.getString("name")).put("id", dbObject.getInteger("id"))
              .put("markdown", dbObject.getString("content")).put("html", Processor.process(dbObject.getString("content")));
          response.put("success", true).put("page", payload);
          context.response().setStatusCode(200);
        } else {
          context.response().setStatusCode(404);
          response.put("success", false).put("error", "There is no page with ID " + id);
        }
      } else {
        response.put("success", false).put("error", reply.cause().getMessage());
        context.response().setStatusCode(500);
      }
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(response.encode());
    });
  }

  private void apiRoot(RoutingContext context) {
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "all-pages-data"); // <2>

    vertx.eventBus().send(wikiDbQueue, new JsonObject(), options, reply -> {
      JsonObject response = new JsonObject();
      if (reply.succeeded()) {
        JsonObject pages = (JsonObject)reply.result().body()/*.stream()
            .map(obj -> new JsonObject().put("id", obj.getInteger("ID")).put("name", obj.getString("NAME"))).collect(Collectors.toList())*/;
        response.put("success", true).put("pages", pages);
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(response.encode());
      } else {
        response.put("success", false).put("error", reply.cause().getMessage());
        context.response().setStatusCode(500);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(response.encode());
      }
    });
  }
}
