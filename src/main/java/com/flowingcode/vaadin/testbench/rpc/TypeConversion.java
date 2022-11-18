package com.flowingcode.vaadin.testbench.rpc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.TypeUtils;

@UtilityClass
class TypeConversion {

  static Object cast(Object value, Class<?> returnType) {
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

  static JsonArrayList<?> castList(List<?> value, Type returnType) {
    if (returnType instanceof ParameterizedType) {
      Type arg = ((ParameterizedType) returnType).getActualTypeArguments()[0];
      Class<?> elementType = TypeUtils.getRawType(arg, null);
      value = ((List<?>) value).stream().map(e -> cast(e, elementType))
          .collect(Collectors.toList());
    }
    return JsonArrayList.wrapForTestbench(value);
  }

}
