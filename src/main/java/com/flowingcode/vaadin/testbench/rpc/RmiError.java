package com.flowingcode.vaadin.testbench.rpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Error codes related to RMI for Vaadin TestBench.
 *
 * @author Javier Godoy / Flowing Code
 */
@RequiredArgsConstructor
enum RmiError {

  /** An invalid request was received. */
  E_PROTOCOL_ERROR(true),

  /** The requested target class cannot be located. */
  E_CLASS_NOT_FOUND(false),

  /** The requested class does not implement the requested method. */
  E_NO_SUCH_METHOD(false),

  /** Attempt to pass an unregistered value as an object reference. */
  E_OBJECT_NOT_EXIST(false),

  /**
   * An exception ocurred when executing the remote method.
   * The response data includes the serialized form of the thrown exception.
   */
  E_INVOKE(true),

  /**
   * Response error: an exception ocurred when serializing the response.
   * The response data includes the serialized form of the thrown exception.
   */
  E_MARSHAL(true),

  /**
   * Response error: an exception ocurred when deserializing the request arguments.
   * The response data includes the serialized form of the thrown exception.
   */
  E_UNMARSHAL(true),

  /**
   * Response error: unlisted exception.
   * The response data includes the serialized form of the thrown exception.
   */
  E_UNKNOWN(true);

  @Getter
  @Accessors(fluent = true)
  private final boolean hasException;
}