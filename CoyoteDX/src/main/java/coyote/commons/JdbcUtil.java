/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import coyote.loader.log.Log;


/**
 * 
 */
public class JdbcUtil {

  private JdbcUtil() {}




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
  * Determine if a particular table exists in the database.
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

}
