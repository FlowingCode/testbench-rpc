# RPC for Vaadin TestBench

This library provides support for [calling Server-Side methods](https://vaadin.com/docs/latest/create-ui/element-api/client-server-rpc/#calling-server-side-methods-from-the-client) from [Vaadin TestBench](https://vaadin.com/docs/latest/testing/end-to-end) integration tests.

The main purpose of this library is to facilitate using Vaadin TestBench for testing add-ons. 
The structure of tests written for add-ons differs from the structure of integration tests written for applications (where there is a clear sequence of actions that the user follows, and the result is observable in the UI).
In the case of add-ons, the test usually requires to configure the component in some way (by calling server-side setters), then execute an action in the browser, and finally assert that the action has modified the server-side state.
In addition, the views used for testing add-ons are not part of the add-on itself (while applications are usually tested by using the same views that comprise the application), thus developers have more leeway in the implementation.

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

Then, follow these steps for creating a contribution:

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

Types `boolean`, `int`, `double`, their boxed types (`Boolean`, `Integer` and `Double`), `String` and `JsonValue` are supported as both argument and return types in `@ClientCallable` methods. Enumeration types are supported as arguments, but not as return types.

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


## Remote Method Invocation

RPC for Vaadin TestBench provides an enhanced RMI-style mechanism that allows the test code to transparently invoke methods on a server-side object, as if the object were local. 
The mechanism also enables the test environment to manipulate the server-side instances. This means that the test scripts can change the state of the server-side components, modify their properties, or simulate different scenarios to test the behavior of the application. (Note: this mechanism follows RMI semantics, but it's not an implementation of Java RMI.)

If the Vaadin view and callable interface have support for RMI-style invocations, then any interface extending `RmiRemote`, as well as serializable classes and interfaces can be used as formal parameters or return type. It's an error if the formal parameter or return type is a serializable class that implements `RmiRemote`. 
 
Remote objects are automatically exported upon return or reference, eliminating the need for "binding" them with a RMI registry. Once a remote object is returned to the test code, it can be seamlessly passed as parameters for further remote method invocations, and calling a method on the remote object will dispatch a remote invocation in the server side. During a remote method call, stubs representing the remote objects are transmitted in place of the actual objects. In the test code, stubs for the same remote object will be equals, but they will not necessarily be the same instance. In the application code, stubs will be resolved to the same original remote instance.

On the other hand, when non-remote objects are passed or returned they are copied through Java serialization, thus changes made to the copied object do not affect the original object. 
However, referential integrity is guaranteed within a single remote method call (multiple references to the same object within arguments of a single remote method call will still refer to the same instance). When passing or returning values by copy, it's a runtime error if the actual value is a `Component` or an objects that references `Component`.

Instances of remote objects share the same lifecycle as the view, and they are garbage collected when the view is collected.
Both the application and the integration tests must use the same classes and the same version of RPC for Vaadin TestBench (there is no protocol negotiation or dynamic classloading).

## Getting started with RMI

In order to support RMI-style invocations:

1. Make the callable interface extend `RmiCallable`
```
public interface SampleCallables extends RmiCallable {
  // ...
}
```

2. Override the `$call` method in the view class and annotate it with `@ClientCallable` (this is a workaround for https://github.com/vaadin/flow/issues/17098):

```
@Route("")
public class SampleView extends Div implements SampleCallables {
  @Override
  @ClientCallable
  public JsonValue $call(JsonObject invocation) {
    return SampleCallables.super.$call(invocation);
  }
}
```

3. Interfaces extending `RmiRemote` can be used as argument or return types in the callable interface:

```
  interface MyRemoteObject extends RmiRemote { 
    String getName(); 
  }
  
  public interface SampleCallables extends RmiCallable {
    MyRemoteObject createRemote(String name);
    void receiveRemote(MyRemoteObject remote);
  }
```

4. In the view class, implement the additional methods (without `@ClientCallable`). Note that remote objects are automatically registered with RMI:

```
  @Override
  public MyRemoteObject createRemote(String name) {
    return new MyRemoteObjectImpl(name);
  }

  @Override
  public void receiveRemote(MyRemoteObject remote) {
    // ...
  }
```  
 
5. In the test class, methods called on remote objects will execute in the server.

```
  @Test
  public void testRemoteObject() {
    MyRemoteObject remote = $server.createRemote("foo");
    $server.receiveRemote(remote);
    assertEquals("foo", remote.getName());
  }
```  

## Side-channel Invocation

Side-channel invocation allows invoking methods (either by using RMI or client-callable RPC) from another view. 
This feature is useful in cases where you want to expose methods from reusable views.

In order to support side-channel invocations:

1. Initialize the proxy by providing a URL. The url will be opened in a second tab, and calls to server methods will be dispatched from that tab. TestbenchRPC takes care of switching back to the original tab when the call completes. 
```
OtherCallables $server = createCallableProxy(OtherCallables.class, getURL("other"));
```

2. Optionally, implement `SideChannelSupport` in the callable interface. This interface adds a `closeSideChannel()` method to the proxy, which allows closing the side tab.
```
public interface OtherCallables extends SideChannelSupport { ... }
```

When using RMI, remember that stubs are UI-scoped. Closing the side channel will invalidate all of its stubs.