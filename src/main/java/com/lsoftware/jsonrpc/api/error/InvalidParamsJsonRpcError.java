package com.lsoftware.jsonrpc.api.error;

public class InvalidParamsJsonRpcError extends JsonRpcError {

  public InvalidParamsJsonRpcError(String details) {
    super(-32602, "Invalid method parameter(s)", details);
  }
}
