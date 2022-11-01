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
import com.flowingcode.vaadin.testbench.rpc.JsonArrayList;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import java.util.Arrays;

@SuppressWarnings("serial")
@Route(IntegrationView.ROUTE)
public class IntegrationView extends Div implements IntegrationViewCallables {

  public static final String ROUTE = "it";

  public IntegrationView() {
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
  public boolean negate(boolean arg) {
    return !arg;
  }

  @Override
  @ClientCallable
  public String concatWorld(String arg) {
    return arg.concat(WORLD);
  }

  @Override
  @ClientCallable
  public boolean returnTrue() {
    return true;
  }

  @Override
  @ClientCallable
  public int return42() {
    return 42;
  }

  @Override
  @ClientCallable
  public String returnHelloWorld() {
    return HELLO_WORLD;
  }

  @Override
  @ClientCallable
  public JsonArrayList<String> returnHelloAndWorld() {
    return JsonArrayList.createArray(Arrays.asList(HELLO, WORLD), Json::create);
  }

}
