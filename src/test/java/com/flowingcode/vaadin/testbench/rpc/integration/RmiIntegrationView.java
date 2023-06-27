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
package com.flowingcode.vaadin.testbench.rpc.integration;

import com.flowingcode.vaadin.testbench.rpc.RmiRemote;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@SuppressWarnings("serial")
@Route(RmiIntegrationView.ROUTE)
public class RmiIntegrationView extends Div implements RmiIntegrationViewCallables {

  public static final String ROUTE = "it/rmi";

  public RmiIntegrationView() {
    setId("view");
  }

  @Override
  @ClientCallable
  public void testCallableSuccess() {
    // do nothing
  }

  @Override
  @ClientCallable
  public void testCallableFailure() {
    throw new RuntimeException();
  }

  @Override
  @ClientCallable
  public JsonValue $call(JsonObject invocation) {
    return RmiIntegrationViewCallables.super.$call(invocation);
  }

  @RequiredArgsConstructor
  @EqualsAndHashCode
  @Getter
  @Setter
  private final static class IdentityImpl implements Identity {
    final String name;
    String value;
  }

  @Override
  public Identity createIdentity(String name) {
    return new IdentityImpl(name);
  }

  @Getter
  @RequiredArgsConstructor
  private final static class MyRemoteObjectImpl implements MyRemoteObject, AnotherInterface {
    final String name;
  }

  @Override
  public MyRemoteObject createRemote(String name) {
    return new MyRemoteObjectImpl(name);
  }

  @Override
  public String remoteArgument(MyRemoteObject remote) {
    return remote.getName();
  }

  private Map<String, ICounter> counters = new HashMap<>();

  @Getter
  @Setter
  @RequiredArgsConstructor
  private final static class Counter implements ICounter, MyRemoteObject, AnotherInterface {
    long count;
    final String name;
  }

  @Override
  public ICounter getCounter(String name) {
    return counters.computeIfAbsent(name, Counter::new);
  }

  @Override
  public long testLong(long arg) {
    return arg;
  }

  @Override
  public <T extends Serializable> T test(T t) {
    return t;
  }

  @Override
  public boolean same(Serializable a, Serializable b) {
    return a == b;
  }

  @Override
  public boolean same(RmiRemote a, RmiRemote b) {
    return a == b;
  }

  @Override
  public Component returnComponent() {
    return this;
  }

  @Override
  public <T extends RmiRemote> Wrapper<T> wrap(T remote) {
    return new Wrapper<>(remote);
  }

  @Override
  public Wrapper<ICounter> createWrappedCounter(String name) {
    return new Wrapper<>(getCounter(name));
  }

  @Override
  public JsonObject returnJsonObject(String key, String value) {
    JsonObject obj = Json.createObject();
    obj.put(key, value);
    return obj;
  }
}
