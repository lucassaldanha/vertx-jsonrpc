package com.lsoftware.jsonrpc;

import static com.lsoftware.jsonrpc.api.JsonRpcMethod.JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX;

import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponse;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponses;
import com.lsoftware.jsonrpc.api.JsonRpcRequest;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import com.lsoftware.jsonrpc.api.JsonRpcSuccessResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcProcessor extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRpcProcessor.class);
  public static final String JSONRPC_PROCESSOR_EVENTBUS_ADDRESS = "jsonrpc.processor";
  private MessageConsumer<Object> consumer;

  @Override
  public void start(Promise<Void> startFuture) {
    consumer = vertx.eventBus().consumer(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, messageHandler());
    consumer.completionHandler(startFuture);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    consumer.unregister(stopPromise);
  }

  private Handler<Message<Object>> messageHandler() {
    return msg -> {
      LOG.debug("Processing message {}", msg.body());

      JsonRpcRequest request;
      try {
        request = Json.decodeValue((String) msg.body(), JsonRpcRequest.class);
        if (!request.isValid()) {
          msg.reply(Json.encode(JsonRpcErrorResponses.invalidRequest()));
          return;
        }
      } catch (ClassCastException | DecodeException e) {
        msg.reply(Json.encode(JsonRpcErrorResponses.parseError()));
        return;
      }

      vertx.eventBus()
          .request(JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + request.getMethod(),
              Json.encode(request), response -> {
                if (response.succeeded()) {
                  JsonRpcResult result = Json
                      .decodeValue((String) response.result().body(), JsonRpcResult.class);

                  if (result.isSuccess()) {
                    msg.reply(
                        Json.encode(
                            new JsonRpcSuccessResponse(request.getId(), result.getResult())));
                  } else {
                    msg.reply(
                        Json.encode(new JsonRpcErrorResponse(request.getId(), result.getError())));
                  }
                } else {
                  if (response.cause() instanceof ReplyException) {
                    handleReplyException(msg, request, response);
                  } else {
                    msg.reply(
                        Json.encode(JsonRpcErrorResponses.internalError(request.getId(), null)));
                  }
                }
              });
    };
  }

  private void handleReplyException(Message<Object> msg, JsonRpcRequest request,
      AsyncResult<Message<Object>> resp) {
    ReplyException replyException = (ReplyException) resp.cause();

    switch (replyException.failureType()) {
      case NO_HANDLERS: {
        msg.reply(Json.encode(JsonRpcErrorResponses.methodNotFound(request.getId())));
        return;
      }
      case RECIPIENT_FAILURE: {
        msg.reply(Json.encode(JsonRpcErrorResponses
            .internalError(request.getId(), replyException.getMessage())));
        return;
      }
      case TIMEOUT:
      default:
        msg.reply(
            Json.encode(JsonRpcErrorResponses.internalError(request.getId(), null)));
    }
  }
}
