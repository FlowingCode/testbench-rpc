package com.flowingcode.vaadin.testbench.rpc.sample;

public interface SideChannel<T> {

  T on(String route);

}
