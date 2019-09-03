/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.commons.StringParseException;


/**
 * 
 */
public class FilterStepper {

  public static void main( final String[] args ) {

    String query = "pc_sys_class_name=u_notebook_pc^ORpc_sys_class_name=u_tablet_pc^ORpc_sys_class_name=u_thin_client_pc^ORpc_sys_class_name=cmdb_ci_desktop_pc^ORpc_sys_class_name=u_computer_pc^ORpc_sys_class_name=u_cmdb_virtual_pc";
    try {
      SnowFilter filter = FilterParser.parse( query );
      System.out.println( "Here is what we parsed:\r\n" + filter.toString() + "\r\nClause count = " + filter.clauseCount() );
    } catch ( StringParseException e ) {
      e.printStackTrace();
    }

  }
}
