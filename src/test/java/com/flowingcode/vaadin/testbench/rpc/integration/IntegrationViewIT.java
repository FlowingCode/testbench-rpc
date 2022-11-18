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
package com.flowingcode.vaadin.testbench.rpc.integration;

import static com.flowingcode.vaadin.testbench.rpc.integration.IntegrationViewConstants.HELLO;
import static com.flowingcode.vaadin.testbench.rpc.integration.IntegrationViewConstants.HELLO_WORLD;
import static com.flowingcode.vaadin.testbench.rpc.integration.IntegrationViewConstants.WORLD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.flowingcode.vaadin.testbench.rpc.RpcException;
import com.flowingcode.vaadin.testbench.rpc.integration.IntegrationViewCallables.TestEnum;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationViewIT extends AbstractViewTest implements HasRpcSupport {

  public IntegrationViewIT() {
    super(IntegrationView.ROUTE);
  }

  IntegrationViewCallables $server = createCallableProxy(IntegrationViewCallables.class);

  @Test
  public void test01_CallableSuccess() {
    // test that the callable mechanism works
    $server.testCallableSuccess();
  }

  @Test
  public void test02_CallableFailure() {
    // test that the callable mechanism detect failures
    assertThrows(RpcException.class, () -> $server.testCallableFailure());
  }

  @Test
  public void test03_ReturnBooleanPrimitive() {
    assertThat($server.returnTrue(), Matchers.equalTo(true));
  }

  @Test
  public void test04_ReturnIntegerPrimitive() {
    assertThat($server.return42IntegerPrimitive(), Matchers.equalTo(42));
  }

  @Test
  public void test04_ReturnDoublePrimitive() {
    assertThat($server.return42DoublePrimitive(), Matchers.equalTo(42.0));
  }

  @Test
  public void test04_ReturnInteger() {
    assertThat($server.return42Integer(), Matchers.equalTo(42));
  }

  @Test
  public void test04_ReturnDouble() {
    assertThat($server.return42Double(), Matchers.equalTo(42.0));
  }

  @Test
  public void test05_ReturnString() {
    assertThat($server.returnHelloWorld(), Matchers.equalTo(HELLO_WORLD));
  }

  @Test
  public void test06_CallableBoolean() {
    assertThat($server.negate(true), Matchers.equalTo(false));
    assertThat($server.negate(false), Matchers.equalTo(true));
  }

  @Test
  public void test07_CallableString() {
    assertThat($server.concatWorld(""), Matchers.equalTo(WORLD));
    assertThat($server.concatWorld(HELLO), Matchers.equalTo(HELLO_WORLD));
  }

  @Test
  public void test08_CallableEnum() {
    assertTrue($server.testFooEnum(TestEnum.FOO));
  }

  @Test
  public void test09_CallableArrayString() {
    List<String> list = $server.getStrings().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(HELLO));
    assertThat(list.get(1), Matchers.equalTo(WORLD));
  }

  @Test
  public void test09_CallableArrayBoolean() {
    List<Boolean> list = $server.getBooleans().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(false));
    assertThat(list.get(1), Matchers.equalTo(true));
  }

  @Test
  public void test09_CallableArrayDouble() {
    List<Double> list = $server.getDoubles().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(1.1d));
    assertThat(list.get(1), Matchers.equalTo(2.2d));
  }

  @Test
  public void test09_CallableArrayInteger() {
    List<Integer> list = $server.getIntegers().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(1));
    assertThat(list.get(1), Matchers.equalTo(2));
  }

  @Test
  public void test09_CallableArrayLong() {
    List<Long> list = $server.getLongs().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(1L));
    assertThat(list.get(1), Matchers.equalTo(2L));
  }

}
