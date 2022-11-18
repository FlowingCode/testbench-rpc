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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.flowingcode.vaadin.testbench.rpc.RpcException;
import io.vavr.control.Try;
import org.junit.Test;


public class NoCallables2IT extends AbstractViewTest implements HasRpcSupport {

  public NoCallables2IT() {
    super(NoCallablesView2.ROUTE);
  }

  // View does not implement the required callable
  IntegrationViewCallables $server = createCallableProxy(IntegrationViewCallables.class);

  @Test
  public void testMethodNotFound() {
    String actualMessage[] = new String[1];
    String expectedMessage =
        "testCallableSuccess() RPC call failed: Method is not published. Check that the method exists and it is annotated with @ClientCallable";

    assertThrows(RpcException.class, Try.run($server::testCallableSuccess)
        .onFailure(t -> actualMessage[0] = t.getMessage())::get);
    assertEquals(expectedMessage, actualMessage[0]);
  }

  @Test
  public void testCastVoidAsPrimitive() {
    String actualMessage[] = new String[1];
    String expectedMessage = "return42IntegerPrimitive() RPC call failed: Cannot cast null as int";

    assertThrows(RpcException.class,
        Try.run($server::return42IntegerPrimitive)
        .onFailure(t -> actualMessage[0] = t.getMessage())::get);
    assertEquals(expectedMessage, actualMessage[0]);
  }

  @Test
  public void testCastIntAsString() {
    String actualMessage[] = new String[1];
    String expectedMessage =
        "returnHelloWorld() RPC call failed: Cannot cast java.lang.Long as java.lang.String";

    assertThrows(RpcException.class,
        Try.run($server::returnHelloWorld)
        .onFailure(t -> actualMessage[0] = t.getMessage())::get);
    assertEquals(expectedMessage, actualMessage[0]);
  }
}
