/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package org.slf4j.impl;

import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;


/**
 * This implementation is bound to {@link NOPMDCAdapter}.
 */
public class StaticMDCBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();




  private StaticMDCBinder() {}




  /**
   * Currently this method always returns an instance of 
   * {@link StaticMDCBinder}.
   */
  public MDCAdapter getMDCA() {
    return new NOPMDCAdapter();
  }




  public String getMDCAdapterClassStr() {
    return NOPMDCAdapter.class.getName();
  }

}
