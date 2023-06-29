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
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
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

    RmiObjectRegistry registry = RmiObjectRegistry.getInstance((Component) this);

    String id, className, methodName, argumentsFromClient;
    JsonArray signatureFromClient;

    try {

      try {
        if (invocation.hasKey(RmiConstants.RMI_INSTANCE_ID)) {
          id = invocation.getString(RmiConstants.RMI_INSTANCE_ID);
        } else {
          id = null;
        }

        if (invocation.hasKey(RmiConstants.RMI_CLASS_NAME)) {
          className = invocation.getString(RmiConstants.RMI_CLASS_NAME);
          if (id == null) {
            throw new IllegalArgumentException();
          }
        } else {
          className = null;
          if (id != null) {
            throw new IllegalArgumentException();
          }
        }

        methodName = invocation.getString(RmiConstants.RMI_METHOD_NAME);
        signatureFromClient = invocation.getArray(RmiConstants.RMI_METHOD_SIGNATURE);

        argumentsFromClient = invocation.hasKey(RmiConstants.RMI_METHOD_ARGUMENTS)
            ? invocation.getString(RmiConstants.RMI_METHOD_ARGUMENTS)
            : null;
      } catch (Exception e) {
        return RmiCallable$companion.createException(RmiError.E_PROTOCOL_ERROR, e);
      }

      Class<?>[] signature = new Class<?>[signatureFromClient.length()];

      Class<?> clazz;
      try {
        clazz = className == null ? getClass() : Class.forName(className);
      } catch (ClassNotFoundException e) {
        return RmiCallable$companion.createException(RmiError.E_CLASS_NOT_FOUND);
      }

      Method method;
      try {
        for (int i = 0; i < signatureFromClient.length(); i++) {
          String argClassName = signatureFromClient.getString(i);
          signature[i] = RmiCallable$companion.classForName(argClassName, clazz);
        }
        method = clazz.getMethod(methodName, signature);
      } catch (ClassNotFoundException | NoSuchMethodException e) {
        return RmiCallable$companion.createException(RmiError.E_NO_SUCH_METHOD);
      }

      Object instance;
      if (id == null) {
        instance = this;
      } else {
        try {
          instance = registry.lookup(id);
        } catch (RpcException e) {
          return RmiCallable$companion.createException(RmiError.E_OBJECT_NOT_EXIST);
        }
        clazz.cast(instance);
      }

      Object[] args = null;
      if (argumentsFromClient != null) {
        byte[] decoded;
        try {
          decoded = Base64.getDecoder().decode(argumentsFromClient);
        } catch (IllegalArgumentException e) {
          return RmiCallable$companion.createException(RmiError.E_UNMARSHAL, e);
        }

        try (ObjectInputStream ois = new ObjectInputStream(
            new ByteArrayInputStream(decoded)) {
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

          @Override
          protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc)
              throws ClassNotFoundException, IOException {
            return RmiCallable$companion.classForName(desc.getName(), clazz);
          }

        }) {
          args = (Object[]) ois.readObject();
        } catch (IOException e) {
          return RmiCallable$companion.createException(RmiError.E_UNMARSHAL, e);
        }
      }

      Object result;
      try {
        result = method.invoke(instance, args);
      } catch (InvocationTargetException e) {
        return RmiCallable$companion.createException(RmiError.E_INVOKE, e.getCause());
      }

      if (result == null || JsonCodec.canEncodeWithoutTypeInfo(result.getClass())) {
        return JsonCodec.encodeWithTypeInfo(result);
      }

      try {
        return RmiCallable$companion.createResponse(registry, result);
      } catch (ObjectStreamException e) {
        return RmiCallable$companion.createException(RmiError.E_MARSHAL, e);
      }

    } catch (Exception e) {
      try {
        return RmiCallable$companion.createException(RmiError.E_UNKNOWN, e);
      } catch (IOException e1) {
        throw new UndeclaredThrowableException(e);
      }
    }
  }

}


class RmiCallable$companion {

  private static String encode(RmiObjectRegistry registry, Object result) throws IOException {
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

    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  static JsonObject createResponse(RmiObjectRegistry registry, Object result) throws IOException {
    JsonObject jsonResult = Json.createObject();
    jsonResult.put(RmiConstants.RMI_RESPONSE_MARKER, RmiCallable.class.getName());
    jsonResult.put(RmiConstants.RMI_RESPONSE_DATA, encode(registry, result));
    return jsonResult;
  }

  static JsonObject createException(RmiError error) throws IOException {
    return createException(error, null);
  }

  static JsonObject createException(RmiError error, Throwable t) throws IOException {
    if (error.hasException() ^ (t != null)) {
      throw new IllegalArgumentException();
    }
    JsonObject jsonResult = Json.createObject();
    jsonResult.put(RmiConstants.RMI_RESPONSE_MARKER, RmiCallable.class.getName());
    jsonResult.put(RmiConstants.RMI_RESPONSE_ERROR, error.name());
    if (t != null) {
      jsonResult.put(RmiConstants.RMI_RESPONSE_DATA, encode(null, t));
    }
    return jsonResult;
  }

  /** Class.forName, with support for primitive types. */
  static Class<?> classForName(String className, Class<?> caller) throws ClassNotFoundException {
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
    if (className.equals(RmiStubReplacement.class.getName())) {
      return RmiStubReplacement.class;
    } else {
      return Class.forName(className, false, caller.getClassLoader());
    }
  }
}
