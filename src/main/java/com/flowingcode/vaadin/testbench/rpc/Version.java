package com.flowingcode.vaadin.testbench.rpc;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * A serializable snapshot of the Vaadin framework version information.
 *
 * @see com.vaadin.flow.server.Version
 */
@Getter
@SuppressWarnings("javadoc")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Version implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Gets the full version, in format {@literal x.y.z} or {@literal x.y.z.qualifier}.
   *
   * @return the full version number
   */
  String fullVersion;

  /**
   * Gets the major version, {@literal y} in {@literal x.y.z}.
   *
   * @return the major version number
   */
  int majorVersion;

  /**
   * Gets the minor version, {@literal y} in {@literal x.y.z}.
   *
   * @return the minor version number
   */
  int minorVersion;

  /**
   * Gets the revision, {@literal z} in {@literal x.y.z}.
   *
   * @return the revision number
   */
  int revision;

  public Version() {
    fullVersion = com.vaadin.flow.server.Version.getFullVersion();
    majorVersion = com.vaadin.flow.server.Version.getMajorVersion();
    minorVersion = com.vaadin.flow.server.Version.getMinorVersion();
    revision = com.vaadin.flow.server.Version.getRevision();
  }


}
