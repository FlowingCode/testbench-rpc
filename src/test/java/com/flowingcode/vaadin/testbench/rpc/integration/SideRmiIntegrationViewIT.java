package com.flowingcode.vaadin.testbench.rpc.integration;

import static org.junit.Assert.assertEquals;
import com.flowingcode.vaadin.testbench.rpc.AbstractViewTest;
import com.flowingcode.vaadin.testbench.rpc.HasRpcSupport;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SideRmiIntegrationViewIT extends AbstractViewTest implements HasRpcSupport {

  public SideRmiIntegrationViewIT() {
    super(OtherView.ROUTE);
  }

  RmiIntegrationViewCallables $server =
      createCallableProxy(RmiIntegrationViewCallables.class, getURL(RmiIntegrationView.ROUTE));

  @Test
  public void test01_callable() {
    $server.testCallableSuccess();
    assertEquals(2, getDriver().getWindowHandles().size());
  }

  @Test
  public void test02_reuse() {
    $server.testCallableSuccess();
    $server.testCallableSuccess();
  }

  @Test
  public void test03_close() {
    $server.testCallableSuccess();
    $server.closeSideChannel();
    assertEquals(1, getDriver().getWindowHandles().size());
  }

  @Test
  public void test04_reopen() {
    $server.testCallableSuccess();
    $server.closeSideChannel();
    $server.testCallableSuccess();
    assertEquals(2, getDriver().getWindowHandles().size());
  }

}