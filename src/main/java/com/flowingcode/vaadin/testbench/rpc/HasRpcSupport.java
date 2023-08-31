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
import com.vaadin.testbench.HasDriver;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JsonUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.openqa.selenium.JavascriptExecutor;

/**
 * Provides support for Remote Procedure Calls (RPC) using TestBench.
 *
 * @author Javier Godoy / Flowing Code
 */
public interface HasRpcSupport extends HasDriver {

  /**
   * Sets the amount of time to wait for an asynchronous script to finish execution before throwing
   * an error. If the timeout is negative, then the script will be allowed to run indefinitely.
   *
   * @param timeoutMillis The timeout value in milliseconds.
   */
  default void setScriptTimeout(long timeoutMillis) {
    if (timeoutMillis < 0) {
      timeoutMillis = (long) 9e15;
    }
    getDriver().manage().timeouts().setScriptTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Create a TestBench proxy that invokes methods from the interface through a client call.
   */
  default <T> T createCallableProxy(Class<T> intf) {
    return HasRpcSupport$companion.createCallableProxy(this, intf);
  }


  @Deprecated
  default Object call(String callable, Object... arguments) {
    try {
      return new HasRpcSupport$SimpleInvocationHandler(this).call(callable, arguments);
    } catch (RpcCallException e) {
      throw new RpcException(callable, arguments, e.getMessage());
    }
  }

}


class HasRpcSupport$companion {

  private static boolean isRmiSupported(Class<?> interfaces[], String instanceId) {
    for (Class<?> intf : interfaces) {
      if (instanceId == null && RmiCallable.class.isAssignableFrom(intf)) {
        return true;
      }
      if (instanceId != null && RmiRemote.class.isAssignableFrom(intf)) {
        return true;
      }
    }
    return false;
  }

  static <T> T createCallableProxy(HasRpcSupport rpc, Class<T> intf) {
    return intf.cast(createCallableProxy(rpc, new Class<?>[] {intf}, null));
  }

  static Object createCallableProxy(HasRpcSupport rpc, Class<?> interfaces[], String instanceId) {
    final boolean rmiSupported = isRmiSupported(interfaces, instanceId);

    for (Class<?> intf : interfaces) {
      if (!intf.isInterface()) {
        throw new IllegalArgumentException(intf.getName() + " is not an interface");
      }

      for (Method method : intf.getMethods()) {
        if (!Modifier.isStatic(method.getModifiers())) {
          TypeConversion.checkMethod(method, rmiSupported);
        }
      }
    }

    InvocationHandler invocationHandler;
    if (rmiSupported) {
      invocationHandler = new HasRpcSupport$RmiInvocationHandler(rpc, interfaces, instanceId);
    } else {
      invocationHandler = new HasRpcSupport$SimpleInvocationHandler(rpc);
    }

    return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, invocationHandler);
  }

}


/** Representation of a RPC call failure exception. */
@SuppressWarnings("serial")
class RpcCallException extends Exception {
  public RpcCallException(String message) {
    super(message);
  }
  public RpcCallException(String message, Throwable cause) {
    super(message, cause);
  }
}


@RequiredArgsConstructor
abstract class HasRpcSupport$InvocationHandler implements InvocationHandler {

  protected final HasRpcSupport rpc;

  /**
   * Call a {@link ClientCallable} defined on the integration view.
   *
   * @param callable the client callable name
   * @param arguments arguments to be passed to the callable
   * @throws TimeoutException if the callable times out (see
   *         {@link HasRpcSupport#setScriptTimeout(long)})
   * @throws RuntimeException if the callable fails.
   */
  Object call(String callable, Object... arguments)
      throws RpcCallException {
    arguments = Optional.ofNullable(arguments).orElse(new Object[0]);
    for (int i = 0; i < arguments.length; i++) {
      if (arguments[i] instanceof Enum) {
        arguments[i] = ((Enum<?>) arguments[i]).name();
      } else if (arguments[i] instanceof JsonValue) {
        arguments[i] = TypeConversion.fromJsonValue((JsonValue) arguments[i]);
      }
    }
    StringBuilder script = new StringBuilder();

    Object callArguments[] = arguments.clone();

    List<Boolean> raw = new ArrayList<>(arguments.length);
    for (int i = 0; i < arguments.length; i++) {
      if (arguments[i] instanceof JsonObject) {
        raw.add(Boolean.FALSE);
        callArguments[i] = JsonUtil.stringify(((JsonObject) arguments[i]));
      } else {
        raw.add(Boolean.TRUE);
      }
    }

    // view is the (first) children of <body> that has a $server
    script.append("var callback = arguments[3];");
    script.append("var view = [].slice.call(document.body.children)"); // V14
    script.append("   .concat([].slice.call(document.querySelectorAll('body > #outlet > * > *')))"); // V22+
    script.append("   .find(e=>e.$server);");

    script.append(
        "if (!view) return callback({message:'Could not find view. Check that the view contains @ClientCallable methods'}), 0;");

    script.append("var callable = view.$server[arguments[0]];");
    script.append(
        "if (!callable) return callback({message:'Method is not published. Check that the method exists and it is annotated with @ClientCallable'}), 0;");

    script.append("var raw = arguments[2];");
    script.append("arguments[1] = arguments[1].map((arg,i)=>raw[i]?arg:JSON.parse(arg));");
    script.append("callable.call(view.$server, ...arguments[1])");
    script.append(" .then(result=>callback({result}))");
    script.append(" .catch(e=>callback({message : e.message || ''}));");

    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>) ((JavascriptExecutor) rpc.getDriver())
            .executeAsyncScript(script.toString(), callable, callArguments, raw);

