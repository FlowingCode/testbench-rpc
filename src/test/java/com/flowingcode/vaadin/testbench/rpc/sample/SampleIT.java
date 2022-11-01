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
package com.flowingcode.vaadin.testbench.rpc.sample;

import static org.junit.Assert.assertEquals;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.Test;

public class SampleIT extends AbstractViewTest implements HasRpcSupport {

  private static final String NOTIFICATION_MESSAGE = "RPC succeeded";

  public SampleIT() {
    super("");
  }

  SampleCallables $server = createCallableProxy(SampleCallables.class);

  @Test
  public void testNotification() {
    $server.showNotification(NOTIFICATION_MESSAGE);
    assertEquals(NOTIFICATION_MESSAGE, $(NotificationElement.class).first().getText());
  }

}
