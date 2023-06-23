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
import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNull;
import elemental.json.JsonNumber;
import elemental.json.JsonString;
import elemental.json.JsonType;
import elemental.json.JsonValue;
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
  public int return42IntegerPrimitive() {
    return 42;
  }

  @Override
  @ClientCallable
  public double return42DoublePrimitive() {
    return 42d;
  }

  @Override
  @ClientCallable
  public Integer return42Integer() {
    return 42;
  }

  @Override
  @ClientCallable
  public Double return42Double() {
    return 42d;
  }

  @Override
  @ClientCallable
  public String returnHelloWorld() {
    return HELLO_WORLD;
  }

  @Override
  @ClientCallable
  public boolean testFooEnum(TestEnum e) {
    return e == TestEnum.FOO;
  }

  @Override
  @ClientCallable
  public JsonArrayList<Double> getDoubles() {
    return JsonArrayList.fromDoubles(Arrays.asList(1.1d, 2.2d));
  }

  @Override
  @ClientCallable
  public JsonArrayList<Boolean> getBooleans() {
    return JsonArrayList.fromBooleans(Arrays.asList(false, true));
  }

  @Override
  @ClientCallable
  public JsonArrayList<String> getStrings() {
    return JsonArrayList.fromStrings(Arrays.asList(HELLO, WORLD));
  }

  @Override
  @ClientCallable
  public JsonArrayList<Integer> getIntegers() {
    return JsonArrayList.fromIntegers(Arrays.asList(1, 2));
  }

  @Override
  @ClientCallable
  public JsonArrayList<Long> getLongs() {
    return JsonArrayList.fromLongs(Arrays.asList(1L, 2L));
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueBoolean(boolean arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueInt(int arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueDouble(double arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueString(String arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueNull() {
    return Json.createNull();
  }

  private JsonArray createArray(JsonValue... elements) {
    JsonArray array = Json.createArray();
    for (int i = 0; i < elements.length; i++) {
      array.set(i, elements[i]);
    }
    return array;
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueBooleanArray(boolean arg1, boolean arg2) {
    return createArray(Json.create(arg1), Json.create(arg2));
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueIntArray(int arg1, int arg2) {
    return createArray(Json.create(arg1), Json.create(arg2));
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueDoubleArray(double arg1, double arg2) {
    return createArray(Json.create(arg1), Json.create(arg2));
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueStringArray(String arg1, String arg2) {
    return createArray(Json.create(arg1), Json.create(arg2));
  }

  @Override
  @ClientCallable
  public JsonValue returnJsonValueNullArray() {
    return createArray(Json.createNull(), Json.createNull());
  }

  @Override
  @ClientCallable
  public String testJsonValue(JsonValue value) {
    if (value != null) {
      return value.getType().name();
    } else {
      // https://github.com/vaadin/flow/issues/17096
      return JsonType.NULL.name();
    }
  }

  @Override
  @ClientCallable
  public JsonBoolean returnJsonBoolean(boolean arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonNumber returnJsonNumber(double arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonString returnJsonString(String arg) {
    return Json.create(arg);
  }

  @Override
  @ClientCallable
  public JsonNull returnJsonNull() {
    return Json.createNull();
  }

  @Override
  @ClientCallable
  public JsonArray returnJsonArray() {
    return Json.createArray();
  }

}
