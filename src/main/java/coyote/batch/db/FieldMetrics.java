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
package coyote.batch.db;

import java.util.HashSet;
import java.util.Set;

import coyote.dataframe.DataField;
import coyote.i13n.Metric;
import coyote.i13n.SimpleMetric;


/**
 * This class examines the sampled data fields and tracks several metrics.
 * 
 * TODO track data based on types...numerics : MinMaxAvgSDev
 */
public class FieldMetrics {
  private static final String STRING_LENGTH = "String Length";
  private static final String BYTE_LENGTH = "Byte Length";
  private String fieldName = null;
  private String fieldType = null;
  private long fieldLength = -1L;
  long nullCount = 0;
  long emptyCount = 0;
  long blankCount = 0;
  SimpleMetric stringLength = new SimpleMetric( STRING_LENGTH, "" );
  SimpleMetric byteLength = new SimpleMetric( BYTE_LENGTH, "" );
  private Set<Object> values = new HashSet<Object>();
  long sampleCount = 0;




  public FieldMetrics( String name ) {
    fieldName = name;
  }




  /**
   * @param typeName
   */
  public void setType( String typeName ) {
    fieldType = typeName;
  }




  /**
   * @return the name of the type of the sampled field
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
    sampleCount++;
    // Set the type of the field
    if ( getType() == null ) {
      setType( field.getTypeName() );
    } else if ( !field.getTypeName().equals( getType() ) ) {
      // TODO count the different types observed for this field not including UDEF as might be observed when a NUL value is sampled.
      // System.err.println( "TYPE SWITCH FROM '" + metric.getType() + "' TO '" + field.getTypeName() + "'" );
    }

    // Set metrics based on the value of the field
    String value = field.getStringValue();
    if ( value != null ) {
      values.add( value );

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
  public long getNullCount() {
    return nullCount;
  }




  /**
   * @return the number of times the value was empty
   */
  public long getEmptyCount() {
    return emptyCount;
  }




  /**
   * @return the number of times the value was all whitespace
   */
  public long getBlankCount() {
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




  public int getUniqueValues() {
    return values.size();
  }




  public float getCoincidence() {
    if ( sampleCount > 0 ) {
      return (float)( sampleCount - ( values.size() - 1 ) ) / (float)sampleCount;
    } else {
      return 1F;
    }
  }




  /**
   * @return the maximum (string) length for this field.
   */
  public long getMaxLength() {
    return stringLength.getMaxValue();
  }




  public void setMaxLength( long len ) {

    stringLength.sample( len );;
  }




  /**
   * @return the field Length
   */
  public long getLength() {
    if ( fieldLength >= 0 ) {
      return fieldLength;
    } else {
      return 0;
    }
  }




  /**
   * @param length the length to set
   */
  public void setLength( long length ) {
    fieldLength = length;
  }

}
