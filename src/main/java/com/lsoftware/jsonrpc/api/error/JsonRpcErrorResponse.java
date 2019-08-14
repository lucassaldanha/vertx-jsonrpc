package com.lsoftware.jsonrpc.api.error;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.lsoftware.jsonrpc.api.JsonRpcResponse;

@JsonPropertyOrder({ "jsonrpc", "id", "error" })
public class JsonRpcErrorResponse extends JsonRpcResponse {

  private JsonRpcError error;

  public JsonRpcErrorResponse(String id, JsonRpcError error) {
    super(id);
    this.error = error;
  }

  public JsonRpcError getError() {
    return error;
  }

  public void setError(JsonRpcError error) {
    this.error = error;
  }

}
