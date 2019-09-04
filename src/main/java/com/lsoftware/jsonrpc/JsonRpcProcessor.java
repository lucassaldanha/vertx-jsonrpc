package com.lsoftware.jsonrpc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponses;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcProcessor extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRpcProcessor.class);
  public static final String JSONRPC_PROCESSOR_EVENTBUS_ADDRESS = "jsonrpc.processor";
  private MessageConsumer<Object> consumer;

  @Override
  public void start(Promise<Void> startFuture) {
    Json.mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    consumer = vertx.eventBus().consumer(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, messageHandler());
    consumer.completionHandler(startFuture);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    consumer.unregister(stopPromise);
  }

  private Handler<Message<Object>> messageHandler() {
    return msg -> {
      LOG.trace("Processing incoming message '{}'", msg.body());

      final JsonArray jsonArray;
      try {
        jsonArray = decodeMessage((String) msg.body());
      } catch (JsonRpcException e) {
        msg.reply(Json.encode(e.getErrorResponse()));
        return;
      }

      final JsonRpcRequestProcessor processor = new JsonRpcRequestProcessor(vertx, jsonArray);
      final List<Future> futures = processor.processRequests();

      CompositeFuture.all(futures).setHandler(ar -> {
        CompositeFuture result = ar.result();
        List<Object> responses = result.list();
        if (responses.size() == 1) {
          msg.reply(Json.encode(responses.get(0)));
        } else {
          msg.reply(Json.encode(responses));
        }
      });
    };
  }

  private JsonArray decodeMessage(String msg) {
    JsonArray jsonArray = null;
    try {
      jsonArray = new JsonArray(msg);
      if (jsonArray.isEmpty()) {
        throw new JsonRpcException(JsonRpcErrorResponses.invalidRequest());
      }
    } catch (DecodeException e) {
      throw new JsonRpcException(JsonRpcErrorResponses.parseError());
    }
    return jsonArray;
  }
}
