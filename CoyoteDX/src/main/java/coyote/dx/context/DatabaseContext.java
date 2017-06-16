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
package coyote.dx.context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.Database;
import coyote.dx.Symbols;
import coyote.dx.db.ColumnDefinition;
import coyote.dx.db.ColumnType;
import coyote.dx.db.DatabaseDialect;
import coyote.dx.db.TableDefinition;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is an operational context backed by a database which allows values to 
 * be persisted on remote systems.
 * 
 * <p>The data in the context can be managed externally without regard to 
 * where the transform is being run, allowing transforms to be run on many 
 * different hosts without having to manage locally persisted context data.
 * 
 * <p>Key value pairs specified in the fields section are used to reset the 
 * field values in the database context at the start of the job. Their values 
 * will be persisted when the context is closed. Other jobs using the context 
 * will then have access to these values unless they reset them in a similar 
 * fashion. 
 * 
 * <p>The primary use case is the running of transform jobs in a pool of 
 * distributed instances in the cloud. A particular instance will run one on 
 * one host and run another time on a different host. Another use case is 
 * running jobs in virtual machines with ephemeral file systems such as Heroku. 
 * The VM is restarted at least daily with a fresh file system and all local 
 * files are lost. This context allows persistent data to be stored remotely 
 * so that local file access is not required.
 * 
 * <p>Unlike a writer, this component deals with fields of a dataframe not the 
 * dataframe itself. Reach field is a record in the table differentiated by 
 * the field name and the name of the job to which it belongs.
 */
public class DatabaseContext extends PersistentContext {

  private static final String TABLE_NAME = "Context";
  private static final String SCHEMA_NAME = "DX";

  /** The JDBC connection used by this context to interact with the database */
  protected Connection connection;

  /** Component we use to handle connection creation */
  private Database database = null;

  /** The connection to the database */
  Connection conn = null;

  /** Our identity to record in the context on inserts and update operations */
  String identity = null;

  /** The list of existing fields in the database for this job */
  FrameSet existingFields = null;




  private boolean isAutoCreate() {
    return configuration.getBoolean( ConfigTag.AUTO_CREATE );
  }




  /**
   * <p>The context is one of the first components opened because all the rest 
   * of the components need an initialized/opened context to perform their 
   * functions and to share data.
   * 
   * @see coyote.dx.context.TransformContext#open()
   */
  @Override
  public void open() {

    if ( configuration != null ) {

      String name = null;

      // TODO: Optionally get database from context e.g. "database":"Oracle5" 

      if ( getEngine() == null ) {
        Log.fatal( "Context is not connected to a transform engine!" );
        setError( "No engine set in context" );
        return;
      } else {
        name = getEngine().getName();
      }

      if ( StringUtil.isNotBlank( name ) ) {
        database = new Database();
        try {
          database.setConfiguration( configuration );
          database.open( null );
          conn = database.getConnection();

          try {
            if ( !conn.isValid( 10 ) ) {
              Log.fatal( "Database connection is not valid" );
              setError( "Database connection is not valid" );
              return;
            }
          } catch ( SQLException e ) {
            Log.fatal( "Database connection is not valid" );
            setError( "Database connection is not valid" );
            return;
          }

          determineIdentity();
          verifyTables();
          readfields( name );
          incrementRunCount();
          setPreviousRunDate();
        } catch ( ConfigurationException e ) {
          e.printStackTrace();
        }

        // Any fields defined in the configuration override values in the data store
        Config section = configuration.getSection( ConfigTag.FIELDS );
        for ( DataField field : section.getFields() ) {
          if ( !field.isFrame() ) {
            if ( StringUtil.isNotBlank( field.getName() ) && !field.isNull() ) {
              String token = field.getStringValue();
              String value = Template.resolve( token, engine.getSymbolTable() );
              engine.getSymbolTable().put( field.getName(), value );
              set( field.getName(), value );
            }
          }
        }

      } // has a name
    }

  }




  /**
   * Attempt to determine what database user this context is running as so the 
   * identity of insert and update or records can be recorded. 
   */
  private void determineIdentity() {
    if ( identity == null ) {
      if ( StringUtil.isNotBlank( database.getUsername() ) ) {
        identity = database.getUsername();
      } else if ( StringUtil.isNotBlank( database.getConnectedUser() ) ) {
        identity = database.getConnectedUser();
      } else {
        identity = this.getClass().getSimpleName();
      }
    }
  }




