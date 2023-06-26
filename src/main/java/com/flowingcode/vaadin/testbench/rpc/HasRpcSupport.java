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
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import elemental.json.impl.JsonUtil;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** @author Javier Godoy / Flowing Code */
public interface HasRpcSupport extends HasDriver {

  /**
   * Create a TestBench proxy that invokes methods from the interface through a client call.
   */
  default <T> T createCallableProxy(Class<T> intf) {
    return HasRpcSupport$companion.createCallableProxy(this, intf);
  }


  @Deprecated
  default Object call(String callable, Object... arguments) {
    try {
      return HasRpcSupport$InvocationHandler.call(this, callable, arguments);
    } catch (RpcCallException e) {
      throw new RpcException(callable, arguments, e.getMessage());
    }
  }

}


class HasRpcSupport$companion {

  static <T> T createCallableProxy(HasRpcSupport rpc, Class<T> intf) {

    if (!intf.isInterface()) {
      throw new IllegalArgumentException(intf.getName() + " is not an interface");
    }
    for (Method method : intf.getMethods()) {
      if (!Modifier.isStatic(method.getModifiers())) {
        TypeConversion.checkMethod(method);
      }
    }

    InvocationHandler invocationHandler = new HasRpcSupport$SimpleInvocationHandler(rpc);
    return intf.cast(Proxy.newProxyInstance(intf.getClassLoader(), new Class<?>[] {intf}, invocationHandler));
  }

}


@SuppressWarnings("serial")
class RpcCallException extends Exception {
  public RpcCallException(String message) {
    super(message);
  }
}


abstract class HasRpcSupport$InvocationHandler implements InvocationHandler {

  /**
   * Call a {@link ClientCallable} defined on the integration view.
   *
   * @param callable the client callable name
   * @param arguments arguments to be passed to the callable
   * @throws TimeoutException if the callable times out (see {@link
   *     WebDriver.Timeouts#setScriptTimeout(long, java.util.concurrent.TimeUnit)
   *     WebDriver.Timeouts}).
   * @throws RuntimeException if the callable fails.
   */
  static Object call(HasRpcSupport rpc, String callable, Object... arguments)
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
    script.append("debugger;");
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
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      Object result = dispatch(method, args);

      Class<?> returnType = method.getReturnType();

      if (returnType == Void.TYPE) {
        return null;
      }

      if (returnType.isPrimitive()) {
        if (result == null) {
          throw new ClassCastException("Cannot cast null as " + returnType);
        }
        returnType = ClassUtils.primitiveToWrapper(method.getReturnType());
      }

      return convertResult(result, method, returnType);
    } catch (RpcException e) {
      throw e;
    } catch (RpcCallException e) {
      throw new RpcException(method.getName(), args, e.getMessage());
    } catch (Exception e) {
      throw new RpcException(method.getName(), args, e);
    }
  }

}


@RequiredArgsConstructor
final class HasRpcSupport$SimpleInvocationHandler extends HasRpcSupport$InvocationHandler {

  private final HasRpcSupport rpc;

  @Override
  Object dispatch(Method method, Object[] args) throws RpcCallException {
    return call(rpc, method.getName(), args);
  }

  @Override
  Object convertResult(Object result, Method method, Class<?> returnType)
      throws IOException, ClassCastException {
    if (returnType == JsonArrayList.class) {
      return TypeConversion.castList((List<?>) result, method.getGenericReturnType());
    }
    return TypeConversion.cast(result, returnType);
  }

}
