package com.lsoftware.jsonrpc.http;

import static com.lsoftware.jsonrpc.JsonRpcProcessor.JSONRPC_PROCESSOR_EVENTBUS_ADDRESS;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

class HttpJsonRpcHandler implements Handler<HttpServerRequest> {

  private final Vertx vertx;

  HttpJsonRpcHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void handle(HttpServerRequest request) {
    request.bodyHandler(buffer -> {
      vertx.eventBus()
          .request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, buffer.toString(), ar -> {
            if (ar.succeeded()) {
              String body = (String) ar.result().body();
              request.response()
                  .putHeader("Content-Type", "application/json")
                  .end(body);
            } else {
              request.response().setStatusCode(500).end(ar.cause().getMessage());
            }
          });
    });
  }
}
