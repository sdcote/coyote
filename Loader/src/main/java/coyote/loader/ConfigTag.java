/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader;

/**
 * 
 */
public class ConfigTag {

  // System Properties

  /** Name ({@value}) of the system property containing the URI used to load the configuration. */
  public static final String CONFIG_URI = "cfg.uri";

  /** Name ({@value}) of the system property containing the name of the cipher to use. */
  public static final String CIPHER_NAME = "cipher.name";

  /** Name ({@value}) of the system property containing the Base64 cipher key value. */
  public static final String CIPHER_KEY = "cipher.key";

  // Configuration tags

  /** Name ({@value}) of the Logging configuration sections. */
  public static final String LOGGING = "Logging";

  /** Name ({@value}) of the categories of events a logger should log. */
  public static final String CATEGORIES = "categories";

  /** Name ({@value}) of the tag specifying the logging target. */
  public static final String TARGET = "target";

  /** Name ({@value}) of the configuration attribute specifying a class. */
  public static final String CLASS = "Class";

  /** Name ({@value}) of the configuration attribute specifying a name. */
  public static final String NAME = "Name";

  /** Name ({@value}) of the configuration attribute specifying an identifier. */
  public static final String ID = "ID";

  /** Name ({@value}) of the configuration attribute specifying the component to load. */
  public static final String COMPONENT = "Component";

  /** Name ({@value}) of the configuration section specifying the Internet Protocol Access Control List (IPACL). */
  public static final String IPACL = "IpACL";

  /** Name ({@value}) of the configuration attribute specifying the Denial of Service (DoD) frequency table. */
  public static final String FREQUENCY = "Frequency";

  /** Flag ({@value}) indicating the loader should continually repeat its execution. */
  public static final String REPEAT = "Repeat";

  // These tags are legacy configuration elements -- may be deprecated
  public static final String LOG_TAG = "Log";
  public static final String ENABLED_TAG = "Enabled";
  public static final String DESCRIPTION_TAG = "Description";
  public static final String ACTIVATION_TOKEN_TAG = "ActivationToken";
  public static final String INTERVAL_TAG = "CycleInterval";

  // Scheduler
  public static final String PATTERN = "Pattern";
  public static final String SCHEDULE = "Schedule";
  public static final String MINUTES = "Minutes";
  public static final String HOURS = "Hours";
  public static final String DAYS = "Days";
  public static final String DAYS_OF_WEEK = "Weekdays";
  public static final String MONTHS = "Months";
  public static final String MILLIS = "Millis";

}