  /**
   * Read in all the context fields from the database with the given name.
   * 
   * @param name the name of the context (i.e. job name) to query.
   */
  private void readfields( String name ) {
    Log.debug( "Reading fields for context '" + name + "' on " + database.getProductName() );
    existingFields = DatabaseUtil.readAllRecords( conn, "select * from " + SCHEMA_NAME + "." + TABLE_NAME + " where Name = '" + name + "'" );
    for ( DataFrame frame : existingFields.getRows() ) {
      Log.debug( "Read in context variable:" + frame.toString() );
      DataField keyField = frame.getFieldIgnoreCase( "Key" );
      if ( keyField != null && StringUtil.isNotBlank( keyField.getStringValue() ) ) {
        DataField valueField = frame.getFieldIgnoreCase( "Value" );
        if ( valueField != null && valueField.isNotNull() ) {
          DataField typeField = frame.getFieldIgnoreCase( "Type" );
          if ( typeField != null && typeField.isNotNull() ) {
            Object contextValue = DataField.parse( valueField.getStringValue(), (short)typeField.getObjectValue() );
            if ( contextValue != null ) {
              set( keyField.getStringValue(), contextValue );
            } else {
              set( keyField.getStringValue(), valueField.getStringValue() );
            }
          } else {
            set( keyField.getStringValue(), valueField.getStringValue() );
          }
        }
      }
    }
  }




  /**
   * Create the table necessary to store named values for a named job.
   * 
   * <p>This should not be the final table; it should be reviewed and altered 
   * by the DBA to meet the needs of the application. This is to help ensure 
   * quick deployment and operation only, not production use.
   */
  private void createTables() {

    String sql = DatabaseDialect.getCreateSchema( database.getProductName(), SCHEMA_NAME, database.getUsername() );
    Log.debug( "Creating table in database..." );
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate( sql );
      Log.debug( "Schema created." );
    } catch ( SQLException e ) {
      Log.error( "Schema creation failed!" );
      e.printStackTrace();
    }

    TableDefinition tdef = new TableDefinition( TABLE_NAME );
    tdef.setSchemaName( SCHEMA_NAME );
    tdef.addColumn( new ColumnDefinition( "SysId", ColumnType.STRING ).setLength( 36 ).setPrimaryKey( true ) );
    tdef.addColumn( new ColumnDefinition( "Name", ColumnType.STRING ).setLength( 64 ) );
    tdef.addColumn( new ColumnDefinition( "Key", ColumnType.STRING ).setLength( 64 ) );
    tdef.addColumn( new ColumnDefinition( "Value", ColumnType.STRING ).setLength( 255 ).setNullable( true ) );
    tdef.addColumn( new ColumnDefinition( "Type", ColumnType.SHORT ).setNullable( true ) );
    tdef.addColumn( new ColumnDefinition( "CreatedBy", ColumnType.STRING ).setLength( 32 ) );
    tdef.addColumn( new ColumnDefinition( "CreatedOn", ColumnType.DATE ) );
    tdef.addColumn( new ColumnDefinition( "ModifiedBy", ColumnType.STRING ).setLength( 32 ) );
    tdef.addColumn( new ColumnDefinition( "ModifiedOn", ColumnType.DATE ) );

    sql = DatabaseDialect.getCreate( database.getProductName(), tdef );

