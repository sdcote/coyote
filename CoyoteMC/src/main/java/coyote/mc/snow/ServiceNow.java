/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

/**
 * Holds the constants for this API
 */
public class ServiceNow {

  public static final String SYS_DB_OBJ = "sys_db_object";
  public static final String SYS_DICTIONARY = "sys_dictionary";
  public static final String SYS_DOCUMENTATION = "sys_documentation";

  /**
   * External Communication Channel table
   */
  public static final String ECC_QUEUE = "ecc_queue";

  /**
   * Contains the database views in the system
   */
  public static final String SYSDBVIEW = "sys_db_view";

  /**
   * Contains the tables which make up the views in the system
   */
  public static final String VIEW_TABLE = "sys_db_view_table";

  public static final String SYS_ID_FIELD = "sys_id";
  public static final String SYS_UPDATED_ON_FIELD = "sys_updated_on";
  public static final String SYS_CREATED_ON_FIELD = "sys_created_on";
  public static final String SYS_CREATED_BY_FIELD = "sys_created_by";
  public static final String SYS_MOD_COUNT_FIELD = "sys_mod_count";
  public static final String SYS_UPDATED_BY_FIELD = "sys_updated_by";
  private static final String CRITICAL = "Critical";
  private static final String HIGH = "High";
  private static final String MODERATE = "Moderate";
  private static final String LOW = "Low";
  private static final String PLANNING = "Planning";
  private static final String MEDIUM = "Medium";


  /**
   * @param number the value of 1,2,3,4 or 5. All other values will be returned as "Planning"
   * @return the string value for the priority level number
   */
  public static String getPriorityValue(String number) {
    String retval;
    String str = "two";
    switch (number) {
      case "1":
        retval = CRITICAL;
        break;
      case "2":
        retval = HIGH;
        break;
      case "3":
        retval = MODERATE;
        break;
      case "4":
        retval = LOW;
        break;
      default:
        retval = PLANNING;
    }
    return retval;
  }


  public static String getUrgencyValue(String number) {
    String retval;
    String str = "two";
    switch (number) {
      case "1":
        retval = HIGH;
        break;
      case "2":
        retval = MEDIUM;
        break;
      case "3":
      default:
        retval = LOW;
    }
    return retval;
  }

  public static String getImpactValue(String number) {
    return getUrgencyValue(number);
  }

}