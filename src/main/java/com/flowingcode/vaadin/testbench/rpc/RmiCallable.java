/*-
 * #%L
 * RPC for Vaadin TestBench
 * %%
 * Copyright (C) 2021 - 2023 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.flowingcode.vaadin.testbench.rpc;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.JsonCodec;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * Interface that defines the behaviour of a callable RMI object.
 *
 * @author Javier Godoy / Flowing Code
 */
public interface RmiCallable {

  /** Name of the $call method. */
  public static final String RMI_CALL_METHOD = "$call";

  /**
   * Default method for handling RMI invocations.
   *
   * @param invocation JSON representation of the method call.
   * @return the result of the method call
   */
  @ClientCallable
  default JsonValue $call(JsonObject invocation) {
    String id, className;

    if (invocation.hasKey(RmiConstants.RMI_INSTANCE_ID)) {
      id = invocation.getString(RmiConstants.RMI_INSTANCE_ID);
      className = invocation.getString(RmiConstants.RMI_CLASS_NAME);
    } else {
      id = null;
      className = null;
    }

    String methodName = invocation.getString(RmiConstants.RMI_METHOD_NAME);
    JsonArray signatureFromClient = invocation.getArray(RmiConstants.RMI_METHOD_SIGNATURE);

    String argumentsFromClient = invocation.hasKey(RmiConstants.RMI_METHOD_ARGUMENTS)
        ? invocation.getString(RmiConstants.RMI_METHOD_ARGUMENTS)
        : null;

    RmiObjectRegistry registry = RmiObjectRegistry.getInstance((Component) this);

    Class<?>[] signature = new Class<?>[signatureFromClient.length()];
    try {
      for (int i = 0; i < signatureFromClient.length(); i++) {
        signature[i] = RmiCallable$companion.classForName(signatureFromClient.getString(i));
      }

      Class<?> clazz = className == null ? getClass() : Class.forName(className);
      Method method = clazz.getMethod(methodName, signature);

      Object instance = id == null ? this : clazz.cast(registry.lookup(id));

      Object[] args = null;
      if (argumentsFromClient != null) {
        try (ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(Base64.getDecoder().decode(argumentsFromClient))) {
          {
            enableResolveObject(true);
          }

          @Override
          protected Object resolveObject(Object obj) throws java.io.IOException {
            if (obj instanceof RmiStubReplacement) {
              return registry.lookup(((RmiStubReplacement) obj).getInstanceId());
            }
            return obj;
          }
        }) {
          args = (Object[]) ois.readObject();
        }
      }

      Object result = method.invoke(instance, args);

      if (result == null || JsonCodec.canEncodeWithoutTypeInfo(result.getClass())) {
        return JsonCodec.encodeWithTypeInfo(result);
      }

      if (result instanceof RmiRemote) {
        String resultId = registry.register((RmiRemote) result);
        result = registry.lookup(resultId);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(baos) {
        {
          enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws java.io.IOException {
          if (obj instanceof Component) {
            throw new NotSerializableException(
                "Serializing component classes is not supported by TestBench-RPC");
          }
          if (obj instanceof RmiRemote) {
            String id = registry.register((RmiRemote) obj);
            return new RmiRemoteReplacement(registry, id);
          }
          return obj;
        }
      }) {
        oos.writeObject(result);
      }

      String encoded = Base64.getEncoder().encodeToString(baos.toByteArray());
      JsonObject jsonResult = Json.createObject();
      jsonResult.put(RmiConstants.RMI_RESPONSE_MARKER, RmiCallable.class.getName());
      jsonResult.put(RmiConstants.RMI_RESPONSE_DATA, encoded);
      return jsonResult;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}


class RmiCallable$companion {

  /** Class.forName, with support for primitive types. */
  static Class<?> classForName(String className) throws ClassNotFoundException {
    switch (className) {
      case "byte":
        return byte.class;
      case "short":
        return short.class;
      case "char":
        return char.class;
      case "int":
        return int.class;
      case "long":
        return long.class;
      case "float":
        return float.class;
      case "double":
        return double.class;
    }
    return Class.forName(className);
  }
}
