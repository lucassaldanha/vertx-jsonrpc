package com.lsoftware.jsonrpc.methods;

import com.lsoftware.jsonrpc.api.JsonRpcMethod;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import java.util.List;

public class AddMethod implements JsonRpcMethod {

  @Override
  public String name() {
    return "add";
  }

  @Override
  public JsonRpcResult process(List<?> params) {
    Integer p1 = (Integer) params.get(0);
    Integer p2 = (Integer) params.get(1);

    return new JsonRpcResult(p1 + p2);
  }
}
