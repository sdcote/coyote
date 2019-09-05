/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.Version;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * Coyote Metric Collector fixture.
 *
 * <p>Constants are placed here along with any static method useful across multiple classes in the project.</p>
 */
public class CMC {
  public static final Version VERSION = new Version(0, 0, 1, Version.EXPERIMENTAL);
  public static final String NAME = "CMC";
  public static final String COUNTER = "counter"; // monotonically increasing value
  public static final String GAUGE = "gauge"; // cane increase or decrease between readings


  public static final BundleBaseName MSG;

  static {
    MSG = new BundleBaseName("CMCMsg");
  }


  /**
   * Called by other classes to get our version number.
   *
   * @return a string represing our version.
   */
  public String getVersion() {
    return VERSION.toString();
  }

}
