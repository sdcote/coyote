/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * 
 */
public class JdbcReaderTest {
  private static final String CATALOG = "readertest";
  private static final String JDBC_DRIVER = "org.h2.Driver";
  private static final String DB_URL = "jdbc:h2:./"+CATALOG;
  private static final String LIBRARY_LOC = "jar:file:.src/resources/demojars/h2-1.4.196.jar!/";
  private static final String USER = "username";
  private static final String PASS = "password";
  private static final String TABLE = "users";




  /**
   * 
   */
  @BeforeClass
  public static void setUpBeforeClass() {
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.TRACE_EVENTS | Log.DEBUG_EVENTS | Log.INFO_EVENTS | Log.NOTICE_EVENTS | Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));
    Connection conn = null;
    Statement stmt = null;
    try {
      Class.forName(JDBC_DRIVER);
      conn = DriverManager.getConnection(DB_URL, USER, PASS);
      stmt = conn.createStatement();
      String sql;
      sql = "CREATE TABLE " + TABLE + " AS SELECT * FROM CSVREAD('classpath:users.csv');";
      stmt.executeUpdate(sql);
      conn.commit();
    } catch (Exception e) {
      Log.error("Could not create database", e);
    } finally {
      DatabaseUtil.closeQuietly(stmt);
      DatabaseUtil.closeQuietly(conn);
    }
  }




  /**
   * @throws Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File dbfile = new File(CATALOG+".mv.db");
    Log.debug(dbfile.getAbsolutePath());
    dbfile.delete();
    // delete the trace file if it exists - it's text file showing what commands were processed
    dbfile = new File(CATALOG+".trace.db");
    if (dbfile.exists()) {
      Log.debug(dbfile.getAbsolutePath());
      dbfile.delete();
    }
  }




  @Test
  public void basicRead() {
    DataFrame cfg = new DataFrame() //
        .set(ConfigTag.SOURCE, DB_URL) //
        .set(ConfigTag.DRIVER, JDBC_DRIVER) //
        .set(ConfigTag.USERNAME, USER) //
        .set(ConfigTag.PASSWORD, PASS) //
        .set(ConfigTag.QUERY, "select * from " + TABLE + "");
    Config config = new Config(cfg);
    System.out.println(config.toFormattedString());

    JdbcReader reader = new JdbcReader();
    try {
      reader.setConfiguration(config);
      TransformContext context = new TransformContext();
      reader.open(context);
      if (StringUtil.isNotBlank(context.getErrorMessage())) {
        Log.error(context.getErrorMessage());
      }
      assertFalse(context.isInError());

      TransactionContext txncontext = new TransactionContext(context);
      int count = 0;
      while (!reader.eof()) {
        count++;
        DataFrame frame = reader.read(txncontext);
        if (txncontext.isInError()) {
          Log.error("Read error: " + txncontext.getErrorMessage());
          break;
        }
        if (frame == null) {
          break;
        }
        if (count > 250) {
          break;
        }
      }
      assertTrue(count == 50);
      assertFalse(txncontext.isInError());

    } catch (Exception e) {
      Log.error(e);
      fail("Exception:" + e);
    } finally {
      try {
        reader.close();
      } catch (Exception ignore) {
        // be quiet
      }
    }
  }




  @Test
  public void libraryConfig() {
    DataFrame cfg = new DataFrame() //
        .set(ConfigTag.SOURCE, DB_URL) //
        .set(ConfigTag.DRIVER, JDBC_DRIVER) //
        .set(ConfigTag.LIBRARY, LIBRARY_LOC) //
        .set(ConfigTag.USERNAME, USER) //
        .set(ConfigTag.PASSWORD, PASS) //
        .set(ConfigTag.QUERY, "select Role, Username from " + TABLE + "");
    Config config = new Config(cfg);
    System.out.println(config.toFormattedString());

    JdbcReader reader = new JdbcReader();
    try {
      reader.setConfiguration(config);
      TransformContext context = new TransformContext();
      reader.open(context);
      if (StringUtil.isNotBlank(context.getErrorMessage())) {
        Log.error(context.getErrorMessage());
      }
      assertFalse(context.isInError());
      TransactionContext txncontext = new TransactionContext(context);
      int count = 0;
      while (!reader.eof()) {
        count++;
        if (count > 250) {
          break;
        }
      }
      assertFalse(txncontext.isInError());
    } catch (Exception e) {
      Log.error(e);
      fail("Exception:" + e);
    } finally {
      try {
        reader.close();
      } catch (Exception ignore) {
        // be quiet
      }
    }
  }

}
