package org.typeunsafe;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.core.json.JsonObject;

import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.StaticHandler;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

import java.util.Optional;
//import java.util.function.*;
//import java.util.List;

public class WebApp extends AbstractVerticle {
  
  private Router defineRoutes(Router router) {
    
    router.route().handler(BodyHandler.create());

    router.post("/api/ping").handler(context -> {
      String name = Optional.ofNullable(context.getBodyAsJson().getString("name")).orElse("John Doe");
      System.out.println("🤖 called by " + name);

      context.response()
        .putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          new JsonObject().put("message", "👋 hey "+ name + " 😃").toString()
        );
    });

    router.get("/api/ping").handler(context -> {
      context.response()
        .putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          new JsonObject().put("message", "🏓 pong!").toString()
        );
    });

    // serve static assets, see /resources/webroot directory
    router.route("/*").handler(StaticHandler.create());

    return router;
  }

  public void start() {
    
    /* === Define routes and start the server === */
    Router router = Router.router(vertx);
    defineRoutes(router);
    Integer httpPort = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8080"));
    HttpServer server = vertx.createHttpServer();

    server
      .requestHandler(router::accept)
      .rxListen(httpPort)
      .subscribe(
        successfulHttpServer -> {
          System.out.println("🌍 Listening on " + successfulHttpServer.actualPort());

        },
        failure -> {
          System.out.println("😡 Houston, we have a problem: " + failure.getMessage());
        }
      );
  }

}
