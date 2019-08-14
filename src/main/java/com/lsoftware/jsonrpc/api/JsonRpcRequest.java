package com.lsoftware.jsonrpc.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.StringJoiner;

public class JsonRpcRequest {

  private String jsonrpc;
  private String id;
  private String method;
  private List<?> params;

  public JsonRpcRequest(String id, String method, List<?> params) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  JsonRpcRequest() {
  }

  public String getJsonrpc() {
    return jsonrpc;
  }

  public String getMethod() {
    return method;
  }

  public List<?> getParams() {
    return params;
  }

  public String getId() {
    return id;
  }

  @JsonIgnore
  public boolean isValid() {
    if (!"2.0".equals(jsonrpc)) {
      return false;
    }

    if (method == null || "".equals(method)) {
      return false;
    }
    // TODO review validation

    return true;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", JsonRpcRequest.class.getSimpleName() + "[", "]")
        .add("jsonrpc='" + jsonrpc + "'")
        .add("id='" + id + "'")
        .add("method='" + method + "'")
        .add("params=" + params)
        .toString();
  }
}
