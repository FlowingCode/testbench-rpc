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

public interface IntegrationViewCallables {

  void testCallableFailure();

  void testCallableSuccess();

  boolean negate(boolean arg);

  String concatWorld(String arg);

  boolean returnTrue();

  int return42();

  String returnHelloWorld();

  boolean testFooEnum(TestEnum e);

  enum TestEnum {
    FOO, BAR
  }


  JsonArrayList<Double> getDoubles();

  JsonArrayList<Boolean> getBooleans();

  JsonArrayList<String> getStrings();

  JsonArrayList<Integer> getIntegers();

  JsonArrayList<Long> getLongs();

}
