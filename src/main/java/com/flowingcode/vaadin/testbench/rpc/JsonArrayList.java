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
package com.flowingcode.vaadin.testbench.rpc;

import elemental.json.Json;
import elemental.json.JsonValue;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

// Vaadin constraints:
// - the result type of a @ClientCallable methods must be assignable to JsonValue.
// - the runtime type of the result must be instanceof JreJsonValue.
// - JreJsonValue can only be extended through intermediate classes.

// Testbench constraints:
// - Selenium already converts JSON to a List of objects (thus we have a "T" and not a JsonValue).
// - In the integration test, we are interested on the "T" and not the raw JsonValue.
// - It makes sense to implement Collection<T>, in order to facilitate the use of hamcrest Matchers
// - JsonArray cannot implement List it because the return type of get(int) is incompatible.
// - List isn't too helpul, because most Matchers work with Iterable/Collection.
// - JsonValue methods (JS type coercion, etc.) are not needed in integration tests.
/** @author Javier Godoy / Flowing Code */
public interface JsonArrayList<T> extends JsonValue, Collection<T> {

  List<T> asList();

  @Deprecated
  /** @deprecated. Use {@link #fromStrings(Iterable)} */
  public static JsonArrayList<String> fromStringArray(List<String> list) {
    return fromStrings(list);
  }

  @Deprecated
  /** @deprecated. Use {@link #fromBooleans(Iterable)} */
  public static JsonArrayList<Boolean> fromBooleanArray(List<Boolean> list) {
    return fromBooleans(list);
  }

  @Deprecated
  /** @deprecated. Use {@link #fromDoubles(Iterable)} */
  public static JsonArrayList<Double> fromDoubleArray(List<Double> list) {
    return fromDoubles(list);
  }

  public static JsonArrayList<String> fromStrings(Iterable<String> list) {
    return createArray(list, Json::create);
  }

  public static JsonArrayList<Boolean> fromBooleans(Iterable<Boolean> list) {
    return createArray(list, Json::create);
  }

  public static JsonArrayList<Double> fromDoubles(Iterable<Double> list) {
    return createArray(list, Json::create);
  }

  public static JsonArrayList<Integer> fromIntegers(Iterable<Integer> list) {
    return createArray(list, Json::create, Integer::doubleValue);
  }

  public static JsonArrayList<Long> fromLongs(Iterable<Long> list) {
    return createArray(list, Json::create, Long::doubleValue);
  }

  @Deprecated
  /** @deprecated. This method should be private. */
  public static <T> JsonArrayList<T> createArray(
      Iterable<T> list, Function<? super T, JsonValue> mapper) {
    return new JreJsonArrayList<>(list, mapper);
  }

  @Deprecated
  /** @deprecated. This method should be private. */
  public static <T, X> JsonArrayList<T> createArray(Iterable<T> list,
      Function<? super X, JsonValue> mapper, Function<? super T, X> before) {
    return new JreJsonArrayList<>(list, mapper.compose(before));
  }

  public static <T> JsonArrayList<T> wrapForTestbench(List<T> list) {
    return new TestbenchJsonArrayList<>(list);
  }
}
