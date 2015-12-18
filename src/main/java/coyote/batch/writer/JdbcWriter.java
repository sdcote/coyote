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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameWriter;
import coyote.batch.TransformContext;
import coyote.batch.eval.EvaluationException;
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

  protected Connection connection;

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
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Completing batch size={%s}", frameset.size() ) );
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
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Closing connection to {%s}", getTarget() ) );

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
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Connected to {%s}", getTarget() ) );
        }
      } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | MalformedURLException e ) {
        Log.error( "Could not connect to database: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
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
    super.context = context;

    // If we don't have a connection, prepare to create one
    if ( connection == null ) {
      // get our configuration data
      setTarget( getString( ConfigTag.TARGET ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a target of {%s}", getTarget() ) );

      setTable( getString( ConfigTag.TABLE ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a table of {%s}", getTable() ) );

      setUsername( getString( ConfigTag.USERNAME ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a user of {%s}", getUsername() ) );

      setPassword( getString( ConfigTag.PASSWORD ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a password with a length of {%s}", StringUtil.isBlank( getPassword() ) ? 0 : getPassword().length() ) );

      setDriver( getString( ConfigTag.DRIVER ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a driver of {%s}", getDriver() ) );

      setAutoCreate( getBoolean( ConfigTag.AUTO_CREATE ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Auto Create tables = {%s}", isAutoCreate() ) );

      setBatchSize( getInteger( ConfigTag.BATCH ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a batch size of {%s}", getBatchSize() ) );

      setLibrary( getString( ConfigTag.LIBRARY ) );
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using a driver JAR of {%s}", getLibrary() ) );

    } else {
      Log.debug( "Using the existing connection" );
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
          context.setError( "Cannot add complex objects to table" );
          break;
        case DataField.UDEF:
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.VARCHAR );
          } else {
            pstmt.setString( indx, "" );
          }
          break;
        case DataField.BYTEARRAY:
          context.setError( "Cannot add byte arrays to table" );
          break;
        case DataField.STRING:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a String", field.getName(), indx ) );
          pstmt.setString( indx, field.getStringValue() );
          break;
        case DataField.S8:
        case DataField.U8:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a S8-byte", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.TINYINT );
          } else {
            pstmt.setByte( indx, (byte)field.getObjectValue() );
          }
          break;
        case DataField.S16:
        case DataField.U16:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as an S16-Short", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.SMALLINT );
          } else {
            pstmt.setShort( indx, (Short)field.getObjectValue() );
          }
          break;
        case DataField.S32:
        case DataField.U32:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a S32-Integer", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.INTEGER );
          } else {
            pstmt.setInt( indx, (Integer)field.getObjectValue() );
          }
          break;
        case DataField.S64:
        case DataField.U64:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a S64-Long", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.BIGINT );
          } else {
            pstmt.setLong( indx, (Integer)field.getObjectValue() );
          }
          break;
        case DataField.FLOAT:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a Float", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.FLOAT );
          } else {
            pstmt.setFloat( indx, (Float)field.getObjectValue() );
          }
          break;
        case DataField.DOUBLE:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a Double", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.DOUBLE );
          } else {
            pstmt.setDouble( indx, (Double)field.getObjectValue() );
          }
          break;
        case DataField.BOOLEANTYPE:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a Boolean", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.BOOLEAN );
          } else {
            pstmt.setBoolean( indx, (Boolean)field.getObjectValue() );
          }
          break;
        case DataField.DATE:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a Timestamp", field.getName(), indx ) );
          if ( field.isNull() ) {
            pstmt.setNull( indx, java.sql.Types.TIMESTAMP );
          } else {
            final Object obj = field.getObjectValue();
            pstmt.setTimestamp( indx, JdbcUtil.getTimeStamp( (Date)obj ) );
          }
          break;
        case DataField.URI:
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Saving {%s} (idx{%s}) as a String", field.getName(), indx ) );
          pstmt.setString( indx, field.getStringValue() );
          break;
        case DataField.ARRAY:
          context.setError( "Cannot add arrays to table field" );
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
      SQL = generateSQL();
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Using SQL ==> {%s}", SQL ) );

      final Connection connection = getConnection();
      try {
        ps = connection.prepareStatement( SQL );
      } catch ( final SQLException e ) {
        Log.error( LogMsg.createMsg( Batch.MSG, "Writer.Could not create prepared statement: {%s}", e.getMessage() ) );
        context.setError( "Could not create prepared statement" );
      }
    }
    if ( context.isNotInError() ) {
      if ( batchsize <= 1 ) {
        final DataFrame frame = frameset.get( 0 );
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing single frame {%s}", frame ) );

        int indx = 1;
        for ( final String name : frameset.getColumns() ) {
          final DataField field = frame.getField( name );
          setData( ps, indx++, field );
          if ( context.isInError() ) {
            break;
          }
        }

        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.EXECUTING: {%s}", ps.toString() ) );

        try {
          ps.execute();
        } catch ( final SQLException e ) {
          context.setError( "Could not insert single row: " + e.getMessage() );
          e.printStackTrace();
        }

      } else {
        // Now write a batch
        Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing batch of {%s} frames", frameset.size() ) );

        for ( final DataFrame frame : frameset.getRows() ) {
          Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing {%s}", frame ) );

          int indx = 1;
          for ( final String name : frameset.getColumns() ) {
            final DataField field = frame.getField( name );
            if ( ( field != null ) && !field.isNull() ) {
              setData( ps, indx++, field );
            }
            if ( context.isInError() ) {
              break;
            }
          }

          // add this frame as a record to the batch
          try {
            ps.addBatch();
          } catch ( final SQLException e ) {
            context.setError( "Could not add the record to the batch: " + e.getMessage() );
          }

        }
        if ( context.isNotInError() ) {
          try {
            ps.executeBatch();
          } catch ( final SQLException e ) {
            context.setError( "Could not insert batch: " + e.getMessage() );
          }
        }
      }
      frameset.clearRows();
    }
  }




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame( final DataFrame frame ) {
    Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing {%s} fields", frame.size() ) );
    frameset.add( frame );

    if ( frameset.size() >= batchsize ) {
      Log.debug( LogMsg.createMsg( Batch.MSG, "Writer.Writing batch, size={%s} batch={%s}", frameset.size(), batchsize ) );
      writeBatch();
    }

  }

}
