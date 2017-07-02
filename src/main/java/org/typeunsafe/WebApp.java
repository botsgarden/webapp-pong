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

import org.redisson.config.Config;
import org.redisson.api.*;
import org.redisson.Redisson;

//import io.vavr.control.*;

public class WebApp extends AbstractVerticle {

  private RedissonClient redisson;
  private RBucket<JsonObject> bucket;

  public void stop(Future<Void> stopFuture) {
    System.out.println("👋 bye bye ");
    stopFuture.complete();

  }


  public void start() {
    
    Router router = Router.router(vertx);

    Integer httpPort = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8080"));
    HttpServer server = vertx.createHttpServer();

    router.route().handler(BodyHandler.create());

    // === Redisson Part === 
    Config config = new Config();



    config.useSingleServer().setAddress(
      Optional.ofNullable(System.getenv("REDIS_URL")).orElse("redis://127.0.0.1:6379")
    ).setPassword(Optional.ofNullable(System.getenv("REDIS_PASSWORD")).orElse(null));
    redisson = Redisson.create(config);
    bucket = redisson.getBucket("ball");

    bucket.set(new JsonObject().put("message","empty"));

    router.post("/api/pong").handler(context -> {
      bucket.set(context.getBodyAsJson());
      System.out.println("🤖 bucket updated");

      context.response()
        .putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          new JsonObject().put("message", "👋 hey bucket updated 😃").toString()
        );
    });

    router.get("/api/pong").handler(context -> {
      context.response()
        .putHeader("content-type", "application/json;charset=UTF-8")
        .end(
          bucket.get().encodePrettily()
        );
    });

    /* === Start the server === */

    // serve static assets, see /resources/webroot directory
    router.route("/*").handler(StaticHandler.create());

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
