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
package coyote.commons.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;
import coyote.dx.db.ColumnDefinition;
import coyote.dx.db.ColumnType;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.TableDefinition;
import coyote.loader.log.Log;


/**
 * 
 */
public class DatabaseUtil {

  /**
   * Determine if a particular table exists in the database.
   * 
   * @param tablename The name of the table for which to query
   * 
   * @return true the named table exists, false the table does not exist.
   */
  public static boolean tableExists( Connection connection, String tablename ) {
    boolean retval = false;
    if ( StringUtil.isNotBlank( tablename ) ) {
      if ( connection == null ) {
        throw new IllegalArgumentException( "Null connection argument" );
      }

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = connection.getMetaData();
        rs = meta.getTables( null, null, "%", null );
        while ( rs.next() ) {
          String table = rs.getString( "TABLE_NAME" );
          if ( tablename.equalsIgnoreCase( table ) ) {
            retval = true;
          }
        }
        return retval;
      } catch ( SQLException e ) {
        e.printStackTrace();
      }
      finally {
        if ( rs != null ) {
          try {
            rs.close();
          } catch ( SQLException ignore ) {
            //ignore.printStackTrace();
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
  public static TableDefinition getTableSchema( Connection connection, String tablename ) {
    TableDefinition retval = null;
    if ( StringUtil.isNotBlank( tablename ) ) {
      if ( connection == null ) {
        throw new IllegalArgumentException( "Null connection argument" );
      }

      String tableSchemaName = null;

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = connection.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables( null, null, "%", null );
        while ( rs.next() ) {
          if ( tablename.equalsIgnoreCase( rs.getString( "TABLE_NAME" ) ) ) {
            tableSchemaName = rs.getString( "TABLE_NAME" );
            break;
          }
        }
      } catch ( SQLException e ) {
        e.printStackTrace();
      }
      finally {
        if ( rs != null ) {
          try {
            rs.close();
          } catch ( SQLException ignore ) {}
        }
      }

      if ( StringUtil.isNotEmpty( tableSchemaName ) ) {
        retval = new TableDefinition( tableSchemaName );

        rs = null;
        try {
          DatabaseMetaData meta = connection.getMetaData();

          String product = meta.getDatabaseProductName();
          if ( StringUtil.isNotBlank( product ) ) {
            retval.setProductName( product.toUpperCase() );
          }
          retval.setProductVersion( meta.getDatabaseProductVersion() );
          retval.setMajorVersion( meta.getDatabaseMajorVersion() );
          retval.setMinorVersion( meta.getDatabaseMinorVersion() );

          rs = meta.getColumns( null, null, tableSchemaName, "%" );

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

          while ( rs.next() ) {
            readOnly = nullable = mandatory = primaryKey = unique = false;
            length = pos = 0;
            name = remarks = null;

            if ( rs.getString( "TABLE_CAT" ) != null && retval.getCatalogName() == null ) {
              retval.setCatalogName( rs.getString( "TABLE_CAT" ) );
            }

            if ( rs.getString( "TABLE_SCHEM" ) != null && retval.getSchemaName() == null ) {
              retval.setSchemaName( rs.getString( "TABLE_SCHEM" ) );
            }

            name = rs.getString( "COLUMN_NAME" );
            length = rs.getInt( "COLUMN_SIZE" );
            pos = rs.getInt( "ORDINAL_POSITION" );
            remarks = rs.getString( "REMARKS" );

            switch ( rs.getInt( "DATA_TYPE" ) ) {
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
            }

            switch ( rs.getInt( "NULLABLE" ) ) {
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
            }
            retval.addColumn( new ColumnDefinition( name, type, length, nullable, readOnly, mandatory, primaryKey, unique, remarks, pos ) );
          }

        } catch ( SQLException e ) {
          e.printStackTrace();
        }
        finally {
          if ( rs != null ) {
            try {
              rs.close();
            } catch ( SQLException ignore ) {}
          }
        }
      }
    }
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
  public static DataFrame readRecord( Connection connection, String query ) {
    DataFrame retval = null;

    ResultSet result = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;

    Log.debug( String.format( "Executing query: '%s'", query ) );

    if ( connection != null ) {

      try {
        Statement statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
        result = statement.executeQuery( query );

        rsmd = result.getMetaData();

        columnCount = rsmd.getColumnCount();

        if ( result != null ) {
          try {
            if ( result.next() ) {
              retval = new DataFrame();
              for ( int i = 1; i <= columnCount; i++ ) {
                retval.add( rsmd.getColumnName( i ), DatabaseDialect.resolveValue( result.getObject( i ), rsmd.getColumnType( i ) ) );
              }
            } else {
              Log.error( "Read past EOF" );
              return retval;
            }
          } catch ( SQLException e ) {
            e.printStackTrace();
          }
        }
      } catch ( SQLException e ) {
        String emsg = String.format( "Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query );
        Log.error( emsg );
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
  public static FrameSet readAllRecords( Connection connection, String query ) {
    FrameSet retval = new FrameSet();

    ResultSet result = null;
    ResultSetMetaData rsmd = null;
    int columnCount = 0;

    Log.debug( String.format( "Executing query: '%s'", query ) );

    if ( connection != null ) {

      try {
        Statement statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
        result = statement.executeQuery( query );
        rsmd = result.getMetaData();
        columnCount = rsmd.getColumnCount();

        if ( result != null ) {
          try {
            while ( result.next() ) {
              DataFrame record = new DataFrame();
              for ( int i = 1; i <= columnCount; i++ ) {
                // Log.debug( rsmd.getColumnName( i ) + " - '" + result.getString( i ) + "' (" + rsmd.getColumnType( i ) + ")" );
                record.add( rsmd.getColumnName( i ), DatabaseDialect.resolveValue( result.getObject( i ), rsmd.getColumnType( i ) ) );
              }
              retval.add( record );
            }
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      } catch ( SQLException e ) {
        String emsg = String.format( "Error querying database: '%s' - query = '%s'", e.getMessage().trim(), query );
        Log.error( emsg );
      }

    }
    return retval;
  }

}
