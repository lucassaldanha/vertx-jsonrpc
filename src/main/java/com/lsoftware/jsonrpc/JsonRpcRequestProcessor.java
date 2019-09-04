package com.lsoftware.jsonrpc;

import static com.lsoftware.jsonrpc.api.JsonRpcMethod.JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX;

import com.lsoftware.jsonrpc.api.JsonRpcRequest;
import com.lsoftware.jsonrpc.api.JsonRpcResponse;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import com.lsoftware.jsonrpc.api.JsonRpcSuccessResponse;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponse;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponses;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcRequestProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRpcRequestProcessor.class);

  private final Vertx vertx;
  private final JsonArray jsonArray;
  private final List<Future> futures = new ArrayList<>();

  JsonRpcRequestProcessor(Vertx vertx, JsonArray jsonArray) {
    this.vertx = vertx;
    this.jsonArray = jsonArray;
  }

  List<Future> processRequests() {
    jsonArray.forEach(json -> {
      LOG.trace("Processing JSON-RPC request '{}'", json);

      final Promise<Object> promise = Promise.promise();
      futures.add(promise.future());

      final JsonRpcRequest request;
      try {
        request = parseRequest(json);
      } catch (JsonRpcException e) {
        promise.complete(e.getErrorResponse());
        return;
      }

      final String eventBusAddress =
          JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + request.getMethod();

      LOG.trace("Dispatching request {} to {}", Json.encode(request), eventBusAddress);

      vertx.eventBus()
          .request(eventBusAddress, Json.encode(request), handleResponse(request, promise));
    });

    return futures;
  }

  private JsonRpcRequest parseRequest(Object json) {
    try {
      JsonRpcRequest request = ((JsonObject) json).mapTo(JsonRpcRequest.class);
      if (!request.isValid()) {
        throw new JsonRpcException(JsonRpcErrorResponses.invalidRequest());
      }
      return request;
    } catch (ClassCastException | DecodeException e) {
      throw new JsonRpcException(JsonRpcErrorResponses.invalidRequest());
    }
  }

  private Handler<AsyncResult<Message<Object>>> handleResponse(JsonRpcRequest request,
      Promise<Object> promise) {
    return response -> {
      final JsonRpcResponse jsonRpcResponse;
      if (response.succeeded()) {
        JsonRpcResult result = Json
            .decodeValue((String) response.result().body(), JsonRpcResult.class);

        if (result.isSuccess()) {
          jsonRpcResponse = new JsonRpcSuccessResponse(request.getId(), result.getResult());
        } else {
          jsonRpcResponse = new JsonRpcErrorResponse(request.getId(), result.getError());
        }
      } else {
        if (response.cause() instanceof ReplyException) {
          jsonRpcResponse = replyExceptionToJsonRpcError(request, response);
        } else {
          jsonRpcResponse = JsonRpcErrorResponses.internalError(request.getId(), null);
        }
      }
      promise.complete(jsonRpcResponse);
    };
  }

  private JsonRpcResponse replyExceptionToJsonRpcError(JsonRpcRequest request,
      AsyncResult<Message<Object>> resp) {
    ReplyException replyException = (ReplyException) resp.cause();

    switch (replyException.failureType()) {
      case NO_HANDLERS: {
        return JsonRpcErrorResponses.methodNotFound(request.getId());
      }
      case RECIPIENT_FAILURE: {
        return JsonRpcErrorResponses
            .internalError(request.getId(), replyException.getMessage());
      }
      case TIMEOUT:
      default:
        return JsonRpcErrorResponses.internalError(request.getId(), null);
    }
  }
}
