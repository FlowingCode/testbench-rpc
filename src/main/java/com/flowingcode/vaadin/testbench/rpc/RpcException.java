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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RpcException extends RuntimeException {

  public RpcException(String method, Object[] arguments, String message) {
    super(makeMessage(method, arguments, message));
  }

  public RpcException(String method, Object[] arguments, Throwable cause) {
    super(makeMessage(method, arguments, cause.getMessage()), cause);
  }

  private static String makeMessage(String method, Object[] arguments, String message) {
    return String.format(
        "%s(%s) RPC call failed%s",
        method,
        Optional.ofNullable(arguments).map(Stream::of).orElse(Stream.empty()).map(Object::toString)
            .collect(Collectors.joining(",")),
        Optional.ofNullable(message).map(": "::concat).orElse(""));
  }

}
