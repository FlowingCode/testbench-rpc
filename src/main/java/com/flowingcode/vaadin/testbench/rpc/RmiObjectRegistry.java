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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Synchronized;

/**
 * Represents a registry for objects that implement the {@link RmiRemote} interface and allows the
 * lookup of these objects by their assigned ids.
 * 
 * @author Javier Godoy / Flowing Code
 */
class RmiObjectRegistry {

  /** Maps identifiers to their corresponding RmiRemote instances. */
  private final Map<String, RmiRemote> idToInstances = new HashMap<>();

  /** Maps RmiRemote instances to their identifiers. */
  private final Map<RmiRemote, String> instancesToId = new HashMap<>();

  private RmiObjectRegistry() {}

  /**
   * Registers an object that implements {@code RmiRemote} and returns its assigned id. If the
   * object is already registered, this method does not perform any action and the identifier
   * assigned to the already registered object is returned.
   *
   * @param obj the object to register
   * @return the registered object identifier
   */
  final String register(RmiRemote obj) {
    return instancesToId.computeIfAbsent(obj, __ -> {
      String instanceId = UUID.randomUUID().toString();
      idToInstances.put(instanceId, obj);
      return instanceId;
    });
  }

  /**
   * Returns the object with the given id.
   *
   * @param id the identifier assigned to a registered object
   * @return the registered object
   * @throws RpcException if an object with the given id is not registered
   */
  @Synchronized
  final RmiRemote lookup(String id) {
    return Optional.ofNullable(idToInstances.get(id))
        .orElseThrow(() -> new RpcException("No remote object with id " + id));
  }

  /**
   * Returns an instance of the RmiObjectRegistry class for the given Vaadin component. If no
   * {@code RmiObjectRegistry} is associated with the component, a new registry instance will be
   * created.
   *
   * @param c the GUI component associated with the registry instance
   * @return the associated RmiObjectRegistry instance
   */
  static RmiObjectRegistry getInstance(Component c) {
    RmiObjectRegistry registry = ComponentUtil.getData(c, RmiObjectRegistry.class);
    if (registry == null) {
      registry = new RmiObjectRegistry();
      ComponentUtil.setData(c, RmiObjectRegistry.class, registry);
    }
    return registry;
  }

}