    Log.debug( "Creating table in database..." );
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate( sql );
      Log.debug( "Table created." );
    } catch ( SQLException e ) {
      Log.error( "Table creation failed!" );
      e.printStackTrace();
    }
  }




  /**
   * Make sure the tables exist.
   */
  private void verifyTables() {
    if ( !DatabaseUtil.tableExists( conn, TABLE_NAME ) ) {
      if ( isAutoCreate() ) {
        createTables();
      }
    }
  }




  /**
   * @see coyote.dx.context.TransformContext#close()
   */
  @Override
  public void close() {
    DataFrame frame = new DataFrame();
    for ( String key : properties.keySet() ) {
      try {
        frame.add( key, properties.get( key ) );
      } catch ( Exception e ) {
        Log.debug( "Cannot persist property '" + key + "' - " + e.getMessage() );
      }
    }

    frame.put( Symbols.RUN_COUNT, runcount );

    Object rundate = get( Symbols.DATETIME );
    if ( rundate != null ) {
      if ( rundate instanceof Date ) {
        frame.put( Symbols.PREVIOUS_RUN_DATETIME, rundate );
      } else {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Context.run_date_reset", rundate ) );
      }
    }

    upsertFields( conn, TABLE_NAME, frame );

    super.close();
  }




  @SuppressWarnings("unchecked")
  private void upsertFields( Connection conn, String tableName, DataFrame frame ) {
    SymbolTable sqlsymbols = new SymbolTable();
    sqlsymbols.put( DatabaseDialect.DB_SCHEMA_SYM, SCHEMA_NAME );
    sqlsymbols.put( DatabaseDialect.TABLE_NAME_SYM, TABLE_NAME );

    String sql = null;

    for ( DataField field : frame.getFields() ) {
      DataFrame existingFrame = existingFields.getFrameByColumnValue( "Key", field.getName() );
      if ( existingFrame != null ) {
        DataField sysIdField = existingFrame.getFieldIgnoreCase( "SysId" );
        if ( sysIdField != null ) {
          String existingValue = null;
          DataField valueField = existingFrame.getFieldIgnoreCase( "Value" );
          if ( valueField != null ) {
            existingValue = valueField.getStringValue();
          }
          // Only update if the value is different
          if ( !field.getStringValue().equals( existingValue ) ) {
            Log.debug( "Field:" + field.getName() + " was '" + existingValue + "' and now is '" + field.getStringValue() + "'" );

            sqlsymbols.put( DatabaseDialect.FIELD_MAP_SYM, "Value=?, Type=?, ModifiedBy=?, ModifiedOn=?" );
            sqlsymbols.put( DatabaseDialect.SYS_ID_SYM, sysIdField.getStringValue() );
            sql = DatabaseDialect.getSQL( database.getProductName(), DatabaseDialect.UPDATE, sqlsymbols );

            try {
              PreparedStatement preparedStatement = conn.prepareStatement( sql );
              if ( field.getType() == DataField.DATE ) {
                preparedStatement.setString( 1, new SimpleDateFormat( CDX.DEFAULT_DATETIME_FORMAT ).format( (Date)field.getObjectValue() ) );
              } else {
                preparedStatement.setString( 1, field.getStringValue() );
              }
              preparedStatement.setInt( 2, field.getType() );
              preparedStatement.setString( 3, identity );
              preparedStatement.setTimestamp( 4, new java.sql.Timestamp( new Date().getTime() ) );
              int rowsAffected = preparedStatement.executeUpdate();
            } catch ( SQLException e ) {
              e.printStackTrace();
            }
          }
        } else {
          Log.error( "Existing field does not contain a sysid: " + existingFrame.toString() );
          insertField( field, sqlsymbols );
        }
      } else {
        insertField( field, sqlsymbols );
      }
    }
  }




  @SuppressWarnings("unchecked")
  private void insertField( DataField field, SymbolTable sqlsymbols ) {
    sqlsymbols.put( DatabaseDialect.FIELD_NAMES_SYM, "SysId, Name, Key, Value, Type, CreatedBy, CreatedOn, ModifiedBy, ModifiedOn" );
    sqlsymbols.put( DatabaseDialect.FIELD_VALUES_SYM, "?, ?, ?, ?, ?, ?, ?, ?, ?" );
    String sql = DatabaseDialect.getSQL( database.getProductName(), DatabaseDialect.INSERT, sqlsymbols );
    try {
      PreparedStatement preparedStatement = conn.prepareStatement( sql );
      preparedStatement.setString( 1, UUID.randomUUID().toString() );
      preparedStatement.setString( 2, getEngine().getName() );
      preparedStatement.setString( 3, field.getName() );
      preparedStatement.setString( 4, field.getStringValue() );
      preparedStatement.setInt( 5, field.getType() );
      preparedStatement.setString( 6, identity );
      preparedStatement.setTimestamp( 7, new java.sql.Timestamp( new Date().getTime() ) );
      preparedStatement.setString( 8, identity );
      preparedStatement.setTimestamp( 9, new java.sql.Timestamp( new Date().getTime() ) );
      int rowsAffected = preparedStatement.executeUpdate();
    } catch ( SQLException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
