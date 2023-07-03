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

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@SuppressWarnings("serial")
@Route(IntegrationViewRmi.ROUTE)
public class IntegrationViewRmi extends IntegrationView implements IntegrationViewRmiCallables {

  public static final String ROUTE = "it/rmi2";

  public IntegrationViewRmi() {
    setId("view");
  }

  @Override
  @ClientCallable
  public JsonValue $call(JsonObject invocation) {
    return IntegrationViewRmiCallables.super.$call(invocation);
  }

}
