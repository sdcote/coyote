/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import coyote.batch.Batch;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This class holds the mapping of different database types to DataFrame types 
 * so tables can be created in different databases for batch data.
 */
public class DatabaseDialect {


  // The symbols we expect to find in the symbol table
  //  private static final String JOB_NAME_SYM = "jobName";
  public static final String TABLE_NAME_SYM = "tableName";
  //  private static final String DB_USER_SYM = "dbUser";
  //  private static final String CLASS_SYM = "fullClassname";
  //  private static final String CLASS_BASE_SYM = "classname";
  public static final String DATABASE_SYM = "database";
  public static final String DATABASE_VERSION_SYM = "databaseVersion";
  public static final String DATABASE_MAJOR_SYM = "dbMajorVersion";
  public static final String DATABASE_MINOR_SYM = "dbMinorVersion";

  
  public static final String CREATE = "create";
  public static final String GRANT = "grant";
  public static final String INSERT = "insert";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  public static final String TRUNCATE = "truncate";
  public static final String TIMESTAMP = "timestamp";

  // Database Technologies supported
  public static final String ORACLE = "Oracle";
  public static final String MYSQL = "MySQL";
  public static final String H2 = "H2";

  private static final String DEFAULT = "default";

  private static final Map<String, Map<String, String>> TYPES = new HashMap<String, Map<String, String>>();
  private static final Map<String, Map<String, String>> SYNTAX = new HashMap<String, Map<String, String>>();

