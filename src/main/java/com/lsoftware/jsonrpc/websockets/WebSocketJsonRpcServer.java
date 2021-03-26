package com.lsoftware.jsonrpc.websockets;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketJsonRpcServer extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(WebSocketJsonRpcServer.class);

  private HttpServer httpServer;

  @Override
  public void start(Promise<Void> startFuture) {
    HttpServerOptions options = new HttpServerOptions()
        .setPort(8081);

    httpServer = vertx.createHttpServer(options);
    httpServer.webSocketHandler(new WebSocketJsonRpcHandler(vertx));

    httpServer.listen(res -> {
      if (res.succeeded()) {
        LOG.info("WebSocket server started and listening on port {}", res.result().actualPort());
        startFuture.complete();
      } else {
        startFuture.fail(res.cause());
      }
    });
  }

  public void stop(Promise<Void> endFuture) {
    httpServer.close(endFuture);
  }
}
