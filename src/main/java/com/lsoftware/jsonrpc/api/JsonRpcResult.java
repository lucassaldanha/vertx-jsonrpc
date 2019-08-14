package com.lsoftware.jsonrpc.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lsoftware.jsonrpc.api.error.JsonRpcError;

public class JsonRpcResult {

  @JsonProperty("success")
  private boolean success;
  @JsonProperty("result")
  private Object result;
  @JsonProperty("error")
  private JsonRpcError error;

  public JsonRpcResult(Object result) {
    this.result = result;
    this.success = true;
    this.error = null;
  }

  public JsonRpcResult(JsonRpcError error) {
    this.error = error;
    this.success = false;
    this.result = null;
  }

   JsonRpcResult() {
  }

  public boolean isSuccess() {
    return success;
  }

  public Object getResult() {
    return result;
  }

  public JsonRpcError getError() {
    return error;
  }

}
