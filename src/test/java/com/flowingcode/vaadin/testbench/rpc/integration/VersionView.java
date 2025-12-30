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

import com.flowingcode.vaadin.jsonmigration.InstrumentedRoute;
import com.flowingcode.vaadin.jsonmigration.LegacyClientCallable;
import com.flowingcode.vaadin.testbench.rpc.Version;
import com.vaadin.flow.component.html.Div;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

@SuppressWarnings("serial")
@InstrumentedRoute(VersionView.ROUTE)
public class VersionView extends Div implements VersionViewCallables {

  public static final String ROUTE = "it/version";

  @Override
  @LegacyClientCallable
  public JsonValue $call(JsonObject invocation) {
    return VersionViewCallables.super.$call(invocation);
  }

  @Override
  public Version getVersion() {
    return new Version();
  }

  @Override
  public String getFullVersion() {
    return com.vaadin.flow.server.Version.getFullVersion();
  }

  @Override
  public int getMajorVersion() {
    return com.vaadin.flow.server.Version.getMajorVersion();
  }

  @Override
  public int getMinorVersion() {
    return com.vaadin.flow.server.Version.getMinorVersion();
  }

  @Override
  public int getRevision() {
    return com.vaadin.flow.server.Version.getRevision();
  }

}
