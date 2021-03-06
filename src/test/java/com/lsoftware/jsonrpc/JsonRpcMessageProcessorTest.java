package com.lsoftware.jsonrpc;

import static com.lsoftware.jsonrpc.JsonRpcMessageProcessor.JSONRPC_PROCESSOR_EVENTBUS_ADDRESS;
import static com.lsoftware.jsonrpc.api.JsonRpcMethod.JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

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
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class JsonRpcMessageProcessorTest {

  private EventBus eventBus;

  @BeforeEach
  public void before(Vertx vertx, VertxTestContext context) {
    eventBus = vertx.eventBus();

    JsonRpcMessageProcessor jsonRpcMessageProcessor = new JsonRpcMessageProcessor();
    vertx.deployVerticle(jsonRpcMessageProcessor).onComplete(context.succeedingThenComplete());
  }

  @Test
  public void invalidJsonShouldReturnParseError(VertxTestContext context) {
    String parseErrorResponse = Json.encode(JsonRpcErrorResponses.parseError());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{",
        assertExpectedResponse(context, parseErrorResponse));
  }

  @Test
  public void invalidJsonRpcRequestShouldReturnInvalidRequest(VertxTestContext context) {
    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, "{}",
        assertExpectedResponse(context, expectedInvalidRequestResponse));
  }

  @Test
  public void absentMethodShouldReturnMethodNotFound(VertxTestContext context) {
    String expectedMethodNotFoundResponse = Json.encode(JsonRpcErrorResponses.methodNotFound("1"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, expectedMethodNotFoundResponse));
  }

  @Test
  public void invalidParamsShouldReturnInvalidParams(VertxTestContext context) {
    String expectedInvalidParamsResponse = Json
        .encode(JsonRpcErrorResponses.invalidParams("1", "foo"));

    prepareResponse(msg -> {
      InvalidParamsJsonRpcError foo = new InvalidParamsJsonRpcError("foo");
      msg.reply(Json.encode(new JsonRpcResult(foo)));
    });

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, expectedInvalidParamsResponse));
  }

  @Test
  public void unhandledErrorShouldReturnInternalError(VertxTestContext context) {
    String expectedInternalErrorResponse = Json
        .encode(JsonRpcErrorResponses.internalError("1", "foo"));

    prepareResponse(msg -> msg.fail(-999, "foo"));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, expectedInternalErrorResponse));
  }

  @Test
  public void unsuccessfulJsonRpcResultShouldReturnJsonRpcErrorResponse(VertxTestContext context) {
    JsonRpcError expectedError = new JsonRpcError(-32000, "aError", "foo");
    String expectedErrorResponse = Json.encode(new JsonRpcErrorResponse("1", expectedError));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult(expectedError))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, expectedErrorResponse));
  }

  @Test
  public void successfulJsonRpcResultShouldReturnJsonRpcSuccessfulResponse(
      VertxTestContext context) {
    String expectedResponse = Json.encode(new JsonRpcSuccessResponse("1", "aResponse"));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, request(),
        assertExpectedResponse(context, expectedResponse));
  }

  @Test
  public void batchRequestWithEmptyArrayShouldReturnInvalidRequest(VertxTestContext context) {
    String batchRequest = "[]";
    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithOneInvalidRequestsShouldReturnInvalidRequestResponseObject(
      VertxTestContext context) {
    String batchRequest = "[1]";
    String expectedInvalidRequestResponse = Json.encode(JsonRpcErrorResponses.invalidRequest());

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithInvalidRequestsShouldReturnInvalidRequestForEachOne(
      VertxTestContext context) {
    String batchRequest = "[1,2,3]";
    String expectedInvalidRequestResponse = Json.encode(Arrays.asList(
        JsonRpcErrorResponses.invalidRequest(),
        JsonRpcErrorResponses.invalidRequest(),
        JsonRpcErrorResponses.invalidRequest()
    ));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, expectedInvalidRequestResponse));
  }

  @Test
  public void batchRequestWithOneValidRequestsShouldReturnOneResponse(
      VertxTestContext context) {
    String batchRequest = "[{}, {\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"}]";
    String expectedResponse = Json.encode(Arrays.asList(
        JsonRpcErrorResponses.invalidRequest(),
        new JsonRpcSuccessResponse("1", "aResponse")
    ));

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, expectedResponse));
  }

  @Test
  public void batchRequest(VertxTestContext context) {
    String batchRequest = "[{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"},"
        + "{\"jsonrpc\": \"2.0\", \"id\": 2, \"method\": \"aMethod\"}]";

    String expectedBatchResponse = "[{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"result\":\"aResponse\"}"
        + ",{\"jsonrpc\":\"2.0\",\"id\":\"2\",\"result\":\"aResponse\"}]";

    prepareResponse(msg -> msg.reply(Json.encode(new JsonRpcResult("aResponse"))));

    eventBus.request(JSONRPC_PROCESSOR_EVENTBUS_ADDRESS, batchRequest,
        assertExpectedResponse(context, expectedBatchResponse));
  }

  private void prepareResponse(Handler<Message<Object>> handler) {
    eventBus.consumer(JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + "aMethod", handler);
  }

  private String request() {
    return "{\"jsonrpc\": \"2.0\", \"id\": 1, \"method\": \"aMethod\"}";
  }

  private Handler<AsyncResult<Message<Object>>> assertExpectedResponse(VertxTestContext context,
      String expectedResponse) {
    return resp -> context
        .verify(() -> {
          assertThat(expectedResponse).isEqualTo(resp.result().body());
          context.completeNow();
        });
  }

}