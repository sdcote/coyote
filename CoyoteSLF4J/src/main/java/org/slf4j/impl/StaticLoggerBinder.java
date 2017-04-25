/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import coyote.loader.log.CategoryLoggerFactory;


/**
 * 
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

  /** The unique instance of this class. */
  private static final StaticLoggerBinder FIXTURE = new StaticLoggerBinder();

  /** The name of the logger factory */
  private static final String loggerFactoryClassStr = CategoryLoggerFactory.class.getName();

  /** Declare the version of the SLF4J API this implementation is compiled against. The value of this field is usually modified with each release. */
  public static String REQUESTED_API_VERSION = "1.7"; // this field must *not* be final

  /** The ILoggerFactory instance returned by the {@link #getLoggerFactory} method (should always be the same object). */
  private final ILoggerFactory loggerFactory;




  /**
   * Private constructor so no other instances are created.
   */
  private StaticLoggerBinder() {
    loggerFactory = new CategoryLoggerFactory();
  }




  /**
   * Return the singleton of this class.
   *
   * @return the StaticLoggerBinder singleton
   */
  public static final StaticLoggerBinder getSingleton() {
    return FIXTURE;
  }




  /**
   * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactory()
   */
  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }




  /**
   * @see org.slf4j.spi.LoggerFactoryBinder#getLoggerFactoryClassStr()
   */
  @Override
  public String getLoggerFactoryClassStr() {
    return loggerFactoryClassStr;
  }

}
