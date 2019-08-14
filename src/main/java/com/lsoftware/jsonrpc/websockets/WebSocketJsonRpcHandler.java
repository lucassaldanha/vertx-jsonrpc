package com.lsoftware.jsonrpc.websockets;

import static com.lsoftware.jsonrpc.JsonRpcProcessor.JSONRPC_PROCESSOR_EVENTBUS_ADDRESS;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;

class WebSocketJsonRpcHandler implements Handler<ServerWebSocket> {

  private final Vertx vertx;

  WebSocketJsonRpcHandler(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void handle(ServerWebSocket websocket) {
    websocket.handler(buffer -> {
      vertx.eventBus()
          .request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, buffer.toString(), ar -> {
            String body = (String) ar.result().body();
            vertx.eventBus().send(websocket.textHandlerID(), body);
          });
    });
  }
}
