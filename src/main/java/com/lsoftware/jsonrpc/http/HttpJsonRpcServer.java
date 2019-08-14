package com.lsoftware.jsonrpc.http;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpJsonRpcServer extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(HttpJsonRpcServer.class);

  private HttpServer httpServer;

  @Override
  public void start(Promise<Void> startPromise) {
    HttpServerOptions options = new HttpServerOptions()
        .setPort(8080);

    httpServer = vertx.createHttpServer(options);
    httpServer.requestHandler(new HttpJsonRpcHandler(vertx));

    httpServer.listen(res -> {
      if (res.succeeded()) {
        LOG.info("HttpServer started and listening on port {}", res.result().actualPort());
        startPromise.complete();
      } else {
        startPromise.fail(res.cause());
      }
    });
  }

  public void stop(Promise<Void> endFuture) {
    httpServer.close(endFuture);
  }

}
