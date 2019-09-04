package com.lsoftware.jsonrpc;

import static com.lsoftware.jsonrpc.JsonRpcMessageProcessor.JSONRPC_PROCESSOR_EVENTBUS_ADDRESS;
import static com.lsoftware.jsonrpc.api.JsonRpcMethod.JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX;

import com.lsoftware.jsonrpc.api.JsonRpcResult;
import com.lsoftware.jsonrpc.api.JsonRpcSuccessResponse;
import com.lsoftware.jsonrpc.api.error.InvalidParamsJsonRpcError;
import com.lsoftware.jsonrpc.api.error.JsonRpcError;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponse;
import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponses;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class JsonRpcMessageProcessorTest {

  private EventBus eventBus;

  @Before
  public void before(TestContext context) {
    Vertx vertx = Vertx.vertx();
    eventBus = vertx.eventBus();

    JsonRpcMessageProcessor jsonRpcMessageProcessor = new JsonRpcMessageProcessor();
    vertx.deployVerticle(jsonRpcMessageProcessor, context.asyncAssertSuccess());
  }

  @Test
  public void invalidJsonShouldReturnParseError(TestContext context) {
    Async async = context.async();

    String parseErrorResponse = Json.encode(JsonRpcErrorResponses.parseError());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{",
        assertExpectedResponse(context, async, parseErrorResponse));
  }

  @Test
  public void invalidJsonRpcRequestShouldReturnInvalidRequest(TestContext context) {
    Async async = context.async();

    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{}",
        assertExpectedResponse(context, async, expectedInvalidRequestResponse));
  }

  @Test
  public void absentMethodShouldReturnMethodNotFound(TestContext context) {
    Async async = context.async();

    String expectedMethodNotFoundResponse = Json.encode(JsonRpcErrorResponses.methodNotFound("1"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, async, expectedMethodNotFoundResponse));
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

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, async, expectedInvalidParamsResponse));
  }

  @Test
  public void unhandledErrorShouldReturnInternalError(TestContext context) {
    Async async = context.async();

    String expectedInternalErrorResponse = Json
        .encode(JsonRpcErrorResponses.internalError("1", "foo"));

    prepareResponse(msg -> msg.fail(-999, "foo"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, async, expectedInternalErrorResponse));
  }

  @Test
  public void unsuccessfulJsonRpcResultShouldReturnJsonRpcErrorResponse(TestContext context) {
    Async async = context.async();

    JsonRpcError expectedError = new JsonRpcError(-32000, "aError", "foo");
    String expectedErrorResponse = Json.encode(new JsonRpcErrorResponse("1", expectedError));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult(expectedError))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, async, expectedErrorResponse));
  }

  @Test
  public void successfulJsonRpcResultShouldReturnJsonRpcSuccessfulResponse(TestContext context) {
    Async async = context.async();

    String expectedResponse = Json.encode(new JsonRpcSuccessResponse("1", "aResponse"));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, async, expectedResponse));
  }

  @Test
  public void batchRequestWithEmptyArrayShouldReturnInvalidRequest(TestContext context) {
    Async async = context.async();

    String batchRequest = "[]";
    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, async, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithOneInvalidRequestsShouldReturnInvalidRequestResponseObject(
      TestContext context) {
    Async async = context.async();

    String batchRequest = "[1]";
    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, async, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithInvalidRequestsShouldReturnInvalidRequestForEachOne(
      TestContext context) {
    Async async = context.async();

    String batchRequest = "[1,2,3]";
    String expectedInvalidRequestResponse = Json.encode(Arrays.asList(
        JsonRpcErrorResponses.invalidRequest(),
        JsonRpcErrorResponses.invalidRequest(),
        JsonRpcErrorResponses.invalidRequest()
    ));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, async, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithOneValidRequestsShouldReturnOneResponse(
      TestContext context) {
    Async async = context.async();

    String batchRequest = "[{}, {\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"}]";
    String expectedResponse = Json.encode(Arrays.asList(
        JsonRpcErrorResponses.invalidRequest(),
        new JsonRpcSuccessResponse("1", "aResponse")
    ));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, async, expectedResponse));
  }

  @Test
  public void batchRequest(TestContext context) {
    Async async = context.async();

    String batchRequest = "[{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"},"
        + "{\"jsonrpc\": \"2.0\", \"id\": 2, \"method\": \"aMethod\"}]";

    String expectedBatchResponse = "[{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":\"aResponse\"}"
        + ",{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"result\":\"aResponse\"}]";

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, async, expectedBatchResponse));
  }

  private void prepareResponse(Handler<Message<Object>> handler) {
    eventBus.consumer(JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + "aMethod", handler);
  }

  private String request() {
    return "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"}";
  }

  private Handler<AsyncResult<Message<Object>>> assertExpectedResponse(TestContext context,
      Async async, String expectedResponse) {
    return resp -> {
      context.assertEquals(expectedResponse, resp.result().body());
      async.complete();
    };
  }

}