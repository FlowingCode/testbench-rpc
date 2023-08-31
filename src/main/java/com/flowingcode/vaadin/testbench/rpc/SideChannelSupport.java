package com.flowingcode.vaadin.testbench.rpc;

public interface SideChannelSupport {

  default void closeSideChannel() {
    throw new UnsupportedOperationException();
  }

}
