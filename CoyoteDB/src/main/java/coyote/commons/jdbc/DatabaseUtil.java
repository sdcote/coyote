/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;

import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;
import coyote.loader.log.Log;


/**
 * 
 */
public class DatabaseUtil {

  /**
   * Get the name of the product to which this connection is attached.
   * 
   * @param connection the connection to query
   * 
   * @return the name of the database product attached to this connection.
   */
  public static String getProduct(Connection connection) {
    String retval = null;
    try {
      DatabaseMetaData meta = connection.getMetaData();
      String product = meta.getDatabaseProductName();
      if (StringUtil.isNotBlank(product)) {
        retval = product;
      }
    } catch (SQLException e) {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Could not get database product name: " + e.getClass().getName() + " - " + e.getMessage());
      }
    }
    return retval;
  }




  /**
   * Get the version of the database to which we are connected.
   * 
   * @param connection the connection to query
   * 
   * @return The version of the database used by this connection.
   */
  public static Version getDatabaseVersion(Connection connection) {
    Version retval = null;
    try {
      DatabaseMetaData meta = connection.getMetaData();
      retval = parseVersionString(meta.getDatabaseProductVersion());
      if (retval == null) {
        retval = new Version(meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion(), 0);
      }
    } catch (SQLException e) {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Could not get database product version: " + e.getClass().getName() + " - " + e.getMessage());
      }
    }
    return retval;
  }




  /**
   * Get the version of the database to which we are connected.
   * 
   * @param connection the connection to query
   * 
   * @return The version of the database used by this connection.
   */
  public static Version getDriverVersion(Connection connection) {
    Version retval = null;
    try {
      DatabaseMetaData meta = connection.getMetaData();
      retval = parseVersionString(meta.getDriverVersion());
      if (retval == null) {
        retval = new Version(meta.getDriverMajorVersion(), meta.getDriverMinorVersion(), 0);
      }
    } catch (SQLException e) {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Could not get driver version: " + e.getClass().getName() + " - " + e.getMessage());
      }
    }
    return retval;
  }




  /**
   * Locate the version in the string and pares it into a version.
   * 
   * <p>This splits the string up by spaces and tries to locate the first 
   * token containing a dot (.) and passes it to the Version object for 
   * parsing.
   * 
   * @param text the string to parse
   * 
   * @return A version based on the string passed or null if no string could 
   *         be found.
   */
  private static Version parseVersionString(String text) {
    Version retval = null;
    if (StringUtil.isNotBlank(text)) {
      String[] tokens = text.split(" ");
      for (int x = 0; x < tokens.length; x++) {
        String str = tokens[x];
        if (StringUtil.isNotBlank(str) && str.indexOf('.') > 0) {
          retval = Version.createVersion(str);
          if (retval != null && retval.getMajor() > 0) {
            break;
          } else {
            retval = null;
          }
        }
      }
    }
    return retval;
  }




  /**
   * Generate a TableDefinition for the given table name.
   * 
   * <p>Mappings are generally those suggested in the Oracle JDBC mapping guide 
   * with minor exceptions for DECIMAL and NUMERIC as BigDecimal is not 
   * supported by Data Frame at this time.
   * 
   * @param tablename name of the table being generated
   * 
   * @return a table schema for the database table to which this writer is writing.
   */
  public static TableDefinition getTableSchema(Connection connection, String tablename) {
    TableDefinition retval = null;
    if (StringUtil.isNotBlank(tablename)) {
      if (connection == null) {
        throw new IllegalArgumentException("Null connection argument");
      }

      String tableSchemaName = null;

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = connection.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables(null, null, "%", null);
        while (rs.next()) {
          if (StringUtil.equalsIgnoreCase(tablename, rs.getString("TABLE_NAME"))) {
            tableSchemaName = rs.getString("TABLE_NAME");
            break;
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException ignore) {}
        }
      }

      if (StringUtil.isNotEmpty(tableSchemaName)) {
        retval = new TableDefinition(tableSchemaName);

        rs = null;
        try {
          DatabaseMetaData meta = connection.getMetaData();

          String product = meta.getDatabaseProductName();
          if (StringUtil.isNotBlank(product)) {
            retval.setProductName(product.toUpperCase());
          }
          retval.setProductVersion(meta.getDatabaseProductVersion());
          retval.setMajorVersion(meta.getDatabaseMajorVersion());
          retval.setMinorVersion(meta.getDatabaseMinorVersion());

          rs = meta.getColumns(null, null, tableSchemaName, "%");

          String name;
          ColumnType type;
          int length;
          boolean readOnly;
          boolean mandatory;
          boolean primaryKey;
          boolean unique;
          boolean nullable;
          int pos;
          String remarks;

          while (rs.next()) {
            readOnly = nullable = mandatory = primaryKey = unique = false;
            length = pos = 0;
            name = remarks = null;

            if (rs.getString("TABLE_CAT") != null && retval.getCatalogName() == null) {
              retval.setCatalogName(rs.getString("TABLE_CAT"));
            }

            if (rs.getString("TABLE_SCHEM") != null && retval.getSchemaName() == null) {
              retval.setSchemaName(rs.getString("TABLE_SCHEM"));
            }

            name = rs.getString("COLUMN_NAME");
            length = rs.getInt("COLUMN_SIZE");
            pos = rs.getInt("ORDINAL_POSITION");
            remarks = rs.getString("REMARKS");

            switch (rs.getInt("DATA_TYPE")) {
              case Types.TIME:
              case Types.TIMESTAMP:
              case Types.DATE:
                type = ColumnType.DATE;
                break;
              case Types.BOOLEAN:
                type = ColumnType.BOOLEAN;
                break;
              case Types.TINYINT:
                type = ColumnType.BYTE;
                break;
              case Types.SMALLINT:
                type = ColumnType.SHORT;
                break;
              case Types.INTEGER:
                type = ColumnType.INT;
                break;
              case Types.FLOAT:
              case Types.DOUBLE:
              case Types.REAL:
                type = ColumnType.FLOAT;
                break;
              case Types.DECIMAL:
              case Types.NUMERIC:
                type = ColumnType.DOUBLE;
                break;
              case Types.BIGINT:
                type = ColumnType.LONG;
                break;
              case Types.DISTINCT:
                unique = true;
                type = ColumnType.STRING;
                break;
              default:
                type = ColumnType.STRING;
                break;
            }

            switch (rs.getInt("NULLABLE")) {
              case DatabaseMetaData.columnNoNulls:
                nullable = false;
                break;
              case DatabaseMetaData.columnNullable:
                nullable = true;
                break;
              case DatabaseMetaData.columnNullableUnknown:
                nullable = false;
                break;
              default:
                nullable = false;
                break;
            }
            retval.addColumn(new ColumnDefinition(name, type, length, nullable, readOnly, mandatory, primaryKey, unique, remarks, pos));
          }

        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (rs != null) {
            try {
              rs.close();
            } catch (SQLException ignore) {}
          }
        }
      }
    }
    Log.trace("Returning table definition of: " + retval);
    return retval;
  }




  /**
   * Read from the database and return the first record from the result set.
   * 
   * <p>Handy for reading a record by its key, expecting one result back. 
   * Commonly used for Select * from [whatever] where SysId = [somevalue]
   * 
   * @return A DataFrame containing the record result or null if no record was retrieved.
   */
  public static DataFrame readRecord(Connection connection, String query) {
    DataFrame retval = null;

    ResultSet result = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;

    Log.debug(String.format("Executing query: '%s'", query));

    if (connection != null) {

      try {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        result = statement.executeQuery(query);

        rsmd = result.getMetaData();

        columnCount = rsmd.getColumnCount();

        if (result != null) {
          try {
            if (result.next()) {
              retval = new DataFrame();
              for (int i = 1; i <= columnCount; i++) {
                retval.add(rsmd.getColumnName(i), DatabaseDialect.resolveValue(result.getObject(i), rsmd.getColumnType(i)));
              }
            } else {
              Log.debug("No data, read past EOF - query: " + query);
              return retval;
            }
          } catch (SQLException e) {
            e.printStackTrace();
          }
        }
      } catch (SQLException e) {
        String emsg = String.format("Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query);
        Log.error(emsg);
      }
    }
    return retval;
  }




  /**
   * Designed for SELECT queries to allow all results to be retrieved into a 
   * list of DataFrames
   * 
   * @param connection the connection on which to perform the query
   * @param query the SQL (SELECT) query to perform
   * 
   * @return FrameSet containing the DataFrames representing the retrieved 
   *         data, may be empty, but never null.
   */
  public static FrameSet readAllRecords(Connection connection, String query) {
    FrameSet retval = new FrameSet();

    ResultSet result = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;

    Log.debug(String.format("Executing query: '%s'", query));

    if (connection != null) {
      try {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        result = statement.executeQuery(query);
        rsmd = result.getMetaData();
        columnCount = rsmd.getColumnCount();

        if (result != null) {
          try {
            while (result.next()) {
              DataFrame record = new DataFrame();
              for (int i = 1; i <= columnCount; i++) {
                // Log.debug( rsmd.getColumnName( i ) + " - '" + result.getString( i ) + "' (" + rsmd.getColumnType( i ) + ")" );
                record.add(rsmd.getColumnName(i), DatabaseDialect.resolveValue(result.getObject(i), rsmd.getColumnType(i)));
              }
              retval.add(record);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      } catch (SQLException e) {
        String emsg = String.format("Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query);
        Log.error(emsg);
      }

    }
    return retval;
  }




  public static Timestamp getCurrentTimeStamp() {
    return getTimeStamp(new java.util.Date());
  }




  public static Timestamp getTimeStamp(java.util.Date date) {
    return new Timestamp(date.getTime());
  }




  /**
   * Determine if a particular table exists in the database.
   * 
   * @param tablename The name of the table for which to query
   * 
   * @return true the named table exists, false the table does not exist.
   */
  public static boolean tableExists(String tablename, Connection conn) {
    return tableExists(tablename, null, conn);
  }




  /**
   * Determine if a particular table exists in a particular schema of the database.
   * 
   * @param table name of the table to search
   * @param schema name of the schema in which to search (may be null)
   * @param conn the connection on which to communicate with the database
   * 
   * @return true if that table exists, false otherwise.
   */
  public static boolean tableExists(String table, String schema, Connection conn) {
    boolean retval = false;
    if (StringUtil.isNotBlank(table)) {
      if (conn == null) {
        Log.error("Cannot get connection");
        return false;
      }

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = conn.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables(null, null, "%", null);
        while (rs.next()) {
          if (StringUtil.equalsIgnoreCase(table, rs.getString("TABLE_NAME")) && (StringUtil.isEmpty(schema) || (StringUtil.isNotEmpty(schema) && StringUtil.equalsIgnoreCase(schema, rs.getString("TABLE_SCHEM"))))) {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              StringBuffer b = new StringBuffer("Found ");
              b.append(rs.getString("TABLE_SCHEM"));
              b.append('.');
              b.append(rs.getString("TABLE_NAME"));
              if (StringUtil.isNotEmpty(rs.getString("TYPE_CAT"))) {
                b.append(" in catalog ");
                b.append(rs.getString("TYPE_CAT"));
              }
              b.append(" of type '");
              b.append(rs.getString("TABLE_TYPE"));
              b.append('\'');
              Log.debug(b.toString());
            }
            retval = true;
          }
        }
        return retval;

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException ignore) {
            //ignore.printStackTrace();
          }
        }
      }
    }
    return retval;
  }




  /**
  * Determine if a particular schema exists in the database.
  * 
  * @param name The name of the schema for which to query
  * 
  * @return true the named schema exists, false the schema does not exist.
  */
  public static boolean schemaExists(String name, Connection conn) {
    boolean retval = false;
    if (StringUtil.isNotBlank(name)) {
      if (conn == null) {
        Log.error("Cannot get connection");
        return false;
      }

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = conn.getMetaData();

        rs = meta.getSchemas();
        while (rs.next()) {
          String tableSchema = rs.getString(1);
          String tableCatalog = rs.getString(2);
          if (StringUtil.equalsIgnoreCase(name, tableSchema)) {
            Log.debug("Found schema '" + tableSchema + "' in catalog '" + tableCatalog + "'");
            retval = true;
          }
        }
        return retval;

      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (rs != null) {
          try {
            rs.close();
          } catch (SQLException ignore) {
            //ignore.printStackTrace();
          }
        }
      }
    }
    return retval;
  }




  /**
   * Retrieve the name of the user connected to the database with the given 
   * connection.
   * 
   * <p>Not all drivers support the ability to return the name of the user so
   * the return value may be null, blank or empty.
   * 
   * @param connection the connection to query
   * 
   * @return the name of the user or null if the underlying connection driver 
   *         does not support this feature.
   */
  public static String getUserName(Connection connection) {
    String retval = null;
    try {
      DatabaseMetaData meta = connection.getMetaData();
      String username = meta.getUserName();
      if (StringUtil.isNotBlank(username)) {
        retval = username;
      }
    } catch (SQLException e) {
      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Could not get database product name: " + e.getClass().getName() + " - " + e.getMessage());
      }
    }
    return retval;
  }

}
