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

import coyote.dataframe.DataField;
import coyote.i13n.Metric;


/**
 * This class examines the sampled data fields and tracks several metrics. 
 */
public class FieldMetrics {
  private static final String STRING_LENGTH = "String Length";
  private static final String BYTE_LENGTH = "Byte Length";
  private String fieldName = null;
  private String fieldType = null;
  long nullCount = 0;
  long emptyCount = 0;
  long blankCount = 0;
  Metric stringLength = new Metric( STRING_LENGTH, "" );
  Metric byteLength = new Metric( BYTE_LENGTH, "" );




  FieldMetrics( String name ) {
    fieldName = name;
  }




  /**
   * @param typeName
   */
  public void setType( String typeName ) {
    fieldType = typeName;
  }




  /**
   * @return
   */
  public String getType() {
    return fieldType;
  }




  /**
   * @return the name of the field to which this metric applies
   */
  public String getName() {
    return fieldName;
  }




  public void sample( DataField field ) {
    // Set the type of the field
    if ( getType() == null ) {
      setType( field.getTypeName() );
    } else if ( !field.getTypeName().equals( getType() ) ) {
      // System.err.println( "TYPE SWITCH FROM '" + metric.getType() + "' TO '" + field.getTypeName() + "'" );
    }

    // Set metrics based on the value of the field
    String value = field.getStringValue();
    if ( value != null ) {
      if ( value.length() == 0 ) {
        emptyCount++;
      } else if ( value.trim().length() == 0 ) {
        blankCount++;
      }

      stringLength.sample( value.length() );
      byteLength.sample( field.getBytes().length );
    } else {
      nullCount++;
    }

  }




  /**
   * @return the number of times the value was null
   */
  protected long getNullCount() {
    return nullCount;
  }




  /**
   * @return the number of times the value was empty
   */
  protected long getEmptyCount() {
    return emptyCount;
  }




  /**
   * @return the number of times the value was all whitespace
   */
  protected long getBlankCount() {
    return blankCount;
  }




  /**
   * @return formatted string of string length metrics
   */
  public Object getStringMetrics() {
    return stringLength.toString();
  }




  /**
   * @return formatted string of byte length metrics
   */
  public Object getByteMetrics() {
    return byteLength.toString();
  }




  public long getAverageStringLength() {
    return stringLength.getAvgValue();
  }




  public long getMinimumStringLength() {
    return stringLength.getMinValue();
  }




  public long getMaximumStringLength() {
    return stringLength.getMaxValue();
  }




  public long getStdDevStringLength() {
    return stringLength.getStandardDeviation();
  }




  public long getTotalStringLength() {
    return stringLength.getTotal();
  }




  public long getAverageByteLength() {
    return byteLength.getAvgValue();
  }




  public long getMinimumByteLength() {
    return byteLength.getMaxValue();
  }




  public long getMaximumByteLength() {
    return byteLength.getMinValue();
  }




  public long getStdDevByteLength() {
    return byteLength.getStandardDeviation();
  }




  public long getTotalByteLength() {
    return byteLength.getTotal();
  }

}
