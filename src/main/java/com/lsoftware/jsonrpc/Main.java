package com.lsoftware.jsonrpc;

import com.lsoftware.jsonrpc.http.HttpJsonRpcServer;
import com.lsoftware.jsonrpc.methods.MathMethodsGroup;
import com.lsoftware.jsonrpc.websockets.WebSocketJsonRpcServer;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    LOG.info("Starting up...");

    Vertx vertx = Vertx.vertx();

    Future<String> jsonRpcProcessorFuture = vertx.deployVerticle(new JsonRpcMessageProcessor());

    JsonRpcMethodRegistry methodRegistry = new JsonRpcMethodRegistry();
    methodRegistry.addMethodGroup(new MathMethodsGroup());
    Future<String> methodRegistryFuture = vertx.deployVerticle(methodRegistry);

    Future<String> httpServerFuture = vertx.deployVerticle(new HttpJsonRpcServer());
    Future<String> websocketServerFuture = vertx.deployVerticle(new WebSocketJsonRpcServer());

    CompositeFuture future = CompositeFuture.join(
        jsonRpcProcessorFuture,
        methodRegistryFuture,
        httpServerFuture,
        websocketServerFuture);
    future.onSuccess((f) -> LOG.info("Startup complete!"));

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("Shutting down...");
      vertx.close().onComplete((result) -> LOG.info("Shutdown complete!"));
    }));
  }
}
