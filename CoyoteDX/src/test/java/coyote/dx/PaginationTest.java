/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;


/**
 * 
 */
public class PaginationTest {

  /**
   * Test method for {@link coyote.dx.Pagination#step()}.
   */
  @Test
  public void testStep() {
    Pagination page = new Pagination(5);
    assertEquals("page", page.getName());
    assertTrue(page.getStep() == 5);
    assertTrue(page.getOffset() == 0);
    page.step();
    assertTrue(page.getOffset() == 5);
  }




  /**
   * Test method for {@link coyote.dx.Pagination#reset()}.
   */
  @Test
  public void testReset() {
    final String NAME = "offset";
    Pagination page = new Pagination(NAME, 5);
    assertEquals(NAME, page.getName());
    assertTrue(page.getStep() == 5);
    assertTrue(page.getOffset() == 0);
    page.step();
    page.step();
    page.step();
    page.step();
    page.step();
    page.step();
    assertTrue(page.getOffset() == 30);
    assertTrue(page.getEnd() == 35);
    page.reset();
    assertTrue(page.getOffset() == 0);
    assertTrue(page.getEnd() == 5);
  }




  /**
   * Test method for {@link coyote.dx.Pagination#toSymbolTable()}.
   */
  @Test
  public void testToSymbolTable() {
    final String NAME = "offset";
    Pagination page = new Pagination(NAME, 0, 5);
    assertEquals(NAME, page.getName());
    assertTrue(page.getStep() == 5);
    assertTrue(page.getOffset() == 0);
    page.step();
    assertTrue(page.getOffset() == 5);

    SymbolTable symbols = page.toSymbolTable();
    assertNotNull(symbols);
    assertTrue(StringUtil.isNotBlank(symbols.getString(NAME + ".start")));
    assertTrue(StringUtil.isNotBlank(symbols.getString(NAME + ".size")));
    assertTrue(StringUtil.isNotBlank(symbols.getString(NAME + ".end")));
    assertTrue(StringUtil.isBlank(symbols.getString(NAME + ".pickle")));
  }




  public void startStepCtor() {
    Pagination page = new Pagination(10, 5);
    assertTrue(page.getStep() == 5);
    assertTrue(page.getOffset() == 10);
    page.step();
    assertTrue(page.getOffset() == 15);
  }




  @Test
  public void blankName() {
    Pagination page = new Pagination("", 0, 5);
    assertEquals(Pagination.DEFAULT_NAME, page.getName());
    page = new Pagination(" ", 0, 5);
    assertEquals(Pagination.DEFAULT_NAME, page.getName());
    page = new Pagination(null, 0, 5);
    assertEquals(Pagination.DEFAULT_NAME, page.getName());
  }




  @Test
  public void toStringTest() {
    final String expected = "Pagination: 'offset' start:0 step:5";
    final String NAME = "offset";
    Pagination page = new Pagination(NAME, 0, 5);
    assertEquals(expected, page.toString());
  }

}
