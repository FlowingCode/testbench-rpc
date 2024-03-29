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
import elemental.json.impl.JreJsonArray;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.experimental.Delegate;

/**
 * Server-side flavor of {@code JsonArrayList}
 *
 * @author Javier Godoy / Flowing Code
 */
@SuppressWarnings("serial")
class JreJsonArrayList<T> extends JreJsonArray implements JsonArrayList<T> {

  @Delegate(excludes = JreJsonArray.class)
  private Collection<T> list =
      new AbstractCollection<T>() {
        // the delegate is only for the purpose of implementing Collection,
        // but the Collection interface is unsupported on instances of JreJsonArrayList
        @Override
        public Iterator<T> iterator() {
          throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
          throw new UnsupportedOperationException();
        }
      };

  public JreJsonArrayList(Iterable<T> list, Function<? super T, JsonValue> mapper) {
    super(Json.instance());
    for (T t : list) {
      set(length(), Optional.ofNullable(t).map(mapper).orElseGet(Json::createNull));
    }
  }

  @Override
  public List<T> asList() {
    // JsonArrayList#asList is unsupported
    throw new UnsupportedOperationException();
  }
}
