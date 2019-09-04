package com.lsoftware.jsonrpc;

import com.lsoftware.jsonrpc.api.error.JsonRpcErrorResponse;

public class JsonRpcException extends RuntimeException {

  private final JsonRpcErrorResponse errorResponse;

  public JsonRpcException(JsonRpcErrorResponse errorResponse) {
    this.errorResponse = errorResponse;
  }

  public JsonRpcErrorResponse getErrorResponse() {
    return errorResponse;
  }
}
