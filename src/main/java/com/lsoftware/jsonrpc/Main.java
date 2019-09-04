package com.lsoftware.jsonrpc;

import com.lsoftware.jsonrpc.http.HttpJsonRpcServer;
import com.lsoftware.jsonrpc.methods.MathMethodsGroup;
import com.lsoftware.jsonrpc.websockets.WebSocketJsonRpcServer;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOG = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    Promise<String> jsonRpcProcessorPromise = Promise.promise();
    vertx.deployVerticle(new JsonRpcMessageProcessor(), jsonRpcProcessorPromise);

    JsonRpcMethodRegistry methodRegistry = new JsonRpcMethodRegistry();
    methodRegistry.addMethodGroup(new MathMethodsGroup());
    Promise<String> methodRegistryPromise = Promise.promise();
    vertx.deployVerticle(methodRegistry, methodRegistryPromise);

    Promise<String> httpServerPromise = Promise.promise();
    vertx.deployVerticle(new HttpJsonRpcServer(), httpServerPromise);

    Promise<String> websocketServerPromise = Promise.promise();
    vertx.deployVerticle(new WebSocketJsonRpcServer(), websocketServerPromise);

    List<Future> futures = Arrays.asList(
        jsonRpcProcessorPromise.future(),
        methodRegistryPromise.future(),
        httpServerPromise.future(),
        websocketServerPromise.future()
    );

    CompositeFuture.join(futures).setHandler(ar -> LOG.info("Startup complete"));

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOG.info("Shutting down");
      vertx.close();
    }));
  }
}
