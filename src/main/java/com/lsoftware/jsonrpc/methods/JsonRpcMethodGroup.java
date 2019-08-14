package com.lsoftware.jsonrpc.methods;

import com.lsoftware.jsonrpc.api.JsonRpcMethod;
import java.util.Collection;

public interface JsonRpcMethodGroup {

  String name();

  Collection<JsonRpcMethod> methods();

}
