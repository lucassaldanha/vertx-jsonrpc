package com.lsoftware.jsonrpc.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "code", "message", "data" })
public class JsonRpcError {

  private Integer code;
  private String message;
  @JsonInclude(Include.NON_NULL)
  private Object data;

  public JsonRpcError(Integer code, String message) {
    this.code = code;
    this.message = message;
  }

  public JsonRpcError(Integer code, String message, Object data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  JsonRpcError() {
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
