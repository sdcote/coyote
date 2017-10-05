/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.util.HashSet;
import java.util.Set;

import coyote.dataframe.DataField;
import coyote.i13n.SimpleMetric;


/**
 * This class examines the sampled data fields and tracks several metrics.
 *
 * TODO track data based on types...numerics : MinMaxAvgSDev
 */
public class FieldMetrics {
  private static final String BYTE_LENGTH = "Byte Length";
  private static final String STRING_LENGTH = "String Length";
  private long fieldLength = -1L;
  private String fieldName = null;
  private String fieldType = null;
  private final Set<Object> values = new HashSet<Object>();
  long blankCount = 0;
  SimpleMetric byteLength = new SimpleMetric(BYTE_LENGTH, "");
  long emptyCount = 0;
  long nullCount = 0;
  long sampleCount = 0;
  SimpleMetric stringLength = new SimpleMetric(STRING_LENGTH, "");




  public FieldMetrics(final String name) {
    fieldName = name;
  }




  public long getAverageByteLength() {
    return byteLength.getAvgValue();
  }




  public long getAverageStringLength() {
    return stringLength.getAvgValue();
  }




  /**
   * @return the number of times the value was all whitespace
   */
  public long getBlankCount() {
    return blankCount;
  }




  /**
   * @return formatted string of byte length metrics
   */
  public Object getByteMetrics() {
    return byteLength.toString();
  }




  public float getCoincidence() {
    if (sampleCount > 0) {
      return (float)(sampleCount - (values.size() - 1)) / (float)sampleCount;
    } else {
      return 1F;
    }
  }




  /**
   * @return the number of times the value was empty
   */
  public long getEmptyCount() {
    return emptyCount;
  }




  /**
   * @return the field Length
   */
  public long getLength() {
    if (fieldLength >= 0) {
      return fieldLength;
    } else {
      return 0;
    }
  }




  public long getMaximumByteLength() {
    return byteLength.getMinValue();
  }




  public long getMaximumStringLength() {
    return stringLength.getMaxValue();
  }




  /**
   * @return the maximum (string) length for this field.
   */
  public long getMaxLength() {
    return stringLength.getMaxValue();
  }




  public long getMinimumByteLength() {
    return byteLength.getMaxValue();
  }




  public long getMinimumStringLength() {
    return stringLength.getMinValue();
  }




  /**
   * @return the name of the field to which this metric applies
   */
  public String getName() {
    return fieldName;
  }




  /**
   * @return the number of times the value was null
   */
  public long getNullCount() {
    return nullCount;
  }




  public long getStdDevByteLength() {
    return byteLength.getStandardDeviation();
  }




  public long getStdDevStringLength() {
    return stringLength.getStandardDeviation();
  }




  /**
   * @return formatted string of string length metrics
   */
  public Object getStringMetrics() {
    return stringLength.toString();
  }




  public long getTotalByteLength() {
    return byteLength.getTotal();
  }




  public long getTotalStringLength() {
    return stringLength.getTotal();
  }




  /**
   * @return the name of the type of the sampled field
   */
  public String getType() {
    return fieldType;
  }




  public int getUniqueValues() {
    return values.size();
  }




  public void sample(final DataField field) {
    sampleCount++;
    // Set the type of the field
    if (getType() == null) {
      setType(field.getTypeName());
    } else if (!field.getTypeName().equals(getType())) {
      // TODO count the different types observed for this field not including UDEF as might be observed when a NUL value is sampled.
      // System.err.println( "TYPE SWITCH FROM '" + metric.getType() + "' TO '" + field.getTypeName() + "'" );
    }

    // Set metrics based on the value of the field
    final String value = field.getStringValue();
    if (value != null) {
      values.add(value);

      if (value.length() == 0) {
        emptyCount++;
      } else if (value.trim().length() == 0) {
        blankCount++;
      }

      stringLength.sample(value.length());
      byteLength.sample(field.getBytes().length);
    } else {
      nullCount++;
    }

  }




  /**
   * @param length the length to set
   */
  public void setLength(final long length) {
    fieldLength = length;
  }




  public void setMaxLength(final long len) {

    stringLength.sample(len);;
  }




  /**
   * @param typeName
   */
  public void setType(final String typeName) {
    fieldType = typeName;
  }

}
