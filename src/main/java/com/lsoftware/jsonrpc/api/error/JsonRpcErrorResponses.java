package com.lsoftware.jsonrpc.api.error;

public class JsonRpcErrorResponses {

  public static JsonRpcErrorResponse parseError() {
    return new JsonRpcErrorResponse(null, new JsonRpcError(-32700, "Parse error"));
  }

  public static JsonRpcErrorResponse invalidRequest() {
    return new JsonRpcErrorResponse(null, new JsonRpcError(-32600, "Invalid Request"));
  }

  public static JsonRpcErrorResponse methodNotFound(String id) {
    return new JsonRpcErrorResponse(id, new JsonRpcError(-32601, "Method not found"));
  }

  public static JsonRpcErrorResponse invalidParams(String id, Object data) {
    return new JsonRpcErrorResponse(id,
        new JsonRpcError(-32602, "Invalid method parameter(s)", data));
  }

  public static JsonRpcErrorResponse internalError(String id, Object data) {
    return new JsonRpcErrorResponse(id, new JsonRpcError(-32603, "Internal JSON-RPC error", data));
  }

}
