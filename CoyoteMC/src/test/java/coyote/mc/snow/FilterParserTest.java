/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.commons.StringParseException;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * TODO: use Filter.clauseCount() to make sure all the clauses have been parsed
 */
public class FilterParserTest {

  String[] testdata = {
          "active is true and category is Software",
          "name is REQ123",
          "name = REQ123",
          "name=REQ123",
          "name is REQ123 ^OR number notempty",
          "name is REQ123",
          "name = REQ123",
          "name contains REQ123",
          "name like REQ123",
          "name startswith REQ123",
          "name != REQ123",
          "sys_updated_on>=2015-07-09 00:00:00^sys_updated_on<2015-07-10 00:00:00",
          "pc_sys_class_name=u_notebook_pc^ORpc_sys_class_name=u_tablet_pc^ORpc_sys_class_name=u_thin_client_pc^ORpc_sys_class_name=cmdb_ci_desktop_pc^ORpc_sys_class_name=u_computer_pc^ORpc_sys_class_name=u_cmdb_virtual_pc"
  };


  @Test
  public void testParseDate() {
    String text = "sys_updated_on>=2015-07-09 00:00:00^sys_updated_on<2015-07-10 00:00:00";
    try {
      SnowFilter filter = FilterParser.parse(text);
      assertNotNull(filter);
      assertEquals(filter.clauseCount(), 2);
      //System.out.println( filter.toString() );
    } catch (StringParseException e) {
      fail("failed to parse '" + text + "' - " + e.getMessage());
    }
  }


  @Test
  public void testParseOR() {
    String text = "name is REQ123 ^OR number notempty";
    try {
      SnowFilter filter = FilterParser.parse(text);
      assertNotNull(filter);
      assertEquals(filter.clauseCount(), 2);
      //System.out.println( filter.toString() );
    } catch (StringParseException e) {
      fail("failed to parse '" + text + "' - " + e.getMessage());
    }
  }


  @Test
  public void testParseData() {
    String data = null;
    try {
      for (int x = 0; x < testdata.length; x++) {
        data = testdata[x];
        SnowFilter filter = FilterParser.parse(data);
        assertNotNull( "Should not be null",filter);
      }
    } catch (StringParseException e) {
      System.err.println(data);
      fail("failed to parse '" + data + "'");
    }

  }


  @Test
  public void testParserComplexFilter() {
    String text = "install_status != 7 ^ install_status != 8 ^ ci.sys_class_name = u_notebook_pc ^OR ci.sys_class_name = u_tablet_pc ^OR ci.sys_class_name = u_thin_client_pc ^OR ci.sys_class_name = cmdb_ci_desktop_pc ^OR ci.sys_class_name = u_computer_pc ^OR ci.sys_class_name = u_cmdb_virtual_pc ^ ci.u_compensation_code STARTSWITH 8";
    try {
      SnowFilter filter = FilterParser.parse(text);
      assertNotNull(filter);
      assertEquals(filter.clauseCount(), 9);
      System.out.println(filter.toString());
    } catch (StringParseException e) {
      fail("failed to parse '" + text + "' - " + e.getMessage());
    }
  }

}
