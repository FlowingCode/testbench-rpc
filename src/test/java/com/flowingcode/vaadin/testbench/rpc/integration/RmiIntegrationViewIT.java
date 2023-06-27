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

import static org.junit.Assert.assertThrows;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.flowingcode.vaadin.testbench.rpc.RpcException;
import com.flowingcode.vaadin.testbench.rpc.integration.RmiIntegrationViewCallables.ICounter;
import com.flowingcode.vaadin.testbench.rpc.integration.RmiIntegrationViewCallables.Identity;
import com.flowingcode.vaadin.testbench.rpc.integration.RmiIntegrationViewCallables.MyRemoteObject;
import elemental.json.JsonObject;
import java.io.Serializable;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RmiIntegrationViewIT extends AbstractViewTest implements HasRpcSupport {

  public RmiIntegrationViewIT() {
    super(RmiIntegrationView.ROUTE);
  }

  RmiIntegrationViewCallables $server = createCallableProxy(RmiIntegrationViewCallables.class);

  @Test
  public void test01_CallableSuccess() {
    // test that the RMI callable mechanism works
    $server.testCallableSuccess();
  }

  @Test
  public void test02_CallableFailure() {
    // test that the RMI callable mechanism detect failures
    assertThrows(RpcException.class, () -> $server.testCallableFailure());
  }

  @Test
  public void test02_ReturnComponentFailure() {
    // test that the RMI callable mechanism detect failures when attempting to return a Component
    assertThrows(RpcException.class, () -> $server.returnComponent());
  }

  @Test
  public void test03_CreateRemote() {
    // test returning a remote object
    String name = "test03_CreateRemote";
    MyRemoteObject remote = $server.createRemote(name);
    Assert.assertNotNull(remote);
  }

  @Test
  public void test04_RemoteArgument() {
    // test passing a remote object as argument
    String name = "test04_RemoteArgument";
    MyRemoteObject remote = $server.createRemote(name);
    Assert.assertEquals(name, $server.remoteArgument(remote));
  }

  @Test
  public void test05_RemoteMethod() {
    // test calling a method upon a remote object
    String name = "test05_RemoteMethod";
    MyRemoteObject remote = $server.createRemote(name);
    Assert.assertEquals(name, remote.getName());
  }

  @Test
  public void test06_equality() {
    // test two stubs for the same object
    String name = "test06_identity";
    ICounter remote1 = $server.getCounter(name);
    ICounter remote2 = $server.getCounter(name);
    Assert.assertTrue(remote1.hashCode() == remote2.hashCode());
    Assert.assertTrue(remote1.equals(remote2));

    remote1.setCount(42);
    Assert.assertEquals(42, remote2.getCount());
  }

  @Test
  public void test06_identity() {
    // test two stubs for two server-side equal remote objects
    String name = "test13_equality";
    Identity obj1 = $server.createIdentity(name);
    Identity obj2 = $server.createIdentity(name);
    Assert.assertTrue(obj1.hashCode() == obj2.hashCode());
    Assert.assertTrue(obj2.equals(obj2));

    String foo = "foo";
    obj1.setValue(foo);
    Assert.assertEquals(foo, obj2.getValue());
  }

  @Test
  public void test07_RemoteObject() {
    // test operations on a remote object
    String name = "test07_RemoteObject";
    ICounter counter = $server.getCounter(name);
    counter.setCount(42);
    Assert.assertEquals(42, counter.getCount());
    Assert.assertEquals(42, $server.getCounter(name).getCount());
  }

  @Test
  public void test08_toString() {
    // test that toString return a sane value (the object implements a single interface)
    String name = "test08_toString";
    MyRemoteObject remote = $server.createRemote(name);
    Assert.assertEquals(MyRemoteObject.class.getSimpleName(), remote.toString());
  }

  @Test
  public void test08_toString2() {
    // test that toString return a sane value (the object implements two interfaces)
    String name = "test08_toString2";
    ICounter remote = $server.getCounter(name);
    Assert.assertTrue("ICounter&MyRemoteObject".equals(remote.toString())
        || "MyRemoteObject&ICounter".equals(remote.toString()));
  }

  @Test
  public void test09_interfaces() {
    // test that the returned object implements all its remote interfaces
    String name = "test09_interfaces";
    ICounter counter = $server.getCounter(name);
    Assert.assertTrue(counter instanceof MyRemoteObject);
  }

  @Test
  public void test10_testLong() {
    // test passing and returning a primitive long
    Assert.assertEquals(42L, $server.testLong(42));
  }

  @Test
  public void test10_testJsonObject() {
    // test passing and returning a JsonObject
    String k = "key", v = "hello";
    JsonObject obj = $server.returnJsonObject(k, v);
    Assert.assertEquals(v, obj.getString(k));
  }

  @Test
  public void test11_testSerializable() {
    // test passing and returning a serializable object
    Assert.assertEquals(Pair.of(1, 2), $server.test(Pair.of(1, 2)));
  }

  @Test
  public void test12_testSameSerializable() {
    // assert that both serializable arguments are mapped to the same object
    Serializable obj = Pair.of(1, 2);
    Assert.assertTrue($server.same(obj, obj));
  }

  @Test
  public void test12_testSameRemote() {
    // assert that both remote are mapped to the same object
    String name = "test12_testSameRemote";
    ICounter remote1 = $server.getCounter(name);
    ICounter remote2 = $server.getCounter(name);
    Assert.assertTrue($server.same(remote1, remote2));
  }

  @Test
  public void test13_wrapped() {
    String name = "test13_wrapped";
    ICounter remote = $server.getCounter(name);
    ICounter wrapped = $server.wrap(remote).getObject();
    remote.setCount(42L);
    Assert.assertEquals(42L, wrapped.getCount());
  }

  @Test
  public void test13_registerWrapped() {
    String name = "test13_registerWrapped";
    ICounter wrapped = $server.createWrappedCounter(name).getObject();
    wrapped.setCount(42L);
    Assert.assertEquals(42L, wrapped.getCount());
  }
}
