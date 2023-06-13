# RPC for Vaadin TestBench

This library provides support for [calling Server-Side methods](https://vaadin.com/docs/latest/create-ui/element-api/client-server-rpc/#calling-server-side-methods-from-the-client) from [Vaadin TestBench](https://vaadin.com/docs/latest/testing/end-to-end) integration tests.

The main purpose of this library is to facilitate using Vaadin TestBench for testing addons. 
The structure of tests written for addons differs from the structure of integration tests written for applications (where there is a clear sequence of actions that the user follows, and the result is observable in the UI).
In the case of addons, the test usually requires to configure the component in some way (by calling server-side setters), then execute an action in the browser, and finally assert that the action has modified the server-side state.
In addition, the views used for testing addons are not part of the addon itself (while applications are usually tested by using the same views that comprise the application), thus developers have more leeway in the implementation.

## Supported versions

Version 1.x of this library is compatible with Vaadin 14-24.


## Maven dependency

Add the following dependency in your pom.xml file:

```xml
<dependency>
   <groupId>com.flowingcode.vaadin.test</groupId>
   <artifactId>testbench-rpc</artifactId>
   <version>X.Y.Z</version>
   <scope>test</scope>
</dependency>
```
<!-- the above dependency should be updated with latest released version information -->

```xml
<repository>
   <id>flowing-code-releases</id>
   <url>https://maven.flowingcode.com/releases</url>
</repository>
```

For SNAPSHOT versions see [here](https://maven.flowingcode.com/snapshots/).

## Building and running demo

- git clone repository
- mvn -Pit -Pdemo -Pproduction verify

Observe that the Vaadin application starts a sample integration tests runs (the application opens in http://localhost:8080/ but there is nothing to see there).

This library is open source. However, you need a [Commercial Vaadin Developer License version 4](https://github.com/vaadin/testbench/blob/master/LICENSE.txt) for Vaadin TestBench.

## Release notes

See [here](https://github.com/FlowingCode/testbench-rpc/releases)

## Issue tracking

The issues for this add-on are tracked on its github.com page. All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. 

As first step, please refer to our [Development Conventions](https://github.com/FlowingCode/DevelopmentConventions) page to find information about Conventional Commits & Code Style requeriments.

Then, follow these steps for creating a contibution:

- Fork this project.
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- For commit message, use [Conventional Commits](https://github.com/FlowingCode/DevelopmentConventions/blob/main/conventional-commits.md) to describe your change.
- Send a pull request for the original project.
- Comment on the original issue that you have implemented a fix for it.

## License & Author

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

RPC for Vaadin TestBench is written by Flowing Code S.A.

# Developer Guide

## Getting started

1. Declare an interface with the client callable methods
```
public interface SampleCallables {
  void showNotification(String message);
}
```

2. Implement that interface in the view class, and annotate the methods with `@ClientCallable`
```
@Route("")
public class SampleView extends Div implements SampleCallables {
  @Override
  @ClientCallable
  public void showNotification(String message) {
    Notification.show(message);
  }
}
```

3. Implement `HasRpcSupport` in the integration test and create a proxy for the callables: `$server = createCallableProxy(SampleCallables.class);`

```
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
```

### Argument and return types

Types `boolean`, `int`, `double`, their boxed types (`Boolean`, `Integer` and `Double`), `String` and `JsonValue` are supported as both argument and return types in `@ClientCallable` methods. 
Enumeration types are supported as arguments, but not as return types.

**Returning Lists** The return type of a `@ClientCallable` method can be declared as `JsonArrayList<T>` (where the element type `T` is a supported return type, or `Long`).
In TestBench side, the `JsonArrayList` will implement `Collection<T>`, which facilitates asserting its value (for instance, with hamcrest matchers).

```
  @Override
  @ClientCallable
  public JsonArrayList<Integer> getIntegers() {
    return JsonArrayList.fromIntegers(Arrays.asList(1, 2));
  }
```

```
  @Test
  public void testListInteger() {
    List<Integer> list = $server.getIntegers().asList();
    assertThat(list, Matchers.hasSize(2));
    assertThat(list.get(0), Matchers.equalTo(1));
    assertThat(list.get(1), Matchers.equalTo(2));
  }
```
