/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseDialect;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dx.CDB;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.db.Database;
import coyote.dx.db.DatabaseConnector;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a frame reader which uses a JDBC result set to create frames.
 */
public class JdbcReader extends AbstractFrameReader {

  /** The thing we use to get connections to the database */
  private DatabaseConnector connector = null;

  /** The JDBC connection used by this reader to interact with the database */
  protected Connection connection;

  private ResultSet result = null;
  private Statement statement = null;
  private volatile boolean EOF = true;
  private ResultSetMetaData rsmd = null;
  private int columnCount = 0;




  /**
   * 
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.setContext(context);

    if (getConfiguration().containsIgnoreCase(ConfigTag.SOURCE)) {
      String source = getString(ConfigTag.SOURCE);
      Log.debug(LogMsg.createMsg(CDX.MSG, "Component.configured_source_is", this.getClass().getSimpleName(), source));

      // If we don't have a connection, prepare to create one
      if (connection == null) {

        String target = getConfiguration().getString(ConfigTag.SOURCE);
        target = Template.preProcess(target, context.getSymbols());
        Object obj = getContext().get(target);
        if (obj != null && obj instanceof DatabaseConnector) {
          setConnector((DatabaseConnector)obj);
          Log.debug(LogMsg.createMsg(CDB.MSG, "Component.found_connector_in_context", this.getClass().getSimpleName(), target));
        }

        if (getConnector() == null) {
          // we have to create a Database based on our configuration
          Database database = new Database();
          Config cfg = new Config();

          if (StringUtil.isNotBlank(getString(ConfigTag.SOURCE)))
            cfg.put(ConfigTag.TARGET, getString(ConfigTag.SOURCE)); // connection target

          if (StringUtil.isNotBlank(getString(ConfigTag.DRIVER)))
            cfg.put(ConfigTag.DRIVER, getString(ConfigTag.DRIVER));

          if (StringUtil.isNotBlank(getString(ConfigTag.LIBRARY)))
            cfg.put(ConfigTag.LIBRARY, getString(ConfigTag.LIBRARY));

          if (StringUtil.isNotBlank(getString(ConfigTag.USERNAME)))
            cfg.put(ConfigTag.USERNAME, getString(ConfigTag.USERNAME));

          if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)))
            cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));

          if (StringUtil.isNotBlank(getString(ConfigTag.PASSWORD)))
            cfg.put(ConfigTag.PASSWORD, getString(ConfigTag.PASSWORD));

          if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)))
            cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));

          setConnector(database);

          try {
            database.setConfiguration(cfg);
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_target", getClass().getSimpleName(), database.getTarget()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_driver", getClass().getSimpleName(), database.getDriver()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_library", getClass().getSimpleName(), database.getLibrary()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_user", getClass().getSimpleName(), database.getUserName()));
              Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_password", getClass().getSimpleName(), StringUtil.isBlank(database.getPassword()) ? 0 : database.getPassword().length()));
            }
          } catch (ConfigurationException e) {
            String msg = LogMsg.createMsg(CDB.MSG, "Component.could_not_configure_database", getClass().getSimpleName(), e.getMessage()).toString();
            Log.error(msg, e);
            context.setError(msg);
          }

          // if there is no schema in the configuration, set it to the same as the username
          if (StringUtil.isBlank(getString(ConfigTag.SCHEMA))) {
            getConfiguration().set(ConfigTag.SCHEMA, database.getUserName());
          }
        }
      } else {
        Log.debug(LogMsg.createMsg(CDB.MSG, "Component.using_existing_connection", getClass().getSimpleName()));
      }

      connection = getConnection();

      if (connection != null) {
        String query = getString(ConfigTag.QUERY);
        Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_query", this.getClass().getSimpleName(), query));

        try {
          statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
          result = statement.executeQuery(query);
          rsmd = result.getMetaData();
          columnCount = rsmd.getColumnCount();

          if (result.isBeforeFirst()) {
            EOF = false;
          }
        } catch (SQLException e) {
          String msg = LogMsg.createMsg(CDB.MSG, "Component.error_quering_database", getClass().getSimpleName(), e.getMessage().trim(), query).toString();
          context.setError(msg);
        }
      } else {
        String msg = LogMsg.createMsg(CDX.MSG, "Component.could_not_connect_to_source", getClass().getSimpleName(), getSource()).toString();
        Log.error(msg);
        context.setError(msg);
      }
    } else {
      String msg = LogMsg.createMsg(CDX.MSG, "Component.no_source_specified", getClass().getSimpleName()).toString();
      Log.error(msg);
      context.setError(msg);
    }
  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = null;

    if (result != null) {
      try {
        if (result.next()) {
          retval = new DataFrame();

          if (result.isLast()) {
            EOF = true;
            context.setLastFrame(true);
          }

          for (int i = 1; i <= columnCount; i++) {
            retval.add(rsmd.getColumnName(i), DatabaseDialect.resolveValue(result.getObject(i), rsmd.getColumnType(i)));
          }

          context.setLastFrame(result.isLast());
        } else {
          Log.error("Read past EOF");
          EOF = true;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        EOF = true;
      }
    } else {
      EOF = true;
    }

    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return EOF;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {
    DatabaseUtil.closeQuietly(result);
    DatabaseUtil.closeQuietly(connection);
    super.close();
  }




  /**  
  * Return the connector we use for 
  * @return the connector
  */
  public DatabaseConnector getConnector() {
    return connector;
  }




  /**
  * @param connector the connector to set
  */
  public void setConnector(DatabaseConnector connector) {
    this.connector = connector;
  }




  private Connection getConnection() {
    if (connection == null) {
      if (getConnector() == null) {
        Log.fatal(LogMsg.createMsg(CDB.MSG, "Component.no_connector", getClass().getSimpleName()));
      }
      connection = getConnector().getConnection();
      if (Log.isLogging(Log.DEBUG_EVENTS) && connection != null) {
        Log.debug(LogMsg.createMsg(CDB.MSG, "Database.connected_to", getClass().getSimpleName(), getSource()));
      }
    }
    return connection;
  }

}
