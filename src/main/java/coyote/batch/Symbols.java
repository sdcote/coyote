/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

/**
 * 
 */
public class Symbols {
  public static final String JOB_DIRECTORY = "jobdir";
  public static final String WORK_DIRECTORY = "wrkdir";
  public static final String DATE = "Date";
  public static final String TIME = "Time";
  public static final String DATETIME = "DateTime";
  public static final String MONTH = "Month";
  public static final String DAY = "Day";
  public static final String YEAR = "Year";
  public static final String HOUR = "Hour";
  public static final String MINUTE = "Minute";
  public static final String SECOND = "Second";
  public static final String MILLISECOND = "Millisecond";
  public static final String MONTH_MM = "MM";
  public static final String DAY_DD = "DD";
  public static final String YEAR_YYYY = "YYYY";
  public static final String HOUR_HH = "hh";
  public static final String MINUTE_MM = "mm";
  public static final String SECOND_SS = "ss";
  public static final String MILLISECOND_ZZZ = "zzz";

  public static final String PREV_MONTH_PM = "PM"; // previous days month
  public static final String PREV_DAY_PD = "PD"; // previous day
  public static final String PREV_YEAR_PYYY = "PYYY"; //previous days year

  public static final String PREV_MONTH_LM = "LM"; // last month
  public static final String PREV_YEAR_LMYY = "LMYY"; // the year last month

  public static final String RUN_COUNT = "RunCount";
  public static final String ERROR_COUNT = "FailureCount"; //

}
