/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.mc.MetricUtil;

import java.text.ParseException;
import java.util.Date;


/**
 * Represents a single record/row from a table
 */
public class SnowRecord extends DataFrame {
  private static final int MINUTE = 1000 * 60;
  protected SnowKey key;
  protected SnowDateTime updatedTimestamp;
  protected SnowDateTime createdTimestamp;


  /**
   * Construct a blank record suitable for holding data.
   *
   * <p>This record is not associated to any table.</p>
   */
  public SnowRecord() {
    key = null;
  }


  public SnowRecord(final SnowKey key) {
    if ((key != null) && (key.value != null) && (key.value.length() > 0)) {
      this.key = key;
      super.put(ServiceNowFields.SYS_ID, key.toString());
    } else {
      throw new IllegalArgumentException("Record key cannot be null or empty");
    }
  }


  /**
   * @param dataframe the source of the data for this record
   * @throws SnowException if the naming of fields or their data is incorrect
   */
  protected SnowRecord(final DataFrame dataframe) throws SnowException {

    // Copy all the fields from the given frame
    for (final DataField field : dataframe.getFields()) {
      super.fields.add(field);
    }

    final String sysid = getAsString(ServiceNowFields.SYS_ID);

    if (sysid != null && sysid.length() > 0) {
      try {
        key = new SnowKey(sysid);
      } catch (Exception e) {
        // Some views will return composite sysids, allow them
        if (sysid.startsWith("__ENC__")) {
          key = new SnowKeyRaw(sysid);
        } else {
          throw new SnowException(e, "Invalid system identifier: '" + sysid + "'");
        }
      }
    }

    final String updated_on = getAsString(ServiceNowFields.SYS_UPDATED_ON);
    if (updated_on != null) {
      try {
        updatedTimestamp = new SnowDateTime(updated_on);
      } catch (final ParseException e) {
        throw new SnowException("Invalid sys_updated_on=" + updated_on, toString());
      }
    }

    final String created_on = getAsString(ServiceNowFields.SYS_CREATED_ON);
    if (created_on != null) {
      try {
        createdTimestamp = new SnowDateTime(created_on);
      } catch (final ParseException e) {
        throw new SnowException("Invalid sys_created_on=" + created_on, toString());
      }
    }

    super.modified = false;
  }


  /**
   * Get sys_created_on from a SnowRecord object.
   *
   * @return the timestamp when this record was created
   */
  public SnowDateTime getCreatedTimestamp() {
    return createdTimestamp;
  }


  /**
   * Return a DateTime field as a SnowDateTime.
   *
   * <p>For a Java date use getDateTime(fieldname).toDate().
   *
   * @param fieldname the field name to parse
   * @return a SnowDateTime reference
   * @throws ParseException if there were issues parsing the date time value
   */
  public SnowDateTime getDateTime(final String fieldname) throws ParseException {
    final String value = getFieldValue(fieldname);
    if (value == null) {
      return null;
    }

    final SnowDateTime result = new SnowDateTime(value);

    return result;
  }


  /**
   * Return the value of the given field name as a boolean value.
   *
   * <p><strong>NOTE:</strong> of the field is null or if the value does not
   * parse into a boolean, the value of {@code false} is returned. An
   * exception is not thrown for illegal or missing values.</p>
   *
   * @param fieldname The name of the field to retrieve
   * @return the value of the field of as a boolean.
   */
  public boolean getFieldAsBoolean(final String fieldname) {
    final String value = getFieldValue(fieldname);

    if ((value != null) && (value.trim().length() > 0)) {
      final String tlc = value.trim().toLowerCase();
      return "true".equals(tlc) || "yes".equals(tlc) || "1".equals(tlc);
    }

    return false;
  }


  /**
   * Return the value of the given field name as an integer.
   *
   * <p><strong>NOTE:</strong> of the field is null or if the value does not
   * parse into a integer, the value of {@code 0} (zero) is returned. An
   * exception is not thrown for illegal or missing values.</p>
   *
   * @param fieldname The name of the field to retrieve
   * @return the value of the field of as an integer.
   */
  public int getFieldAsInt(final String fieldname) {
    try {
      return getAsInt(fieldname);
    } catch (final Exception e) {
      return 0;
    }
  }


  /**
   * Get the value of a field from a SnowRecord.
   *
   * <p> If the field is empty (zero length) or missing then return null.
   *
   * @param fieldname Name of SnowRecord field
   * @return Field value as a string or null if the field is missing or has zero length.
   */
  public String getFieldValue(final String fieldname) {
    String retval = getAsString(fieldname);
    if (retval != null && retval.length() == 0) {
      retval = null;
    }
    return retval;
  }


  /**
   * Get the sys_id from a SnowRecord object.
   *
   * @return sys_id
   */
  public SnowKey getKey() {
    return key;
  }


  public String getIdentifier() {
    return getAsString(ServiceNowFields.SYS_ID);
  }


  public void setIdentifier(String value) {
    key = new SnowKey(value);
    super.put(ServiceNowFields.SYS_ID, key.toString());
  }


  public SnowKey getReference(final String fieldname) {
    final String value = getAsString(fieldname);

    if ((value == null) || (value.length() == 0)) {
      return null;
    }
    return new SnowKey(value);
  }


  /**
   * Get sys_updated_on from a SnowRecord object.
   *
   * @return the timestamp when this record was updated
   */
  public SnowDateTime getUpdatedTimestamp() {
    return updatedTimestamp;
  }


  public int numFields() {
    return super.size();
  }


  /**
   * @return the record as a multi-line, indented JSON String.
   */
  public String toFormattedString() {
    return JSONMarshaler.toFormattedString(this);
  }


  /**
   * @return the record as a JSON formatted string.
   */
  @Override
  public String toString() {
    return JSONMarshaler.marshal(this);
  }


  /**
   * @return return the number of this record or  null if this record does not have a field with the name of "number".
   */
  public String getNumber() {
    return getAsString(ServiceNowFields.NUMBER);
  }

  /**
   * @return return the short description of this record or null if this record does not have a field with the name of "short_description".
   */
  public String getShortDescription() {
    return getAsString(ServiceNowFields.SHORT_DESCRIPTION);
  }

  /**
   * @return the age of this record in minutes
   */
  public int getAgeInMinutes() {
    int retval = 0;
    if (createdTimestamp != null) {
      long elapsed = new Date().getTime() - createdTimestamp.toDate().getTime();
      retval = (int) elapsed / MINUTE;
    }
    return retval;
  }


  /**
   * @return the age of this record in hours
   */
  public float getAgeInHours() {
    float retval = 0;
    int minutes = getAgeInMinutes();
    if (minutes > 0) retval = MetricUtil.round(minutes / 60, 2);
    return retval;
  }

}
