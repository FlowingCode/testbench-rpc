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
package com.flowingcode.vaadin.testbench.rpc.sample;

import static org.junit.Assert.assertEquals;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import org.junit.Test;

public class OtherIT extends AbstractViewTest implements HasRpcSupport {
  // this test opens "" but calls a method in "other"

  public OtherIT() {
    super("");
  }

  OtherCallables $server = createCallableProxy(OtherCallables.class, getURL("other"));

  @Test
  public void testNotification() {
    assertEquals(OtherView.class.getName(), $server.getClassName());
  }

}
