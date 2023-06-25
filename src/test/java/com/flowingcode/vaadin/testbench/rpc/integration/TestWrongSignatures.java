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
package com.flowingcode.vaadin.testbench.rpc.integration;

import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import com.flowingcode.vaadin.testbench.rpc.IllegalRpcSignatureException;
import com.flowingcode.vaadin.testbench.rpc.RmiRemote;
import com.flowingcode.vaadin.testbench.rpc.RmiCallable;
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

  interface WrongSignature_RmiReturnRemote {
    RmiRemote foo();
  }

  interface WrongSignature_RmiRemoteArgument {
    void foo(RmiRemote arg);
  }

  interface GoodSignature_RmiReturnRemote extends RmiCallable {
    RmiRemote foo();
  }

  interface GoodSignature_RmiRemoteArgument extends RmiCallable {
    void foo(RmiRemote arg);
  }

  interface WrongSignature_RmiObjectArgument extends RmiCallable {
    void foo(Object arg);
  }

  interface WrongSignature_RmiReturnObject extends RmiCallable {
    Object foo();
  }

  static class RemoteImpl implements RmiRemote {
  }

  interface WrongSignature_RmiReturnRemoteImpl extends RmiCallable {
    RemoteImpl foo();
  }

  interface WrongSignature_RmiRemoteImplArgument extends RmiCallable {
    void foo(RemoteImpl impl);
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

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiReturnRemote() {
    createCallableProxy(WrongSignature_RmiReturnRemote.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiRemoteArgument() {
    createCallableProxy(WrongSignature_RmiRemoteArgument.class);
  }

  @Test
  public void testReturnRmiRemote_OK() {
    createCallableProxy(GoodSignature_RmiReturnRemote.class);
  }

  @Test
  public void testRmiRemoteArgument_OK() {
    createCallableProxy(GoodSignature_RmiRemoteArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiObjectArgument() {
    createCallableProxy(WrongSignature_RmiObjectArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiReturnObject() {
    createCallableProxy(WrongSignature_RmiReturnObject.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiRemoteImplArgument() {
    createCallableProxy(WrongSignature_RmiRemoteImplArgument.class);
  }

  @Test(expected = IllegalRpcSignatureException.class)
  public void testRmiReturnRemoteImpl() {
    createCallableProxy(WrongSignature_RmiReturnRemoteImpl.class);
  }

  @Override
  public WebDriver getDriver() {
    throw new UnsupportedOperationException();
  }

}
