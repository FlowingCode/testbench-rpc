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

import com.flowingcode.vaadin.testbench.rpc.RmiCallable;
import com.flowingcode.vaadin.testbench.rpc.RmiRemote;
import com.flowingcode.vaadin.testbench.rpc.SideChannelSupport;
import com.vaadin.flow.component.Component;
import elemental.json.JsonObject;
import java.io.Serializable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface RmiIntegrationViewCallables extends RmiCallable, SideChannelSupport {

  void testCallableSuccess();

  void testCallableFailure();

  JsonObject testFailureJsonObject();

  long testLong(long arg);

  interface AnotherInterface {

  }

  interface MyRemoteObject extends RmiRemote {
    String getName();
  }

  interface ICounter extends RmiRemote {
    long getCount();
    void setCount(long count);
  }

  interface Identity extends RmiRemote {
    String getValue();
    void setValue(String value);
  }

  @Getter
  @RequiredArgsConstructor
  class Wrapper<T extends RmiRemote> implements Serializable {
    final T object;
  }

  MyRemoteObject createRemote(String name);

  Identity createIdentity(String name);

  String remoteArgument(MyRemoteObject remote);

  ICounter getCounter(String name);

  <T extends Serializable> T test(T t);

  boolean same(Serializable a, Serializable b);

  boolean same(RmiRemote a, RmiRemote b);

  Component returnComponent();

  <T extends RmiRemote> Wrapper<T> wrap(T remote);

  Wrapper<ICounter> createWrappedCounter(String name);

  JsonObject returnJsonObject(String key, String value);

}
