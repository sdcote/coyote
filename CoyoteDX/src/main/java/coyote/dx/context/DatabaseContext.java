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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import coyote.commons.StringUtil;
import coyote.commons.jdbc.DatabaseUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.FrameSet;
import coyote.dataframe.marshal.JSONMarshaler;
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
  Connection conn = null;




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

      // TODO: Optionally get database from context
      // "database":"Oracle5" 

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
   * Read in all the context fields from the database with the given name.
   * 
   * @param name the name of the context (i.e. job name) to query.
   */
  private void readfields( String name ) {
    Log.debug( "Reading fields for context '" + name + "'" );
    FrameSet fields = DatabaseUtil.readAllRecords( conn, "select * from " + TABLE_NAME + " where Name = " + name );
    for ( DataFrame frame : fields.getRows() ) {
      System.out.println( frame.toString() );
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
    
    // TODO: Create the schema if not exists!
    // CREATE SCHEMA Quiz;
    // CREATE TABLE Quiz.Results
    DatabaseDialect.getCreateSchema( database.getProductName(), SCHEMA_NAME, database.getUsername() );

    TableDefinition tdef = new TableDefinition( TABLE_NAME );
    tdef.setSchemaName( SCHEMA_NAME );
    tdef.addColumn( new ColumnDefinition( "SysId", ColumnType.STRING ).setLength( 32 ).setPrimaryKey( true ) );
    tdef.addColumn( new ColumnDefinition( "Name", ColumnType.STRING ).setLength( 64 ) );
    tdef.addColumn( new ColumnDefinition( "Key", ColumnType.STRING ).setLength( 64 ) );
    tdef.addColumn( new ColumnDefinition( "Value", ColumnType.STRING ).setLength( 255 ).setNullable( true ) );
    tdef.addColumn( new ColumnDefinition( "Type", ColumnType.SHORT ).setNullable( true ) );
    tdef.addColumn( new ColumnDefinition( "CreatedBy", ColumnType.STRING ).setLength( 32 ) );
    tdef.addColumn( new ColumnDefinition( "CreatedOn", ColumnType.DATE ) );
    tdef.addColumn( new ColumnDefinition( "ModifiedBy", ColumnType.STRING ).setLength( 32 ) );
    tdef.addColumn( new ColumnDefinition( "ModifiedOn", ColumnType.DATE ) );

    String sql = DatabaseDialect.getCreate( database.getProductName(), tdef );
    System.out.println( sql );

    System.out.println( "Creating table in database..." );
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate( sql );
      System.out.println( "Table created." );
    } catch ( SQLException e ) {
      System.out.println( "Table creation failed!" );
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

    Log.debug( "Closing context:\n" + JSONMarshaler.toFormattedString( frame ) );

    // TODO: Upsert the frame
    // upsert( conn, TABLE_NAME, frame )

    super.close();
  }

}
