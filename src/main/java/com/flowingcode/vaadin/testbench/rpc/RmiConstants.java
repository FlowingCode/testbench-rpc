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

import lombok.experimental.UtilityClass;

/**
 * Constant values related to RMI for Vaadin TestBench.
 *
 * @author Javier Godoy / Flowing Code
 */
@UtilityClass
class RmiConstants {

  /** ID of the remote object. */
  static final String RMI_INSTANCE_ID = "instanceId";

  static final String RMI_CLASS_NAME = "className";

  /** Name of the invoked method. */
  static final String RMI_METHOD_NAME = "methodName";

  /** Signature of the invoked method. */
  static final String RMI_METHOD_SIGNATURE = "methodSignature";

  /** Arguments of the invoked method. */
  static final String RMI_METHOD_ARGUMENTS = "methodArguments";

  /** Marker indicating that a JsonObject is a RMI response. */
  static final String RMI_RESPONSE_MARKER = "marker";

  /** Response data. */
  static final String RMI_RESPONSE_DATA = "data";

  /** Response error code. */
  static final String RMI_RESPONSE_ERROR = "error";

}
