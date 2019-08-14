package com.lsoftware.jsonrpc.api;

public class JsonRpcResponse {

  private final String jsonrpc = "2.0";
  private String id;

  public JsonRpcResponse(String id) {
    this.id = id;
  }

  JsonRpcResponse() {
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public String getId() {
    return id;
  }

}
