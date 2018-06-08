package cn.henio;import io.vertx.core.AbstractVerticle;import io.vertx.core.Context;import io.vertx.core.eventbus.EventBus;import io.vertx.core.http.HttpServerOptions;import io.vertx.core.http.HttpServerResponse;import io.vertx.core.net.NetServer;import io.vertx.core.net.NetSocket;/** * created by mouzhanpeng at 2018/2/6 13:31 * * @since jdk 1.9.0 */public class FirstVerticle extends AbstractVerticle{    @Override    public void start() throws Exception {        vertx.createHttpServer(new HttpServerOptions().setLogActivity(true)).requestHandler(req -> {            System.err.println("path:--" + req.path());            if("ok".equals(req.getParam("key"))){                req.response().sendFile("view/test.html");            }else if (req.path().equals("/upload")){                System.err.println(req.getHeader("content-type"));                req.setExpectMultipart(true);                /*req.endHandler(v -> {                    // 请求体被完全读取，所以直接读取表单属性                    MultiMap formAttributes = req.formAttributes();                    System.err.println(formAttributes);                });*/                req.uploadHandler(upload -> {                    System.out.println("Got a file upload " + upload.name());                });                // 不支持               HttpServerResponse response = req.response();               /*  // 推送main.js到客户端                response.push(HttpMethod.GET, "/main.js", ar -> {                    if (ar.succeeded()) {                        // 服务器准备推送响应                        HttpServerResponse pushedResponse = ar.result();                        // 发送main.js响应                        pushedResponse.                            putHeader("content-type", "application/json").                            end("alert(\"Push response hello\")");                    } else {                        System.out.println("Could not push client resource " + ar.cause());                    }                });*/                // 发送请求的资源内容                response.sendFile("<html><head><script src=\"/main.js\"></script></head><body></body></html>");            }        }).listen(8080);        vertx.setPeriodic(1500, (l) -> {            vertx.createHttpClient().getNow(8080, "localhost", "/", resp -> {                resp.bodyHandler(body -> {                    System.out.println(body.toString("ISO-8859-1"));                });            });            vertx.createNetClient().connect(14321, "localhost", res -> {                if (res.succeeded()) {                    System.out.println("Connected!");                    NetSocket socket = res.result();                    socket.write("hello!");                } else {                    System.out.println("Failed to connect: " + res.cause().getMessage());                }            });        });        Context context = vertx.getOrCreateContext();        if (context.isEventLoopContext()) {            System.out.println("Context attached to Event Loop");        } else if (context.isWorkerContext()) {            System.out.println("Context attached to Worker Thread");        } else if (context.isMultiThreadedWorkerContext()) {            System.out.println("Context attached to Worker Thread - multi threaded worker");        } else if (! Context.isOnVertxThread()) {            System.out.println("Context not attached to a thread managed by vert.x");        }        EventBus eventBus = vertx.eventBus();        eventBus.consumer("mi.me").handler(m -> System.out.println(m.body()));    }}