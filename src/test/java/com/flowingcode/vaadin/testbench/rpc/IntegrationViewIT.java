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

import static com.flowingcode.vaadin.testbench.rpc.IntegrationViewConstants.HELLO;
import static com.flowingcode.vaadin.testbench.rpc.IntegrationViewConstants.HELLO_WORLD;
import static com.flowingcode.vaadin.testbench.rpc.IntegrationViewConstants.WORLD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
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
  public void test03_ReturnBoolean() {
    assertThat($server.returnTrue(), Matchers.equalTo(true));
  }

  @Test
  public void test04_ReturnInt() {
    assertThat($server.return42(), Matchers.equalTo(42));
  }

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
  public void test08_CallableArray() {
    List<String> list = $server.returnHelloAndWorld().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(HELLO));
    assertThat(list.get(1), Matchers.equalTo(WORLD));
  }

}
