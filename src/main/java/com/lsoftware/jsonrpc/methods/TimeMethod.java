package com.lsoftware.jsonrpc.methods;

import com.lsoftware.jsonrpc.api.JsonRpcMethod;
import com.lsoftware.jsonrpc.api.JsonRpcResult;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TimeMethod implements JsonRpcMethod {

  @Override
  public String name() {
    return "time";
  }

  @Override
  public JsonRpcResult process(final List<?> params) {
    if (params == null || params.isEmpty()) {
      return new JsonRpcResult(formatDate(LocalDateTime.now(ZoneId.of("UTC"))));
    }

    try {
      ZoneId zoneId = ZoneId.of((String) params.get(0));
      return new JsonRpcResult(formatDate(LocalDateTime.now(zoneId)));
    } catch (DateTimeException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private String formatDate(LocalDateTime localDateTime) {
    return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
  }
}