  static {
    // MySQL
    Map<String, String> map = new HashMap<String, String>();
    TYPES.put( MYSQL, map );
    map.put( "STR", "VARCHAR(#)" );
    map.put( "BOL", "TINYINT" );
    map.put( "S8", "TINYINT" );
    map.put( "U8", "TINYINT" );
    map.put( "S16", "INTEGER" );
    map.put( "U16", "INTEGER" );
    map.put( "S32", "INTEGER" );
    map.put( "U32", "INTEGER" );
    map.put( "S64", "INTEGER" );
    map.put( "U64", "INTEGER" );
    map.put( "DBL", "DOUBLE" );
    map.put( "FLT", "DOUBLE" );
    map.put( DEFAULT, "VARCHAR(#)" );
    map = new HashMap<String, String>();
    SYNTAX.put( MYSQL, map );
    map.put( CREATE, "CREATE TABLE [#$dbUser#].[#$tableName#] ( [#$fielddefinitions#], PRIMARY KEY (\"sys_id\"))" );
    map.put( GRANT, "" );
    map.put( INSERT, "INSERT INTO [#$dbUser#].[#$tableName#] ( [#$fieldnames#] ) VALUES ($fieldvalues)" );
    map.put( UPDATE, "UPDATE [#$dbUser#].[#$tableName#] SET $fieldmap WHERE \"sys_id\" = [#$keyvalue#]" );
    map.put( DELETE, "DELETE FROM [#$dbUser#].[#$tableName#] WHERE \"sys_id\" = [#$keyvalue#]" );
    map.put( TRUNCATE, "TRUNCATE TABLE [#$dbUser#].[#$tableName#]" );
    map.put( TIMESTAMP, "SELECT \"sys_updated_on\" FROM [#$dbUser#].[#$tableName#] WHERE \"sys_id\" = [#$keyvalue#]" );

    // Oracle dialect
    map = new HashMap<String, String>();
    TYPES.put( ORACLE, map );
    map.put( "STRING", "VARCHAR2(#)" );
    map.put( "BOOLEAN", "NUMBER(1)" );
    map.put( "INTEGER", "NUMBER(10)" );
    map.put( "NUMERIC", "NUMBER(10)" );
    map.put( "FLOAT", "NUMBER" );
    map.put( "DECIMAL", "NUMBER" );
    map.put( DEFAULT, "VARCHAR2(#)" );
    map = new HashMap<String, String>();
    SYNTAX.put( H2, map );
    map.put( CREATE, "CREATE TABLE [#$dbUser#].[#$tableName#] ( [#$fielddefinitions#], CONSTRAINT [#$tableName#]_PK PRIMARY KEY (SYS_ID))" );
    map.put( GRANT, "GRANT SELECT,REFERENCES ON [#$dbUser#].[#$tableName#] TO PUBLIC" );
    map.put( INSERT, "INSERT INTO [#$dbUser#].[#$tableName#] ( [#$fieldnames#] VALUES [#$fieldvalues#])" );
    map.put( UPDATE, "UPDATE [#$dbUser#].[#$tableName#] SET [#$fieldmap#] WHERE SYS_ID=[#$keyvalue#]" );
    map.put( DELETE, "DELETE FROM [#$dbUser#].[#$tableName#] WHERE SYS_ID=[#$keyvalue#]" );
    map.put( TRUNCATE, "TRUNCATE TABLE [#$dbUser#].[#$tableName#]" );
    map.put( TIMESTAMP, "SELECT SYS_UPDATED_ON FROM [#$dbUser#].[#$tableName#] WHERE SYS_ID=[#$keyvalue#]" );

    // H2 Dialect
    map = new HashMap<String, String>();
    TYPES.put( H2, map );
    map.put( "STR", "VARCHAR(#)" );
    map.put( "BOL", "BOOLEAN" );
    map.put( "S8", "TINYINT" );
    map.put( "U8", "TINYINT" );
    map.put( "S16", "SMALLINT" );
    map.put( "U16", "SMALLINT" );
    map.put( "S32", "INTEGER" );
    map.put( "U32", "INTEGER" );
    map.put( "S64", "BIGINT" );
    map.put( "U64", "BIGINT" );
    map.put( "DBL", "DOUBLE" );
    map.put( "DAT", "TIMESTAMP" );
    map.put( "FLT", "REAL" );
    map.put( DEFAULT, "VARCHAR(#)" );
    map = new HashMap<String, String>();
    SYNTAX.put( H2, map );
    map.put( CREATE, "CREATE TABLE [#$tableName#]( [#$fielddefinitions#] )" );
    map.put( GRANT, "" );
    map.put( INSERT, "INSERT INTO [#$tableName#] ( [#$fieldnames#] VALUES ([#$fieldvalues#])" );
    map.put( UPDATE, "UPDATE [#$tableName#] SET [#$fieldmap#] WHERE SYS_ID=[#$keyvalue#]" );
    map.put( DELETE, "DELETE FROM [#$tableName#] WHERE SYS_ID=[#$keyvalue#]" );
    map.put( TRUNCATE, "TRUNCATE TABLE [#$tableName#]" );
    map.put( TIMESTAMP, "SELECT SYS_UPDATED_ON FROM [#$tableName#] WHERE SYS_ID=[#$keyvalue#]" );
    //map.put( CONFIG_CREATE, "CREATE TABLE [#$tableName#] (JOB_NAME VARCHAR(64) NOT NULL, TABLE_NAME VARCHAR(64) NOT NULL, JOB_TYPE VARCHAR(8), URL VARCHAR(64), PATH VARCHAR(64), ENDPOINT VARCHAR(64), IS_ACTIVE BOOLEAN, USERNAME VARCHAR(64), CREDENTIAL VARCHAR(64), QUERY VARCHAR(64), PARAMETERS VARCHAR(64), PURGE BOOLEAN, BATCH_SIZE INTEGER )" );
    //map.put( CONFIG_INDEX, "ALTER TABLE [#$tableName#] ADD CONSTRAINT PK_[#$tableName#] PRIMARY KEY (JOB_NAME, TABLE_NAME)" );
    //map.put( CONFIG_SELECT, "SELECT JOB_NAME, TABLE_NAME, JOB_TYPE, URL, PATH, ENDPOINT, IS_ACTIVE, USERNAME, CREDENTIAL, QUERY, PARAMETERS, PURGE, BATCH_SIZE FROM [#$tableName#] WHERE JOB_NAME = '[#$jobName#]'" );
  }




