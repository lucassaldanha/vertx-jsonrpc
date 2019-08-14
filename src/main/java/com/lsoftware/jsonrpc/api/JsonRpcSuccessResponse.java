package com.lsoftware.jsonrpc.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"jsonrpc", "id", "result"})
public class JsonRpcSuccessResponse extends JsonRpcResponse {

  private Object result;

  public JsonRpcSuccessResponse(String id, Object result) {
    super(id);
    this.result = result;
  }

  JsonRpcSuccessResponse() {
  }

  public Object getResult() {
    return result;
  }

}
