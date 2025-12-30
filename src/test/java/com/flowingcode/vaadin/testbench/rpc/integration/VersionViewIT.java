/*-
 * #%L
 * RPC for Vaadin TestBench
 * %%
 * Copyright (C) 2021 - 2025 Flowing Code
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
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VersionViewIT extends AbstractViewTest implements HasRpcSupport {

  public VersionViewIT() {
    super(VersionView.ROUTE);
  }

  VersionViewCallables $server = createCallableProxy(VersionViewCallables.class);

  @Test
  public void test_getFullVersion() {
    assertEquals($server.getFullVersion(), $server.getVersion().getFullVersion());
  }

  @Test
  public void test_getMajorVersion() {
    assertEquals($server.getMajorVersion(), $server.getVersion().getMajorVersion());
  }

  @Test
  public void test_getMinorVersion() {
    assertEquals($server.getMinorVersion(), $server.getVersion().getMinorVersion());
  }

  @Test
  public void test_getRevision() {
    assertEquals($server.getRevision(), $server.getVersion().getRevision());
  }
}
