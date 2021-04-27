/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.dx.context.ContextListener;


/**
 * This listener keeps track of the data read in to and out of the engine and 
 * reports on the characteristics of the data observed including the SQL 
 * required for creating tables to hold the data read in and written out.
 */
public class DataProfilerSql extends DataProfiler implements ContextListener {

  /**
   * 
   */
  @SuppressWarnings("unchecked")
  private void writeOutputSQL() {
    if (outputSchema.getSampleCount() > 0) {
      StringBuffer b = new StringBuffer("Table Creation for Output");
      b.append(StringUtil.LINE_FEED);
      symbols.put(DatabaseDialect.DB_SCHEMA_SYM, "DBUser");
      symbols.put(DatabaseDialect.TABLE_NAME_SYM, "TableName");
      b.append("H2: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.H2, outputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append("Oracle: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.ORACLE, outputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append("MySQL: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.MYSQL, outputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append(StringUtil.LINE_FEED);
      write(b.toString());
    }
  }




  /**
   * 
   */
  @SuppressWarnings("unchecked")
  private void writeInputSQL() {
    if (inputSchema.getSampleCount() > 0) {
      StringBuffer b = new StringBuffer("Table Creation for Input");
      b.append(StringUtil.LINE_FEED);
      symbols.put(DatabaseDialect.DB_SCHEMA_SYM, "DBUser");
      symbols.put(DatabaseDialect.TABLE_NAME_SYM, "TableName");
      b.append("H2: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.H2, inputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append("Oracle: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.ORACLE, inputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append("MySQL: ");
      b.append(DatabaseDialect.getCreate(DatabaseDialect.MYSQL, inputSchema, symbols));
      b.append(StringUtil.LINE_FEED);
      b.append(StringUtil.LINE_FEED);
      write(b.toString());
    }
  }




  /**
   * Write the summary of the data read in.
   */
  @Override
  protected void writeInputSummary() {
    super.writeInputSummary();
    writeInputSQL();
  }




  /**
   * Write the summary of the data written out.
   */
  @Override
  protected void writeOutputSummary() {
    super.writeOutputSummary();
    writeOutputSQL();
  }

}