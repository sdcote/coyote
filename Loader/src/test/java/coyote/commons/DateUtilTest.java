/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 */
public class DateUtilTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}




  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {}




  @Test
  public void test() {

    Date date = DateUtil.parse( "2017-06-16T13:32:19.504-04" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16T13:32:19.504" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16T13:32:19" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16T13:32:19" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16 13:32:19.504-04" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16 13:32:19.504" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16 13:32:19" );
    assertNotNull( date );

    date = DateUtil.parse( "2017-06-16" );
    assertNotNull( date );

    String text = date.toString();
    date = DateUtil.parse( text );
    assertNotNull( date );

  }

}
