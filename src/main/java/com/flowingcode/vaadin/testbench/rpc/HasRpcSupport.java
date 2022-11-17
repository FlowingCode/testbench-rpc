/*-
 * #%L
 * RPC for Vaadin TestBench
 * %%
 * Copyright (C) 2021 - 2022 Flowing Code
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** @author Javier Godoy / Flowing Code */
public interface HasRpcSupport extends HasDriver {

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
  default Object call(String callable, Object... arguments) {
    arguments = Optional.ofNullable(arguments).orElse(new Object[0]);
    for (int i = 0; i < arguments.length; i++) {
      if (arguments[i] instanceof Enum) {
        arguments[i] = ((Enum<?>) arguments[i]).name();
      }
    }
    StringBuilder script = new StringBuilder();

    // view is the (first) children of <body> that has a $server
    script.append("var callback = arguments[2];");
    script.append("var view = [].slice.call(document.body.children)"); // V14
    script.append("   .concat([].slice.call(document.querySelectorAll('body > #outlet > * > *')))"); // V22+
    script.append("   .find(e=>e.$server);");

    script.append(
        "if (!view) return callback({message:'Could not find view. Check that the view contains @ClientCallable methods'}), 0;");

    script.append("var callable = view.$server[arguments[0]];");
    script.append(
        "if (!callable) return callback({message:'Method is not published. Check that the method exists and it is annotated with @ClientCallable'}), 0;");
    script.append("callable.call(view.$server, ...arguments[1])");
    script.append(" .then(result=>callback({result}))");
    script.append(" .catch(e=>callback({message : e.message || ''}));");

    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>)
            ((JavascriptExecutor) getDriver())
            .executeAsyncScript(script.toString(), callable, arguments);

    if (!result.containsKey("result")) {
      throw new RpcException(callable, arguments, (String) result.get("message"));
    }

    return result.get("result");
  }

  /**
   * Create a TestBench proxy that invokes methods from the interface through a client {@link
   * #call}.
   */
  default <T> T createCallableProxy(Class<T> intf) {
    return intf.cast(
        Proxy.newProxyInstance(
            intf.getClassLoader(),
            new Class<?>[] {intf},
            new InvocationHandler() {
              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                  Object result = call(method.getName(), args);

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

                  if (returnType == JsonArrayList.class) {
                    return castList((List<?>) result, method.getGenericReturnType());
                  }

                  return cast(result, returnType);

                } catch (RpcException e) {
                  throw e;
                } catch (Exception e) {
                  throw new RpcException(method.getName(), args, e);
                }
              }

              private Object cast(Object result, Class<?> returnType) {

                if (result == null) {
                  return null;
                }

                if (returnType.isInstance(result)) {
                  return result;
                }

                if (returnType == Integer.class && result.getClass() == Long.class) {
                  return BigInteger.valueOf((Long) result).intValueExact();
                }

                throw new ClassCastException(String.format("Cannot cast %s as %s",
                    result.getClass().getName(), returnType.getName()));
              }

              private JsonArrayList<?> castList(List<?> result, Type returnType) {
                if (returnType instanceof ParameterizedType) {
                  Type arg = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                  Class<?> elementType = TypeUtils.getRawType(arg, null);
                  result = ((List<?>) result).stream().map(e -> cast(e, elementType))
                      .collect(Collectors.toList());
                }
                return JsonArrayList.wrapForTestbench(result);
              }

            }));
  }
}
