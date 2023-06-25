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

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.ToString;

/**
 * Serialized form of a {@link RmiRemote} object, in server-to-client communication.
 *
 * @author Javier Godoy / Flowing Code
 */
@ToString
final class RmiRemoteReplacement implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @NonNull Class<?>[] interfaces;
  private final @NonNull String instanceId;

  public RmiRemoteReplacement(RmiObjectRegistry registry, String instanceId) {
    Object object = registry.lookup(instanceId);
    this.instanceId = instanceId;
    interfaces = Stream.of(object.getClass().getInterfaces())
        .filter(RmiRemote.class::isAssignableFrom).toArray(Class<?>[]::new);
  }

  Object createStub(@NonNull HasRpcSupport rpc) {
    Class<?>[] interfaces = this.interfaces;
    interfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
    interfaces[interfaces.length - 1] = RmiStub.class;

    return HasRpcSupport$companion.createCallableProxy(rpc, interfaces, instanceId);
  }

}
