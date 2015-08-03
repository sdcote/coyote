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
package coyote.batch.listener;

import java.util.ArrayList;
import java.util.List;

import coyote.batch.ContextListener;
import coyote.batch.OperationalContext;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * This listener keeps track of the data read in to and out of the engine and 
 * reports on the characteristics of the data observed.
 */
public class DataProfiler extends FileRecorder implements ContextListener {
  private List<FieldMetric> inputFields = new ArrayList<FieldMetric>();
  private List<FieldMetric> outputFields = new ArrayList<FieldMetric>();
  int readCount = 0;
  long readBytes = 0;




  /**
   * @see coyote.batch.listener.FileRecorder#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context ); // initialize FileRecorder

  }




  /**
   * @see coyote.batch.listener.AbstractListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {

    if ( context instanceof TransformContext ) {
      writeSummary();
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onRead(coyote.batch.TransactionContext)
   */
  @Override
  public void onRead( TransactionContext context ) {
    DataFrame frame = context.getSourceFrame();

    if ( frame != null ) {
      readCount++;
      readBytes += frame.getBytes().length;
      for ( DataField field : frame.getFields() ) {
        FieldMetric metric = getInputFieldMetric( field.getName() );

        // Set the type of the field
        if ( metric.getType() == null ) {
          metric.setType( field.getTypeName() );
        } else if ( !field.getTypeName().equals( metric.getType() ) ) {
          System.err.println( "TYPE SWITCH FROM '" + metric.getType() + "' TO '" + field.getTypeName() + "'" );
        }

        // Set length values
        String value = field.getStringValue();
        if ( value != null ) {
          int slen = metric.getStringLength();
          if ( value.length() > slen ) {
            metric.setStringLength( value.length() );
          }
          int blen = field.getBytes().length;
          if ( blen > metric.getByteLength() ) {
            metric.setByteLength( blen );
          }

        } else {
          // No value
          metric.incrementNullValueCount();
        }

      } // for

    } else {

    }

  }




  /**
   * Return a FieldMetric associated with the named field. 
   * 
   * <p>If one is not found, one will be created and placed in the cache for 
   * later reference. This method never returns null.</p>
   * 
   * @param name The name of the field to be represented by the returned metric
   * 
   * @return a FieldMetric associated with the named field. Never returns null.
   */
  private FieldMetric getInputFieldMetric( String name ) {
    FieldMetric retval = null;
    if ( name != null ) {
      for ( FieldMetric metric : inputFields ) {
        if ( name.equals( metric.getName() ) ) {
          retval = metric;
          break;
        }
      }
    }

    if ( retval == null ) {
      retval = new FieldMetric( name );
      inputFields.add( retval );
    }

    return retval;
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onWrite(coyote.batch.TransactionContext)
   */
  @Override
  public void onWrite( TransactionContext transactionContext ) {
    // TODO: analyse each field of the data written out (target frame)
  }




  /**
   * Write the summary of the data read in.
   */
  private void writeInputSummary() {
    StringBuffer b = new StringBuffer( "Input Data Profile:" );
    b.append( StringUtil.LINE_FEED );
    b.append( "Field count: " );
    b.append( inputFields.size() );
    b.append( StringUtil.LINE_FEED );
    write( b.toString() );

    int nameSize = 0;
    int typeSize = 0;
    for ( FieldMetric metric : inputFields ) {
      if ( metric.getName().length() > nameSize ) {
        nameSize = metric.getName().length();
      }
      if ( metric.getType().length() > typeSize ) {
        typeSize = metric.getType().length();
      }

    }

    for ( FieldMetric metric : inputFields ) {
      b.delete( 0, b.length() );
      b.append( StringUtil.fixedLength( metric.getName(), nameSize, 0 ) );
      b.append( " " );
      b.append( StringUtil.fixedLength( metric.getType(), typeSize, 0 ) );
      b.append( " " );
      b.append( StringUtil.fixedLength( Integer.toString( metric.getStringLength() ), 8, 0 ) );

      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
    }

  }




  /**
   * Write the summary of the data written out.
   */
  private void writeOutputSummary() {

  }




  /**
   * Write the summary report of the data processed
   */
  private void writeSummary() {
    writeInputSummary();
    writeOutputSummary();
  }

  /**
   * This is a type which holds data about fields.
   */
  private class FieldMetric {
    private String name = null;
    private String type = null;
    int strLength = 0;
    int byteLength = 0;

    int nullCount = 0;
    int emptyCount = 0;
    long byteCount = 0;




    FieldMetric( String name ) {
      this.name = name;
    }




    /**
     * increment the null counter by 1 
     */
    public void incrementNullValueCount() {
      nullCount++;
    }




    /**
     * @param length
     */
    public void setStringLength( int length ) {
      strLength = length;
    }




    /**
     * @param length
     */
    public void setByteLength( int length ) {
      byteLength = length;
    }




    /**
     * @return
     */
    public int getStringLength() {
      return strLength;
    }




    /**
     * @return
     */
    public int getByteLength() {
      return byteLength;
    }




    /**
     * @param typeName
     */
    public void setType( String typeName ) {
      type = typeName;
    }




    /**
     * @return
     */
    public String getType() {
      return type;
    }




    /**
     * @return the name of the field to which this metric applies
     */
    public String getName() {
      return name;
    }
  }

}
