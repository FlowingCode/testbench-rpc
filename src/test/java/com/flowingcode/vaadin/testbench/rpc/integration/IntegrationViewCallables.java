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

import com.flowingcode.vaadin.testbench.rpc.JsonArrayList;
import elemental.json.JsonArray;
import elemental.json.JsonBoolean;
import elemental.json.JsonNull;
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
import elemental.json.JsonValue;

public interface IntegrationViewCallables {

  void testCallableFailure();

  void testCallableSuccess();

  boolean negate(boolean arg);

  String concatWorld(String arg);

  boolean returnTrue();

  int return42IntegerPrimitive();

  double return42DoublePrimitive();

  Integer return42Integer();

  Double return42Double();

  String returnHelloWorld();

  boolean testFooEnum(TestEnum e);

  JsonValue returnJsonValueBoolean(boolean arg);

  JsonValue returnJsonValueInt(int arg);

  JsonValue returnJsonValueDouble(double arg);

  JsonValue returnJsonValueString(String arg);

  JsonValue returnJsonValueNull();

  JsonValue returnJsonValueBooleanArray(boolean arg1, boolean arg2);

  JsonValue returnJsonValueIntArray(int arg1, int arg2);

  JsonValue returnJsonValueDoubleArray(double arg1, double arg2);

  JsonValue returnJsonValueStringArray(String arg1, String arg2);

  JsonValue returnJsonValueNullArray();

  JsonBoolean returnJsonBoolean(boolean arg);

  JsonNumber returnJsonNumber(double arg);

  JsonString returnJsonString(String arg);

  JsonNull returnJsonNull();

  JsonArray returnJsonArray();

  String testJsonValue(JsonValue value);

  enum TestEnum {
    FOO, BAR
  }


  JsonArrayList<Double> getDoubles();

  JsonArrayList<Boolean> getBooleans();

  JsonArrayList<String> getStrings();

  JsonArrayList<Integer> getIntegers();

  JsonArrayList<Long> getLongs();

  JsonValue returnJsonValueJsonObject(String key, String value);

  JsonObject returnJsonObject(String key, String value);

  JsonValue readJsonObject(JsonObject obj, String key);

  boolean isPrototypeOf(String string);

}
