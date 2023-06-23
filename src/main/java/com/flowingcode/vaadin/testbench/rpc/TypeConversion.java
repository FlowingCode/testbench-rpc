package com.flowingcode.vaadin.testbench.rpc;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.TypeUtils;

@UtilityClass
class TypeConversion {

  static Object cast(Object value, Class<?> returnType) {

    if (returnType == JsonValue.class) {
      return toJsonValue(value);
    }

    if (value == null) {
      return null;
    }

    if (returnType.isInstance(value)) {
      return value;
    }

    if (returnType == Integer.class && value.getClass() == Long.class) {
      return BigInteger.valueOf((Long) value).intValueExact();
    }

    if (returnType == Double.class && value.getClass() == Long.class) {
      return ((Long) value).doubleValue();
    }

    throw new ClassCastException(String.format("Cannot cast %s as %s",
        value.getClass().getName(), returnType.getName()));
  }

  private static JsonValue toJsonValue(Object arg) {
    if (arg == null) {
      return Json.createNull();
    }
    if (arg instanceof Boolean) {
      return Json.create((Boolean) arg);
    }
    if (arg instanceof String) {
      return Json.create((String) arg);
    }
    if (arg instanceof Long) {
      return Json.create((Long) arg);
    }
    if (arg instanceof Double) {
      return Json.create((Double) arg);
    }
    if (arg instanceof List) {
      List<?> list = (List<?>) arg;
      JsonArray array = Json.createArray();
      for (Object e : list) {
        array.set(array.length(), toJsonValue(e));
      }
      return array;
    }
    throw new ClassCastException(String.format("Cannot cast %s as %s",
        arg.getClass().getName(), JsonValue.class));
  }

  static Object fromJsonValue(JsonValue arg) {
    switch (arg.getType()) {
      case BOOLEAN:
        return arg.asBoolean();
      case NUMBER:
        return arg.asNumber();
      case STRING:
        return arg.asString();
      case NULL:
        return null;
      case ARRAY:
        JsonArray array = (JsonArray) arg;
        List<Object> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
          list.add(fromJsonValue(array.get(i)));
        }
        return list;
      default:
        return arg;
    }
  }

  static JsonArrayList<?> castList(List<?> value, Type returnType) {
    if (returnType instanceof ParameterizedType) {
      Type arg = ((ParameterizedType) returnType).getActualTypeArguments()[0];
      Class<?> elementType = TypeUtils.getRawType(arg, null);
      value = ((List<?>) value).stream().map(e -> cast(e, elementType))
          .collect(Collectors.toList());
    }
    return JsonArrayList.wrapForTestbench(value);
  }

  private boolean isValidType(Class<?> type) {
    return type==void.class
        || type == boolean.class
        || type == int.class
        || type == double.class
        || type == Boolean.class
        || type == Integer.class
        || type == Double.class
        || type == String.class
        || type == JsonValue.class;
  }

  static void checkMethod(Method method) {

    for (Class<?> type : method.getParameterTypes()) {
      if (!type.isEnum() && !isValidType(type)) {
        throw new IllegalRpcSignatureException(String
            .format("Argument of type %s is not supported by TestBench-RPC.", type.getName()));
      }
    }

    if (method.getReturnType()==JsonArrayList.class) {
      Type returnType = method.getGenericReturnType();
      if (returnType instanceof ParameterizedType) {
        Type elementType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
        if (elementType == Long.class || elementType == long.class
            || elementType instanceof Class && isValidType((Class<?>) elementType)) {
          return;
        }
      }
      throw new IllegalRpcSignatureException(
          String.format("Return type %s is not supported by TestBench-RPC.", returnType));
    }

    if (!isValidType(method.getReturnType())) {
      throw new IllegalRpcSignatureException(String
          .format("Return type %s is not supported by TestBench-RPC.", method.getReturnType()));
    }
  }

}
