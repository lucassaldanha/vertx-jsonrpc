# JSON-RPC 2.0 Service

This is an example implementation of a JSON-RPC 2.0 service in Java using 
[Vert.x](https://vertx.io/) 3.

The main principles behind this implementation are:
- [JSON-RPC 2.0 spec](https://www.jsonrpc.org/specification) compliant
- Channel agnostic: Thin integration layer between the JSON-RPC code and the transport channel 
(e.g. HTTP, WebSockets, IPC, etc)
- Extendable: New JSON-RPC methods can be added with low effort

This project started as a prototype and now it can help others also implementing JSON-RPC services 
using Vert.x.

## Architecture
There are four main components in this architecture: 
- JsonRpcServer
- JsonRpcProcessor
- JsonRpcMethodRegistry
- JsonRpcMethod

Each one of these components have been implemented as 
[Verticles](https://vertx.io/docs/vertx-core/java/#_verticles) and communicate via the 
[EventBus](https://vertx.io/docs/vertx-core/java/#_verticles).

### JsonRpcServer
The server is responsible for the handling different channels. For example. the HttpJsonRpcServer 
handles HTTP requests.

For each different channel that we want to support, we need a new JsonRpcServer that will 
encode/decode messaged from the channel to the JsonRpcProcessor.

### JsonRpcProcessor
The processor is a main piece in the JSON-RPC service. It is responsible for parsing the messages 
received from the server and decoding it into a JSON-RPC request. On a successful decoding, the 
processor will dispatch the request to the respective JSON-RPC method to get a result.

### JsonRpcMethodRegistry
The registry is where all implemented JSON-RPC methods are registered and made available to the
application. For each JsonRpcMethod, the registry will setup the proper listeners on the EventBus.

### JsonRpcMethod
Each JSON-RPC method should implement this interface and be registered in the registry to be available.
There are two example methods implemented, one to add and one to subtract numbers.

## Example
```
--> request
<-- response

--> { "jsonrpc": "2.0", "id": "1", "method": "add", "params": [2, 3] }
<-- { "jsonrpc": "2.0", "id": "1", "result": 5 }

--> { "jsonrpc": "2.0", "id": "1", "method": "subtract", "params": [5, 1] }
<-- { "jsonrpc": "2.0", "id": "1", "result": 4 }
```

If you want to test yourself, run Main.java and use the following command:
```
$ curl --request POST \
  --url http://localhost:8080 \
  --data '{"jsonrpc": "2.0", "method": "add", "params": [2, 3], "id": "1"}'

${"jsonrpc":"2.0","id":"1","result":5}
```
(by default, the application starts a HTTP server listening on port 8080)

## Coming soon
- Support for by-name parameters
- Support for batch requests
- Extended config option (e.g. http server listening port)