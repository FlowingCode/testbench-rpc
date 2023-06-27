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

import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_CLASS_NAME;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_INSTANCE_ID;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_METHOD_ARGUMENTS;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_METHOD_NAME;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_METHOD_SIGNATURE;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_RESPONSE_DATA;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_RESPONSE_ERROR;
import static com.flowingcode.vaadin.testbench.rpc.RmiConstants.RMI_RESPONSE_MARKER;
import static org.hamcrest.MatcherAssert.assertThat;
import com.vaadin.flow.component.html.Div;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import elemental.json.JsonType;
import elemental.json.JsonValue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("serial")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RmiErrorTest {


  @SuppressWarnings("unused")
  private static class RmiCallableTest extends Div implements RmiCallable {
    public RmiRemote method() {
      return null;
    }

    public void throwException() {
      throw new RuntimeException();
    }

    public void throwError() {
      throw new Error();
    }

    public void methodWithArguments(Serializable args) {

    }

    public Serializable marshalError() {
      return new Serializable() {
        Object notSerializable = new Object() {};
      };
    }
  }

  private Matcher<JsonValue> hasMarker() {
    return new JsonObjectMatcher(RMI_RESPONSE_MARKER, RmiCallable.class.getName());
  }

  private Matcher<JsonValue> hasError(RmiError error) {
    return new JsonObjectMatcher(RMI_RESPONSE_ERROR, error.name()) {

      @Override
      public void describeTo(Description description) {
        super.describeTo(description);
        if (error.hasException()) {
          description.appendText(" with exception data");
        } else {
          description.appendText(" with no exception data");
        }
      }

      @Override
      protected boolean matchesSafely(JsonValue item, Description mismatchDescription) {
        if (super.matchesSafely(item, mismatchDescription)) {
          JsonObject obj = (JsonObject) item;
          if (!error.hasException() && obj.hasKey(RMI_RESPONSE_DATA)) {
            mismatchDescription.appendText(" with exception data");
            return false;
          }
          if (error.hasException() && !obj.hasKey(RMI_RESPONSE_DATA)) {
            mismatchDescription.appendText(" with no exception data");
            return false;
          }
          if (error.hasException()) {
            String data = ((JsonObject) item).getString(RMI_RESPONSE_DATA);
            try {
              ObjectInputStream ois =
                  new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(data)));
              Class<?> clazz = ois.readObject().getClass();
              if (Throwable.class.isAssignableFrom(clazz)) {
                return true;
              } else {
                mismatchDescription.appendText("data is " + clazz.getName());
                return false;
              }
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
          return true;
        } else {
          return false;
        }
      }
    };
  }

  @With
  @AllArgsConstructor
  @NoArgsConstructor
  private static class Request {
    String instanceId;
    String className;
    String methodName;
    String[] methodSignature;
    Object[] methodArguments;
    String rawMethodArguments;

    JsonValue call() {
      JsonObject obj = Json.createObject();
      if (instanceId != null) {
        obj.put(RMI_INSTANCE_ID, instanceId);
      }
      if (className != null) {
        obj.put(RMI_CLASS_NAME, className);
      }

      if (methodName == null) {
        methodName = "method";
      }
      obj.put(RMI_METHOD_NAME, methodName);

      if (methodSignature == null) {
        methodSignature = new String[0];
      }

      JsonArray signatureArray = Json.createArray();
      obj.put(RMI_METHOD_SIGNATURE, signatureArray);
      for (String s : methodSignature) {
        signatureArray.set(signatureArray.length(), s);
      }

      if (rawMethodArguments != null) {
        obj.put(RMI_METHOD_ARGUMENTS, rawMethodArguments);
      } else if (methodArguments != null) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
          oos.writeObject(methodArguments);
        } catch (IOException e) {
          throw new UndeclaredThrowableException(e);
        }
        obj.put(RMI_METHOD_ARGUMENTS, Base64.getEncoder().encodeToString(baos.toByteArray()));
      }


      return new RmiCallableTest() {}.$call(obj);
    }
  }

  private static class JsonObjectMatcher extends TypeSafeDiagnosingMatcher<JsonValue> {

    private final String key;
    private final String value;

    public JsonObjectMatcher(String key, String value) {
      super(JsonObject.class);
      this.key = key;
      this.value = value;
    }

    @Override
    public void describeTo(Description description) {
      if (value == null) {
        description.appendText(String.format("%s is %s", key, JsonType.STRING));
      } else {
        description.appendText(String.format("%s is '%s'", key, value));
      }
    }

    @Override
    protected boolean matchesSafely(JsonValue item, Description mismatchDescription) {
      JsonObject obj = (JsonObject) item;
      if (!obj.hasKey(key)) {
        mismatchDescription.appendText("no " + key);
        return false;
      }
      JsonType type = obj.get(key).getType();
      if (type != JsonType.STRING) {
        mismatchDescription.appendText(key + " is " + type);
        return false;
      }
      if (value != null && !obj.getString(key).equals(value)) {
        mismatchDescription.appendText(String.format("%s is '%s'", key, obj.getString(key)));
        return false;
      }
      return true;
    }
  };

  @Test
  public void test01_ProtocolSuccess() {
    JsonValue response = new Request().call();
    assertThat(response, Matchers.instanceOf(JsonNull.class));
  }

  @Test
  public void test02_ProtocolErrorNoClass() {
    JsonValue response = new Request().withInstanceId("inst").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_PROTOCOL_ERROR));
  }

  @Test
  public void test02_ProtocolErrorNoId() {
    JsonValue response = new Request().withClassName("clazz").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_PROTOCOL_ERROR));
  }

  @Test
  public void test03_ClassNotFound() {
    JsonValue response = new Request().withInstanceId("foo").withClassName("clazz").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_CLASS_NOT_FOUND));
  }

  @Test
  public void test04_NoSuchMethod() {
    JsonValue response = new Request().withMethodName("noSuchMethod").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_NO_SUCH_METHOD));
  }

  @Test
  public void test05_Unmarshall_NotInBase64() {
    JsonValue response = new Request()
        .withMethodName("methodWithArguments")
        .withMethodSignature(new String[] {"java.io.Serializable"})
        .withRawMethodArguments("@").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_UNMARSHAL));
  }

  @Test
  public void test05_Unmarshall_DeserializationError() {
    JsonValue response = new Request().withMethodName("methodWithArguments")
        .withMethodSignature(new String[] {"java.io.Serializable"})
        .withRawMethodArguments(Base64.getEncoder().encodeToString(new byte[0])).call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_UNMARSHAL));
  }

  @Test
  public void test06_Invoke_Exception() {
    JsonValue response = new Request().withMethodName("throwException").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_INVOKE));
  }

  @Test
  public void test06_Invoke_Error() {
    JsonValue response = new Request().withMethodName("throwError").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_INVOKE));
  }

  @Test
  public void test07_ObjectNotExists() {
    JsonValue response = new Request()
        .withInstanceId("foo")
        .withMethodName("toString")
        .withClassName(Object.class.getName())
        .call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_OBJECT_NOT_EXIST));
  }

  @Test
  public void test08_Marshall() {
    JsonValue response = new Request().withMethodName("marshalError").call();
    assertThat(response, hasMarker());
    assertThat(response, hasError(RmiError.E_MARSHAL));
  }

}
