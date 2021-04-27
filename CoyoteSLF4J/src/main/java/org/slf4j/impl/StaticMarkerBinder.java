/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package org.slf4j.impl;

import org.slf4j.IMarkerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;


/**
 * The binding of {@link MarkerFactory} class with an actual instance of 
 * {@link IMarkerFactory} is performed using information returned by this class. 
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

  final IMarkerFactory markerFactory = new BasicMarkerFactory();




  private StaticMarkerBinder() {}




  /**
   * Currently this method always returns an instance of 
   * {@link BasicMarkerFactory}.
   */
  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }




  /**
   * Currently, this method returns the class name of
   * {@link BasicMarkerFactory}.
   */
  @Override
  public String getMarkerFactoryClassStr() {
    return BasicMarkerFactory.class.getName();
  }

}