  /**
   * Retrieve the syntax for a command for a particular database product.
   * 
   * <p>The result will be a template of the command and the caller will need 
   * to substitute specific tokens for intended values.</p>
   * 
   * <p>Hint: Use the static constants of this class to ensure conformity.</p>
   * 
   * @param database The database product being used
   * @param command the command to retrieve
   * @param symbols symbols to be used in resolving template variables
   * 
   * @return the command template or null if the database is not supported or the command is not recognized.
   */
  public static String getSQL( String database, String command, SymbolTable symbols ) {
    // find the map of commands for this database
    Map<String, String> map = SYNTAX.get( database );

    // If this database is supported
    if ( map != null ) {
      // lookup the requested command in this databases map of commands
      String cmdstr = map.get( command );

      if ( StringUtil.isNotBlank( cmdstr ) ) {

        // If we have a symbol table,
        if ( symbols != null ) {

          // resolve the variables in the command string
          Template tmplt = new Template( cmdstr, symbols );
          return tmplt.toString();

        } else {

          //...otherwise just return the raw template
          return cmdstr;
        }
      }
    }
    return null;
  }




  /**
   * Generate the proper SQL command to create a table which will hold data 
   * described by the given schema.
   * 
   * <p>A {@code Template} is used to handle variable substitution. If the 
   * {@code SymbolTable} is null, then the template will not be evaluated and 
   * returned to the caller as a raw template which the caller must process 
   * before using as a valid SQL statement. This can be helpful during the 
   * development process to see what symbols should be provided in the 
   * table.</p>
   * 
   * @param database the name of the database dialect to use (e.g. Oracle, MySQL, etc.)
   * @param schema the schema of the records to be stored
   * @param symbols the table of symbol values to use in variable substitution
   * 
   * @return a template string which can be used to generate the
   * 
   * @see coyote.commons.template.Template
   */
  @SuppressWarnings("unchecked")
  public static String getCreate( String database, Schema schema, SymbolTable symbols ) {

    Map<String, String> typeMap = TYPES.get( database );
    if ( typeMap != null ) {

      StringBuffer b = new StringBuffer();

      for ( Entry<String, String> entry : typeMap.entrySet() ) {
        Log.trace( String.format( "DB: \"%s\",\"%s\"", entry.getKey(), entry.getValue() ) );
        //Log.trace( LogMsg.createMsg( Batch.MSG, "Writer.connected_to",getClass().getName(), getTarget() ) );
      }

      for ( FieldMetrics metrics : schema.getMetrics() ) {

        final String fieldname = metrics.getName();
        final String fieldtype = metrics.getType();
        final long fieldlen = metrics.getMaximumStringLength();

        Log.debug( String.format( "SN: \"%s\",\"%s\",%d", fieldname, fieldtype, fieldlen ) );

        b.append( fieldname );
        b.append( " " );

        // lookup the database type to use for this field
        String type = null;

        if ( StringUtil.isNotBlank( fieldtype ) )
          type = typeMap.get( fieldtype.toUpperCase() );

        if ( StringUtil.isBlank( type ) ) {
          Log.warn( LogMsg.createMsg( Batch.MSG, "Database.could_not_find_type", DatabaseDialect.class.getName(), fieldtype, typeMap.get( DEFAULT ), fieldname ) );
          type = typeMap.get( DEFAULT );
        }

        //Replace any required length tokens
        if ( type.indexOf( '#' ) > -1 ) {
          type = type.replace( "#", Long.toString( fieldlen ) );
        }

        // place the field and type in the buffer 
        b.append( type );
        b.append( ", " );
      }

      // if there is a symbol table, place the field definitions in it
      if ( symbols != null ) {
        symbols.put( "fielddefinitions", b.toString() );
      }

      // now have return the create command using the field definitions in the 
      // symbol table to fill out the command template
      return getSQL( database, CREATE, symbols );

    } else {
      Log.error( LogMsg.createMsg( Batch.MSG, "Could not find type definition for '{}' database", database ) );
    }

    return null;

  }

}
