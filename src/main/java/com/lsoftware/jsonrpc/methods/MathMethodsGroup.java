package com.lsoftware.jsonrpc.methods;

import com.lsoftware.jsonrpc.api.JsonRpcMethod;
import java.util.Collection;
import java.util.HashSet;

public class MathMethodsGroup implements JsonRpcMethodGroup {

  private final Collection<JsonRpcMethod> methods = new HashSet<>();

  public MathMethodsGroup() {
    methods.add(new AddMethod());
    methods.add(new SubtractMethod());
  }

  @Override
  public String name() {
    return "math";
  }

  @Override
  public Collection<JsonRpcMethod> methods() {
    return methods;
  }
}
