/*
 * Copyright (c) 2021 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.text.table;

import coyote.TestUtil;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


/**
 *
 */
public class TextTableTest {


  /**
   * Tests a simple table with example data.
   */
  @Test
  public void testSimpleTable() throws IOException {
    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", "James Holden", "jholden@uncn.mil"},
            {"456", "Naomi Nagata", "nnagata@canterbury.uta"},
            {"789", "Amos Burton", "aburton@baltimore.us"},
            {"101", "Alex Kamal", "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/simpleTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).toString())
    );
    // ASCII Table Format
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/simpleTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).withTableFormat(new TextTableFormat()).toString())
    );

  }


  /**
   * Tests a simple table with example data and a right-aligned column.
   */
  @Test
  public void testSimpleTableRightAlign() throws IOException {
    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", "James Holden", "jholden@uncn.mil"},
            {"456", "Naomi Nagata", "nnagata@canterbury.uta"},
            {"789", "Amos Burton", "aburton@baltimore.us"},
            {"101", "Alex Kamal", "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/simpleTableRight-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).alignColumn(2, TextTable.Align.RIGHT).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/simpleTableRight.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).alignColumn(2, TextTable.Align.RIGHT).withTableFormat(new TextTableFormat()).toString())
    );

  }


  /**
   * Tests tables with no data.
   */
  @Test
  public void testEmptyTables() throws IOException {
    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] emptyData = new String[0][0];

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/emptyTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, emptyData).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/emptyTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, emptyData).withTableFormat(new TextTableFormat()).toString())
    );

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/emptyTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, null).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/emptyTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, null).withTableFormat(new TextTableFormat()).toString())
    );
  }


  /**
   * Tests a table with too few columns in the data array.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNotEnoughColumns() throws IOException {

    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] badData = new String[][]{
            {"123", "James Holden", "jholden@uncn.mil"},
            {"456", "Naomi Nagata"}, // Missing column
            {"789", "Amos Burton", "aburton@baltimore.us"},
            {"101", "Alex Kamal", "aamal@mcrn.mil"},
    };

    TextTable.fromData(headers, badData);
  }


  /**
   * Tests a table with newlines in the data.
   */
  @Test
  public void testMultipleLines() throws IOException {
    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", "James\nHolden", "jholden@uncn.mil"},
            {"456", "Naomi\nNagata", "nnagata@canterbury.uta"},
            {"789", "Amos\nBurton", "aburton@baltimore.us"},
            {"101", "Alex\nKamal", "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/multiLineTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/multiLineTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).withTableFormat(new TextTableFormat()).toString())
    );
  }


  /**
   * Tests a table which has other tables nested within it.
   */
  @Test
  public void testNestedTables() throws IOException {
    String[] nestedHeaders = new String[]{"First", "Last"};
    String[] names = new String[]{"James Holden", "Naomi Nagata", "Amos Burton", "Alex Kamal"};
    TextTable[] nestedTables = new TextTable[4];
    TextTable[] nestedTablesASCIIFormat = new TextTable[4];
    for (int i = 0; i < names.length; i++) {
      nestedTables[i] = TextTable.fromData(nestedHeaders, new String[][]{names[i].split(" ")});
      nestedTablesASCIIFormat[i] = TextTable.fromData(nestedHeaders, new String[][]{names[i].split(" ")}).withTableFormat(new TextTableFormat());
    }

    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", nestedTables[0].toString(), "jholden@uncn.mil"},
            {"456", nestedTables[1].toString(), "nnagata@canterbury.uta"},
            {"789", nestedTables[2].toString(), "aburton@baltimore.us"},
            {"101", nestedTables[3].toString(), "aamal@mcrn.mil"},
    };
    String[][] dataASCIIFormat = new String[][]{
            {"123", nestedTablesASCIIFormat[0].toString(), "jholden@uncn.mil"},
            {"456", nestedTablesASCIIFormat[1].toString(), "nnagata@canterbury.uta"},
            {"789", nestedTablesASCIIFormat[2].toString(), "aburton@baltimore.us"},
            {"101", nestedTablesASCIIFormat[3].toString(), "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/nestedTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/nestedTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, dataASCIIFormat).withTableFormat(new TextTableFormat()).toString())
    );

  }


  /**
   * Tests a table with custom null data.
   */
  @Test
  public void testNullDataTablesWithDefaultNullValue() throws IOException {
    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", "James Holden", "jholden@uncn.mil"},
            {"456", "Naomi Nagata", "nnagata@canterbury.uta"},
            {"789", null, "aburton@baltimore.us"},
            {"101", "Alex Kamal", "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/nullDataTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/nullDataTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).withTableFormat(new TextTableFormat()).toString())
    );

  }


  /**
   * Tests a table with default null data.
   */
  @Test
  public void testNullDataTablesWithCustomNullValue() throws IOException {

    String[] headers = new String[]{"ID", "Name", "Email"};
    String[][] data = new String[][]{
            {"123", "James Holden", "jholden@uncn.mil"},
            {"456", "Naomi Nagata", "nnagata@canterbury.uta"},
            {"789", null, "aburton@baltimore.us"},
            {"101", "Alex Kamal", "aamal@mcrn.mil"},
    };

    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/customNullDataTable-UTF8.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).withNullValue("n/a").toString())
    );
    assertEquals(
            TestUtil.commonizeLineEndings(TestUtil.readFileToString("tables/customNullDataTable.txt")),
            TestUtil.commonizeLineEndings(TextTable.fromData(headers, data).withNullValue("n/a").withTableFormat(new TextTableFormat()).toString())
    );

  }

}