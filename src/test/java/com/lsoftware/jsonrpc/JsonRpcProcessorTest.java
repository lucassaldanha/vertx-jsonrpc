package com.lsoftware.jsonrpc;

import static com.lsoftware.jsonrpc.JsonRpcProcessor.JSONRPC_PROCESSOR_EVENTBUS_ADDRESS;
import static com.lsoftware.jsonrpc.api.JsonRpcMethod.JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX;

import com.lsoftware.jsonrpc.api.error.InvalidParamsJsonRpcError;
import com.lsoftware.jsonrpc.api.error.JsonRpcError;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponse;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponses;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class JsonRpcProcessorTest {

  private EventBus eventBus;

  @Before
  public void before(TestContext context) {
    Vertx vertx = Vertx.vertx();
    eventBus = vertx.eventBus();

    JsonRpcProcessor jsonRpcProcessor = new JsonRpcProcessor();
    vertx.deployVerticle(jsonRpcProcessor, context.asyncAssertSuccess());
  }

  @Test
  public void invalidJsonShouldReturnParseError(TestContext context) {
    Async async = context.async();

    String parseErrorResponse = Json.encode(JsonRpcErrorResponses.parseError());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{", resp -> {
      context.assertEquals(parseErrorResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void invalidJsonRpcRequestShouldReturnInvalidRequest(TestContext context) {
    Async async = context.async();

    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{}", resp -> {
      context.assertEquals(expectedInvalidRequestResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void absentMethodShouldReturnMethodNotFound(TestContext context) {
    Async async = context.async();

    String expectedMethodNotFoundResponse = Json.encode(JsonRpcErrorResponses.methodNotFound("1"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(), resp -> {
      context.assertEquals(expectedMethodNotFoundResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void invalidParamsShouldReturnInvalidParams(TestContext context) {
    Async async = context.async();

    String expectedInvalidParamsResponse = Json
        .encode(JsonRpcErrorResponses.invalidParams("1", "foo"));

    prepareResponse(msg -> {
      InvalidParamsJsonRpcError foo = new InvalidParamsJsonRpcError("foo");
      msg.reply(Json.encode(new JsonRpcResult(foo)));
    });

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(), resp -> {
      context.assertEquals(expectedInvalidParamsResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void unhandledErrorShouldReturnInternalError(TestContext context) {
    Async async = context.async();

    String expectedInternalErrorResponse = Json
        .encode(JsonRpcErrorResponses.internalError("1", "foo"));

    prepareResponse(msg -> msg.fail(-999, "foo"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(), resp -> {
      context.assertEquals(expectedInternalErrorResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void unsuccessfulJsonRpcResultShouldReturnJsonRpcErrorResponse(TestContext context) {
    Async async = context.async();

    JsonRpcError expectedError = new JsonRpcError(-32000, "aError", "foo");
    String expectedErrorResponse = Json.encode(new JsonRpcErrorResponse("1", expectedError));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult(expectedError))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(), resp -> {
      context.assertEquals(expectedErrorResponse, resp.result().body());
      async.complete();
    });
  }

  @Test
  public void successfulJsonRpcResultShouldReturnJsonRpcSuccessfulResponse(TestContext context) {
    Async async = context.async();

    String expectedResponse = "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":\"aResponse\"}";

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(), resp -> {
      context.assertEquals(expectedResponse, resp.result().body());
      async.complete();
    });
  }

  private String request() {
    return "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"}";
  }

  private void prepareResponse(Handler<Message<Object>> handler) {
    eventBus.consumer(JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + ".aMethod", handler);
  }

}