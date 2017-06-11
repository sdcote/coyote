/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.i13n;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * This is a set of tests which demonstrate how to use the StatBoard class in
 * regular operations.
 * 
 * <p>Demo tests are used to illustrate how to use public portions of the code 
 * and are intended to be illustrative and interrogatory.
 */
public class StatBoardDemo {

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




  /**
   * Test method for {@link coyote.i13n.StatBoardImpl#getId()}.
   */
  @Test
  public void testGetId() {
    StatBoard scorecard = new StatBoardImpl();

    // All scorecards should have an identifier
    assertNotNull( scorecard.getId() );

  }

}
