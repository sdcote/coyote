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
package coyote.batch.writer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameWriter;
import coyote.batch.TransformContext;
import coyote.batch.eval.EvaluationException;
import coyote.batch.schema.DatabaseDialect;
import coyote.batch.schema.Schema;
import coyote.commons.JdbcUtil;
import coyote.commons.StringUtil;
import coyote.commons.jdbc.DriverDelegate;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dataframe.FrameSet;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public class JdbcWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  protected static final SymbolTable symbolTable = new SymbolTable();
  private Schema schema = new Schema();
  protected Connection connection;
  private String database = null;

  protected int batchsize = 0;
  protected final FrameSet frameset = new FrameSet();
  protected String SQL = null;
  protected PreparedStatement ps = null;





  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

    if ( frameset.size() > 0 ) {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.completing_batch",getClass().getName(), frameset.size() ) );
      writeBatch();
    }

    if ( ps != null ) {
      try {
        ps.close();
      } catch ( final SQLException e ) {
        Log.error( LogMsg.createMsg( Batch.MSG, "Writer.Could not close prepared statememt: {%s}", e.getMessage() ) );
      }
    }

    if ( connection != null ) {
      try {
        commit();
      } catch ( final SQLException e ) {
        Log.error( LogMsg.createMsg( Batch.MSG, "Writer.Could not commit prior to close: {%s}", e.getMessage() ) );
      }

      // if it looks like we created the connection ourselves (e.g. we have a 
      // configured target) close the connection
      if ( StringUtil.isNotBlank( getTarget() ) ) {
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.closing_connection",getClass().getName(), getTarget() ) );

        try {
          connection.close();
        } catch ( final SQLException e ) {
          Log.error( LogMsg.createMsg( Batch.MSG, "Writer.Could not close connection cleanly: {%s}", e.getMessage() ) );
        }
      }
    }
  }




  public void commit() throws SQLException {
    connection.commit();
  }




  /**
   * @return the insert SQL appropriate for this frameset
   */
  private String generateSQL() {
    final StringBuffer c = new StringBuffer( "insert into " );
    final StringBuffer v = new StringBuffer();

    c.append( getTable() );
    c.append( " (" );
    for ( final String name : frameset.getColumns() ) {
      c.append( name );
      c.append( ", " );
      v.append( "?, " );
    }
    c.delete( c.length() - 2, c.length() );
    v.delete( v.length() - 2, v.length() );

    c.append( ") values (" );
    c.append( v.toString() );
    c.append( ")" );

    return c.toString();
  }




  public int getBatchSize() {
    try {
      return configuration.getAsInt( ConfigTag.BATCH );
    } catch ( final DataFrameException ignore ) {}
    return 0;
  }




  @SuppressWarnings("unchecked")
  private Connection getConnection() {

    if ( connection == null ) {
      // get the connection to the database
      try {
        final URL u = new URL( getLibrary() );
        final URLClassLoader ucl = new URLClassLoader( new URL[] { u } );
        final Driver driver = (Driver)Class.forName( getDriver(), true, ucl ).newInstance();
        DriverManager.registerDriver( new DriverDelegate( driver ) );

        connection = DriverManager.getConnection( getTarget(), getUsername(), getPassword() );

        if ( connection != null ) {
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.connected_to", getClass().getName(), getTarget() ) );
          DatabaseMetaData meta = connection.getMetaData();

          // get the product name 
          String product = meta.getDatabaseProductName();
          // save it for later
          database = product.toUpperCase();

          // update the symbols with database information
          symbolTable.put( DatabaseDialect.DATABASE_SYM, product );
          symbolTable.put( DatabaseDialect.DATABASE_VERSION_SYM, meta.getDatabaseProductVersion() );
          symbolTable.put( DatabaseDialect.DATABASE_MAJOR_SYM, meta.getDatabaseMajorVersion() );
          symbolTable.put( DatabaseDialect.DATABASE_MINOR_SYM, meta.getDatabaseMinorVersion() );

          // log debug information about the database
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.connected_to_product", getClass().getName(), meta.getDatabaseProductName(), meta.getDatabaseProductVersion(), meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion() ) );

        }
      } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
        getContext().setError( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
      }
    }
    return connection;
  }




  public String getDriver() {
    return configuration.getAsString( ConfigTag.DRIVER );
  }




  public String getLibrary() {
    return configuration.getAsString( ConfigTag.LIBRARY );
  }




  public String getPassword() {
    return configuration.getAsString( ConfigTag.PASSWORD );
  }




  public String getTable() {
    return configuration.getAsString( ConfigTag.TABLE );
  }




  public String getUsername() {
    return configuration.getAsString( ConfigTag.USERNAME );
  }




  public boolean isAutoCreate() {
    try {
      return configuration.getAsBoolean( ConfigTag.AUTO_CREATE );
    } catch ( final DataFrameException ignore ) {}
    return false;
  }




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    super.setContext( context );

    // If we don't have a connection, prepare to create one
    if ( connection == null ) {
      // get our configuration data
      setTarget( getString( ConfigTag.TARGET ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_target", getClass().getName(), getTarget() ) );

      setTable( getString( ConfigTag.TABLE ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_table", getClass().getName(), getTable() ) );

      setUsername( getString( ConfigTag.USERNAME ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_user", getClass().getName(), getUsername() ) );

      setPassword( getString( ConfigTag.PASSWORD ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_password", getClass().getName(), StringUtil.isBlank( getPassword() ) ? 0 : getPassword().length() ) );

      setDriver( getString( ConfigTag.DRIVER ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_driver", getClass().getName(), getDriver() ) );

      setAutoCreate( getBoolean( ConfigTag.AUTO_CREATE ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.autocreate_tables", getClass().getName(), isAutoCreate() ) );

      setBatchSize( getInteger( ConfigTag.BATCH ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_batch_size", getClass().getName(), getBatchSize() ) );

      setLibrary( getString( ConfigTag.LIBRARY ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_driver", getClass().getName(), getLibrary() ) );

    } else {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_existing_connection", getClass().getName() ) );
    }

    // validate and cache our batch size
    if ( getBatchSize() < 1 ) {
      batchsize = 0;
    } else {
      batchsize = getBatchSize();
    }

  }




  /**
   * @param value
   */
  public void setAutoCreate( final boolean value ) {
    configuration.put( ConfigTag.AUTO_CREATE, value );
  }




  /**
   * @param value
   */
  private void setBatchSize( final int value ) {
    configuration.put( ConfigTag.BATCH, value );
  }




  /**
   * @param conn
   */
  public void setConnection( final Connection conn ) {
    connection = conn;
  }




  /**
   * @param pstmt the prepared statement to which to add data
   * @param indx the index into the value set 
   * @param field the field containing the value to add
   */
  private void setData( final PreparedStatement pstmt, final int indx, final DataField field ) {
    final short type = field.getType();
    try {
      switch ( type ) {
        case DataField.FRAMETYPE:
          getContext().setError( "Cannot add complex objects to table" );
          break;
        case DataField.UDEF:
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.VARCHAR );
          } else {
            pstmt.setString( indx, "" );
          }
          break;
        case DataField.BYTEARRAY:
          getContext().setError( "Cannot add byte arrays to table" );
          break;
        case DataField.STRING:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "String" ) );
          pstmt.setString( indx, field.getStringValue() );
          break;
        case DataField.S8:
        case DataField.U8:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S8-byte" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.TINYINT );
          } else {
            pstmt.setByte( indx, (byte)field.getObjectValue() );
          }
          break;
        case DataField.S16:
        case DataField.U16:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S16-Short" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.SMALLINT );
          } else {
            pstmt.setShort( indx, (Short)field.getObjectValue() );
          }
          break;
        case DataField.S32:
        case DataField.U32:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S32-Integer" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.INTEGER );
          } else {
            pstmt.setInt( indx, (Integer)field.getObjectValue() );
          }
          break;
        case DataField.S64:
        case DataField.U64:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "S64-Long" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.BIGINT );
          } else {
            pstmt.setLong( indx, (Integer)field.getObjectValue() );
          }
          break;
        case DataField.FLOAT:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Float" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.FLOAT );
          } else {
            pstmt.setFloat( indx, (Float)field.getObjectValue() );
          }
          break;
        case DataField.DOUBLE:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Double" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.DOUBLE );
          } else {
            pstmt.setDouble( indx, (Double)field.getObjectValue() );
          }
          break;
        case DataField.BOOLEANTYPE:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Boolean" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.BOOLEAN );
          } else {
            pstmt.setBoolean( indx, (Boolean)field.getObjectValue() );
          }
          break;
        case DataField.DATE:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "Timestamp" ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.TIMESTAMP );
          } else {
            final Object obj = field.getObjectValue();
            pstmt.setTimestamp( indx, JdbcUtil.getTimeStamp( (Date)obj ) );
          }
          break;
        case DataField.URI:
          Log.trace( LogMsg.createMsg( Batch.MSG, "Database.saving_field_as", getClass().getName(), field.getName(), indx, "String" ) );
          pstmt.setString( indx, field.getStringValue() );
          break;
        case DataField.ARRAY:
          getContext().setError( "Cannot add arrays to table field" );
          break;
        default:
          pstmt.setNull( indx, java.sql.Types.VARCHAR );
          break;
      }
    } catch ( final SQLException e ) {
      e.printStackTrace();
    }

  }




  /**
   * @param value
   */
  private void setDriver( final String value ) {
    configuration.put( ConfigTag.DRIVER, value );
  }




  /**
   * @param value
   */
  private void setLibrary( final String value ) {
    configuration.put( ConfigTag.LIBRARY, value );
  }




  /**
   * @param value
   */
  private void setPassword( final String value ) {
    configuration.put( ConfigTag.PASSWORD, value );
  }




  /**
   * @param value
   */
  public void setTable( final String value ) {
    configuration.put( ConfigTag.TABLE, value );
  }




  /**
   * @param value
   */
  public void setUsername( final String value ) {
    configuration.put( ConfigTag.USERNAME, value );
  }




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

    // have the schema collect data on the frame to compile metadata on frames
    schema.sample( frame );

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true...
        if ( evaluator.evaluateBoolean( expression ) ) {
          writeFrame( frame );
        }
      } catch ( final EvaluationException e ) {
        Log.warn( LogMsg.createMsg( Batch.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage() ) );
      }
    } else {
      // Unconditionally writing frame
      writeFrame( frame );
    }

  }




  private void writeBatch() {

    if ( SQL == null ) {
      // Since this is the fist time we have tried to write to the table, make
      // sure the table exists
      if ( checkTable() ) {

        SQL = generateSQL();
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.using_sql", getClass().getName(), SQL ) );

        final Connection connection = getConnection();
        try {
          ps = connection.prepareStatement( SQL );
        } catch ( final SQLException e ) {

          getContext().setError( LogMsg.createMsg( Batch.MSG, "Writer.preparedstatement_exception", getClass().getName(), e.getMessage() ).toString() );
        }
      }
    }

    if ( getContext().isNotInError() ) {
      if ( batchsize <= 1 ) {
        final DataFrame frame = frameset.get( 0 );
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing single frame {%s}", getClass().getName(), frame ) );

        int indx = 1;
        for ( final String name : frameset.getColumns() ) {
          final DataField field = frame.getField( name );
          setData( ps, indx++, field );
          if ( getContext().isInError() ) {
            break;
          }
        }

        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.executing_sql", getClass().getName(), ps.toString() ) );

        try {
          ps.execute();
        } catch ( final SQLException e ) {
          getContext().setError( "Could not insert single row: " + e.getMessage() );
        }

      } else {
        // Now write a batch
        for ( final DataFrame frame : frameset.getRows() ) {
          Log.trace( LogMsg.createMsg( Batch.MSG, "Writer.writing_frame", this.getClass().getName(), frame ) );

          int indx = 1;
          for ( final String name : frameset.getColumns() ) {
            final DataField field = frame.getField( name );
            if ( ( field != null ) && !field.isNull() ) {
              setData( ps, indx++, field );
            }
            if ( getContext().isInError() ) {
              break;
            }
          }

          // add this frame as a record to the batch
          try {
            ps.addBatch();
          } catch ( final SQLException e ) {
            getContext().setError( "Could not add the record to the batch: " + e.getMessage() );
          }

        }
        if ( getContext().isNotInError() ) {
          try {
            ps.executeBatch();
          } catch ( final SQLException e ) {
            getContext().setError( "Could not insert batch: " + e.getMessage() );
          }
        }
      }
      frameset.clearRows();
    }
  }




  /**
   * This checks the database for the table to exist.
   * 
   * <p>If the table does not exist and autocreate is set to true, this method 
   * will attempt to create the table based on the schema generated for the 
   * records observed so far.</p>
   * 
   * @return true if the table exists and is ready to insert data, false otherwise
   */
  private boolean checkTable() {

    // check to see if the table exists
    if ( !tableExists( getTable() ) ) {

      if ( isAutoCreate() ) {
        Connection conn = getConnection();

        if ( conn == null ) {
          Log.error( "Cannot get connection" );
          context.setError( "Could not connect to the database" );
          return false;
        }

        symbolTable.put( DatabaseDialect.TABLE_NAME_SYM, getTable() );
        String command = DatabaseDialect.getCreate( database, schema, symbolTable );

        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.creating_table", getClass().getName(), getTable(), command ) );

        Statement stmt = null;
        try {
          stmt = conn.createStatement();
          stmt.executeUpdate( command );

        } catch ( Exception e ) {
          Log.warn( LogMsg.createMsg( Batch.MSG, "Problems creating {} table: {}", getTable(), e.getMessage() ) );
        }
        finally {
          try {
            stmt.close();
          } catch ( Exception e ) {
            Log.warn( LogMsg.createMsg( Batch.MSG, "Problems closing create {} statement: {}", getTable(), e.getMessage() ) );
          }
        }

        //TODO getContext().setError( LogMsg.createMsg( Batch.MSG, "Writer.table_creation_exception", getClass().getName(), e.getMessage() ).toString() );
        // return false;
      }
    }

    return true;
  }




  /**
   * Determine if a particular table exists in the database.
   * 
   * @param tablename The name of the table for which to query
   * 
   * @return true the named table exists, false the table does not exist.
   */
  private boolean tableExists( String tablename ) {
    boolean retval = false;
    if ( StringUtil.isNotBlank( tablename ) ) {
      Connection conn = getConnection();
      if ( conn == null ) {
        Log.error( "Cannot get connection" );
        context.setError( "Could not connect to the database" );
        return false;
      }

      ResultSet rs = null;
      try {
        DatabaseMetaData meta = conn.getMetaData();

        // get all the tables so we can perform a case insensitive search
        rs = meta.getTables( null, null, "%", null );
        while ( rs.next() ) {
          if ( tablename.equalsIgnoreCase( rs.getString( "TABLE_NAME" ) ) ) {
            retval = true;
          }
        }
        return retval;

      } catch ( SQLException e ) {
        e.printStackTrace();
        context.setError( "Problems confirming table: " + e.getMessage() );
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
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame( final DataFrame frame ) {
    Log.trace( LogMsg.createMsg( Batch.MSG, "Writer.writing_fields", getClass().getName(), frame.size() ) );
    frameset.add( frame );

    if ( frameset.size() >= batchsize ) {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.writing_batch", getClass().getName(), frameset.size(), batchsize ) );
      writeBatch();
    }

  }

}