    if (!result.containsKey("result")) {
      throw new RpcCallException((String) result.get("message"));
    }

    return result.get("result");
  }


  abstract Object dispatch(Method method, Object[] args) throws Exception;

  abstract Object convertResult(Object result, Method method, Class<?> returnType) throws Exception;

  @Override
  public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Object result = dispatch(method, args);

      Class<?> returnType = method.getReturnType();

      if (returnType != Void.TYPE && returnType.isPrimitive()) {
        if (result == null) {
          throw new ClassCastException("Cannot cast null as " + returnType);
        }
        returnType = ClassUtils.primitiveToWrapper(method.getReturnType());
      }

      return convertResult(result, method, returnType);
    } catch (RpcException e) {
      throw e;
    } catch (RpcCallException e) {
      if (e.getCause() != null) {
        throw new RpcException(method.getName(), args, e.getMessage(), e.getCause());
      } else {
        throw new RpcException(method.getName(), args, e.getMessage());
      }
    } catch (Exception e) {
      throw new RpcException(method.getName(), args, e);
    }
  }

}


final class HasRpcSupport$SimpleInvocationHandler extends HasRpcSupport$InvocationHandler {

  public HasRpcSupport$SimpleInvocationHandler(HasRpcSupport rpc) {
    super(rpc);
  }

  @Override
  Object dispatch(Method method, Object[] args) throws RpcCallException {
    return call(method.getName(), args);
  }

  @Override
  Object convertResult(Object result, Method method, Class<?> returnType)
      throws IOException, ClassCastException {
    if (returnType == JsonArrayList.class) {
      return TypeConversion.castList((List<?>) result, method.getGenericReturnType());
    }
    if (returnType == Void.TYPE) {
      return null;
    }
    return TypeConversion.cast(result, returnType);
  }

}


class HasRpcSupport$RmiInvocationHandler extends HasRpcSupport$InvocationHandler {

  private final Class<?>[] interfaces;
  private final String instanceId;

  public HasRpcSupport$RmiInvocationHandler(HasRpcSupport rpc, Class<?>[] interfaces,
      String instanceId) {
    super(rpc);
    this.interfaces = interfaces;
    this.instanceId = instanceId;
  }

  @Override
  Object dispatch(Method method, Object[] args) throws IOException, RpcCallException {

    if (method.getDeclaringClass() == RmiStub.class) {
      if (method.getName().equals("$getId")) {
        return instanceId;
      }
    }

    if (interfaces.length > 1 && method.getName().equals("toString")
        && (args == null || args.length == 0)) {
      return Stream.of(interfaces).filter(c -> c != RmiStub.class).map(Class::getSimpleName)
          .collect(Collectors.joining("&"));
    }

    JsonObject invocation = Json.createObject();

    String arguments = null;
    if (args != null && args.length > 0) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (ObjectOutputStream oos = new ObjectOutputStream(baos) {
        {
          enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {
          if (obj instanceof RmiStub) {
            return new RmiStubReplacement(((RmiStub) obj).$getId());
          }
          return obj;
        }
      }) {
        oos.writeObject(args);
      }
      arguments = Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    Class<?>[] parameterTypes = method.getParameterTypes();
    JsonArray signature = Json.createArray();
    for (int i = 0; i < parameterTypes.length; i++) {
      signature.set(i, Json.create(parameterTypes[i].getName()));
    }

    if (instanceId != null) {
      invocation.put(RmiConstants.RMI_INSTANCE_ID, instanceId);
      invocation.put(RmiConstants.RMI_CLASS_NAME, method.getDeclaringClass().getName());
    }

    invocation.put(RmiConstants.RMI_METHOD_NAME, method.getName());
    invocation.put(RmiConstants.RMI_METHOD_SIGNATURE, signature);
    if (arguments != null) {
      invocation.put(RmiConstants.RMI_METHOD_ARGUMENTS, arguments);
    }

    return call(RmiCallable.RMI_CALL_METHOD, invocation);
  }

  @Override
  Object convertResult(Object result, Method method, Class<?> returnType)
      throws IOException, ClassCastException, ClassNotFoundException, RpcCallException {
    if (result == null && returnType == Void.TYPE) {
      return null;
    }

    if (result instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> map = (Map<String, Object>) result;
      if (map.containsKey(RmiConstants.RMI_RESPONSE_MARKER)
          && map.get(RmiConstants.RMI_RESPONSE_MARKER).equals(RmiCallable.class.getName())) {

        if (map.containsKey(RmiConstants.RMI_RESPONSE_ERROR)) {
          RmiError err = RmiError.valueOf((String) map.get(RmiConstants.RMI_RESPONSE_ERROR));
          if (err.hasException()) {
            throw new RpcCallException(err.name(), (Throwable) unmarshal(map));
          } else {
            throw new RpcCallException(err.name());
          }
        } else {
          return unmarshal(map);
        }
      }
    }

    if (returnType == JsonArrayList.class) {
      return TypeConversion.castList((List<?>) result, method.getGenericReturnType());
    }

    return TypeConversion.cast(result, returnType);
  }

  private Object unmarshal(Map<String, Object> res) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
        Base64.getDecoder().decode((String) res.get(RmiConstants.RMI_RESPONSE_DATA)))) {
      {
        enableResolveObject(true);
      }

      @Override
      protected Object resolveObject(Object obj) throws IOException {
        if (obj instanceof RmiRemoteReplacement) {
          return ((RmiRemoteReplacement) obj).createStub(rpc);
        }
        return obj;
      }
    }) {
      return ois.readObject();
    }
  }

}
