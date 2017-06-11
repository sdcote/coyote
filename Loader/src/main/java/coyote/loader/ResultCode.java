/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
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

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * The ResultCode class models a component that maps integers to a message with
 * the purpose of simplifying and standardizing result messages.
 * 
 * <p>This class support localized messages by converting the given integer to
 * as String and using it as a key for look-ups in the "ResultCodes" resource
 * bundles. If no resources are defined for that key, the returned string will
 * be the generic message in US English.</p>
 */
public class ResultCode {
  public static final int UNKNOWN = -1;
  public static final int RESERVED = 0; // 0 is often used to indicate success
  public static final int SUCCESS = 0; // 0 is often used to indicate success
  public static final int NAK = 1;
  public static final int SHUTDOWN = 2;
  public static final int AUTHENTICATION_FAILURE = 3;
  public static final int AUTHORIZATION_FAILURE = 4;
  public static final int FUNCTION_NOT_SUPPORTED = 5;
  public static final int UNABLE_TO_GET_CONNECTION = 6;
  public static final int UNABLE_TO_COMMIT = 7;
  public static final int MESSAGE_MISSING_INFO = 8;
  public static final int REQUEST_TIMED_OUT = 9;
  public static final int SESSION_LOOKUP_FAILED = 10;
  public static final int KEY_LOOKUP_FAILED = 11;
  public static final int DATA_MODIFIED_SINCE_LAST_READ = 12; //Attempt to update data using old data (from last_modified fields)
  public static final int TOKEN_NULL_OR_EMPTY = 13;
  public static final int INVALID_LICENSE_DATA = 14;
  public static final int LICENSE_CONVERT_ERROR = 15;
  public static final int INVALID_RESPONSE_DATA_FORMAT = 16;
  public static final int SERVICE_CONFIGURATION_DAMAGED = 22;

  //Workflow Engine (1000-1999)
  public static final int WORKFLOW_NOT_FOUND = 1000;
  public static final int WORKFLOW_LOCKED = 1001;
  public static final int WORKFLOW_NOT_VALID = 1002;
  public static final int TASK_ALREADY_ACCEPTED = 1003;
  public static final int TASK_NOT_IN_PROPER_STATE = 1004;
  public static final int ALREADY_EXISTS = 1005;
  public static final int WORKFLOW_VARIABLES_DONT_MATCH = 1006;

  public static final HashMap<Integer, String> message = new HashMap<Integer, String>();

  private static final ResourceBundle resbundle = ResourceBundle.getBundle( "ResultCodes", Locale.getDefault() );

  static {
    ResultCode.message.put( new Integer( ResultCode.UNKNOWN ), "The API could not determine the problem" );
    ResultCode.message.put( new Integer( ResultCode.NAK ), "The event was negatively acknowledged" );
    ResultCode.message.put( new Integer( ResultCode.SHUTDOWN ), "PowerSG API is in a shutdown state" );
    ResultCode.message.put( new Integer( ResultCode.AUTHENTICATION_FAILURE ), "Authentication Failure" );
    ResultCode.message.put( new Integer( ResultCode.AUTHORIZATION_FAILURE ), "Authorization Failure" );
    ResultCode.message.put( new Integer( ResultCode.FUNCTION_NOT_SUPPORTED ), "Function Not Supported" );
    ResultCode.message.put( new Integer( ResultCode.UNABLE_TO_GET_CONNECTION ), "Unable to receive database connection" );
    ResultCode.message.put( new Integer( ResultCode.UNABLE_TO_COMMIT ), "Unable to commit to database" );
    ResultCode.message.put( new Integer( ResultCode.MESSAGE_MISSING_INFO ), "Information required for the operation was not supplied" );
    ResultCode.message.put( new Integer( ResultCode.REQUEST_TIMED_OUT ), "Request timed-out before receiving a response" );
    ResultCode.message.put( new Integer( ResultCode.SESSION_LOOKUP_FAILED ), "Session lookup failed" );
    ResultCode.message.put( new Integer( ResultCode.KEY_LOOKUP_FAILED ), "Key lookup failed" );
    ResultCode.message.put( new Integer( ResultCode.INVALID_RESPONSE_DATA_FORMAT ), "One or more fields in the response contained data in an unexpected format" );

    ResultCode.message.put( new Integer( ResultCode.WORKFLOW_NOT_FOUND ), "Workflow not found" );
    ResultCode.message.put( new Integer( ResultCode.WORKFLOW_LOCKED ), "Workflow is currently locked" );
    ResultCode.message.put( new Integer( ResultCode.WORKFLOW_NOT_VALID ), "Workflow is not valid" );
    ResultCode.message.put( new Integer( ResultCode.TASK_ALREADY_ACCEPTED ), "Workflow task has already been accepted" );
    ResultCode.message.put( new Integer( ResultCode.TASK_NOT_IN_PROPER_STATE ), "Workflow task is not in proper state" );
  }




  /**
   * 
   */
  private ResultCode() {}




  /**
   * Get the message to the given result code for the current locale.
   * 
   * <p>If there is no message in the given locale, a more generic message will
   * be returned in US English. If the code is unknown a string indicating such
   * will be returned. This method should never return a null or emply 
   * value.</p>
   * 
   * @param code The code to lookup.
   * 
   * @return A string representing the message for the result code. Will not 
   *         return a null or empty string even if the code is not known.
   */
  public static final String getLocalizedMessage( final int code ) {
    String retval = null;
    try {
      retval = ResultCode.resbundle.getString( Integer.toString( code ) );
    } catch ( final Exception e ) {}

    if ( ( retval != null ) && ( retval.length() > 0 ) ) {
      return retval;
    }

    return ResultCode.getMessage( code );
  }




  /**
   * Return the text that describes the result code.
   * 
   * @param code The integer representing the code to describe.
   * 
   * @return A string representing the message for the result code. Will not 
   *         return a null or empty string even if the code is not known.
   */
  public static final String getMessage( final int code ) {
    if ( ( code > -1 ) && ( code <= ResultCode.message.size() ) ) {
      try {
        return (String)ResultCode.message.get( new Integer( code ) );
      } catch ( final Exception e ) {
        return new String( "Message code " + code + " is unknown" );
      }
    }

    return "Message code " + code + " is unknown";
  }

}
