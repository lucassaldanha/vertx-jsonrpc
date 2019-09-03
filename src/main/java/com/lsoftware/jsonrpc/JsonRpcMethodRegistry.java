package com.lsoftware.jsonrpc;

import com.lsoftware.jsonrpc.api.JsonRpcMethod;
import com.lsoftware.jsonrpc.api.JsonRpcRequest;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import com.lsoftware.jsonrpc.methods.JsonRpcMethodGroup;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcMethodRegistry extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRpcProcessor.class);

  private final Map<String, JsonRpcMethod> availableMethods = new HashMap<>();
  private final Set<MessageConsumer<?>> consumers = new ConcurrentHashSet<>();
  private final AtomicBoolean started = new AtomicBoolean(false);

  @Override
  public void start(Promise<Void> startPromise) {
    List<Future> futures = new ArrayList<>();

    availableMethods.forEach((name, method) -> {
      Promise<Void> promise = Promise.promise();
      futures.add(promise.future());

      LOG.debug("Registering method '{}'", name);

      MessageConsumer<Object> consumer = vertx.eventBus()
          .consumer(method.eventBusAddress(), msg -> {
            LOG.trace("Method {} consuming message {}", method.name(), msg.body());

            JsonRpcRequest request = Json.decodeValue((String) msg.body(), JsonRpcRequest.class);
            JsonRpcResult result = method.process(request.getParams());
            msg.reply(Json.encode(result));
          });
      consumer.completionHandler(promise);
      consumers.add(consumer);
    });

    CompositeFuture.all(futures).setHandler(ar -> {
      if (ar.succeeded()) {
        started.set(true);
        startPromise.complete();
      } else {
        startPromise.fail(ar.cause());
      }
    });
  }

  @Override
  public void stop(Promise<Void> endFuture) {
    List<Future> futures = new ArrayList<>();
    consumers.forEach(c -> {
      Promise<Void> promise = Promise.promise();
      futures.add(promise.future());
      c.unregister(promise);
    });

    CompositeFuture.all(futures).setHandler(ar -> {
      if (ar.succeeded()) {
        consumers.clear();
        started.set(false);
        endFuture.complete();
      } else {
        endFuture.fail(ar.cause());
      }
    });
  }


  public void addMethod(JsonRpcMethod method) {
    if (!started.get()) {
      availableMethods.put(method.name(), method);
    } else {
      LOG.warn("Can't add new methods after the registry has been started");
    }
  }

  public void addMethodGroup(JsonRpcMethodGroup group) {
    if (!started.get()) {
      group.methods().forEach(this::addMethod);
    } else {
      LOG.warn("Can't add new methods after the registry has been started");
    }
  }
}
