package com.lsoftware.jsonrpc.api;

import java.util.List;

public interface JsonRpcMethod {

  String JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX = "jsonrpc.method.";

  String name();

  JsonRpcResult process(List<?> params);

  default String eventBusAddress() {
    return JSONRPC_METHOD_EVENTBUS_ADDRESS_PREFIX + name();
  }
}
