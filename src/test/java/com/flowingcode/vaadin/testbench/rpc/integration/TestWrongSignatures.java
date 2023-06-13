package com.flowingcode.vaadin.testbench.rpc.integration;

import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.flowingcode.vaadin.testbench.rpc.IllegalRpcSignatureException;
import java.util.List;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

// Test several callable interfaces with unsupported parameter or return types
public class TestWrongSignatures implements HasRpcSupport {

  interface WrongSignature_PrimitiveLongArgument {
    void foo(long a);
  }

  interface WrongSignature_ObjectLongArgument {
    void foo(Long a);
  }

  interface WrongSignature_ReturnPrimitiveLongArgument {
    Long foo();
  }

  interface WrongSignature_ReturnObjectLongArgument {
    Long foo();
  }

  interface WrongSignature_ReturnRawList {
    @SuppressWarnings("rawtypes")
    List foo();
  }

  interface WrongSignature_ReturnUnsupportedList {
    List<Object> foo();
  }

  interface WrongSignature_ListArgument {
    void foo(List<Integer> arg);
  }

  enum MyEnum {
    A, B, C
  }

  interface WrongSignature_ReturnEnum {
    MyEnum foo();
  }

  @Test
  public void testIntegrationViewCallables() {
    createCallableProxy(IntegrationViewCallables.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testPrimitiveLongArgument() {
    createCallableProxy(WrongSignature_PrimitiveLongArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testObjectLongArgument() {
    createCallableProxy(WrongSignature_ObjectLongArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testReturnPrimitiveLongArgument() {
    createCallableProxy(WrongSignature_ReturnPrimitiveLongArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testReturnObjectLongArgument() {
    createCallableProxy(WrongSignature_ReturnObjectLongArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testReturnRawList() {
    createCallableProxy(WrongSignature_ReturnRawList.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testReturnUnsupportedList() {
    createCallableProxy(WrongSignature_ReturnUnsupportedList.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testReturnEnum() {
    createCallableProxy(WrongSignature_ReturnEnum.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testListArgument() {
    createCallableProxy(WrongSignature_ListArgument.class);
  }

  @Override
  public WebDriver getDriver() {
    throw new UnsupportedOperationException();
  }

}
