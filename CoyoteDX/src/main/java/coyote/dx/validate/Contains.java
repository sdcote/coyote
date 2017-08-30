/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.validate;

import java.io.File;
import java.net.URI;

import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * Configured thusly:<pre>
 * "Contains": { "field" : "Role", "values" : [ "USER", "ADMIN", "GUEST" ] }
 * "Contains": { "field" : "Role", "avoid" : [ "USER", "ADMIN", "GUEST" ] }
 * "Contains": { "field" : "Role", "avoid" : [ "USER", "ADMIN", "GUEST" ], "IgnoreCase" : true }</pre>
 */
public class Contains extends AbstractValidator {
  private java.util.regex.Pattern fieldPattern = null;
  private boolean avoid = false;
  private URI valueUri = null;
  private boolean ignoreCase = false;
  private String[] values = new String[0];




  /**
   * @see coyote.dx.validate.AbstractValidator#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    fieldPattern = java.util.regex.Pattern.compile(getFieldName());

    if (getConfiguration().contains(ConfigTag.AVOID)) {
      setAvoid(true);
      setValues(getConfiguration().get(ConfigTag.AVOID));
    } else if (getConfiguration().contains(ConfigTag.VALUES)) {
      setAvoid(false);
      setValues(getConfiguration().getObject(ConfigTag.VALUES));
    }

    if (getConfiguration().containsIgnoreCase(ConfigTag.IGNORE_CASE)) {
      try {
        ignoreCase = getConfiguration().getBoolean(ConfigTag.IGNORE_CASE);
      } catch (Exception e) {
        throw new ConfigurationException("Configuration option to ignore case is not a valid boolean value: '" + getConfiguration().getAsString(ConfigTag.IGNORE_CASE) + "'");
      }
    }

  }




  /**
   * @see coyote.dx.validate.AbstractValidator#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    if (valueUri != null) {
      // load the values from the URI
    }
  }




  /**
   * @param object
   */
  private void setValues(Object object) throws ConfigurationException {
    if (object != null) {
      if (object instanceof Object[]) {
        loadArray((Object[])object);
      } else if (object instanceof String) {
        // if not an array, it is a filename or URI
        String token = (String)object;
        URI uri = UriUtil.parse(token);
        if (uri != null) {
          if (uri.getScheme() == null) {
            File file = new File(token);
            file = CDX.resolveFile(file, getContext());
          } else {
            valueUri = uri;
          }
        } else {
          throw new ConfigurationException("Invalid URI: '" + token + "'");
        }
      }
    } else {
      throw new ConfigurationException("Invalid value object: NULL");
    }
  }




  /**
   * @param array
   */
  private void loadArray(Object[] array) throws ConfigurationException {
    if (array != null) {
      values = new String[array.length];
      for (int x = 0; x < array.length; x++) {
        if (array[x] != null) {
          values[x] = array[x].toString();
        } else {
          values[x] = "";
        }
      }
    }
  }




  /**
   * @param flag true if the validator is to avoid the given values (return 
   *        false on match), false if the field value must match one of the 
   *        given values.
   */
  public void setAvoid(boolean flag) {
    avoid = flag;
  }




  /**
   * @return true if this validator is to reject any matching value, false if 
   *         this validator is limiting the the values in this validator.
   */
  private boolean isAvoiding() {
    return avoid;
  }




  /**
   * @return true if the search is to ignore the case when searching for a 
   *         match in the array of values, false if the check is to be case 
   *         sensitive.
   */
  private boolean isIgnoringCase() {
    return ignoreCase;
  }




  /**
   * @see coyote.dx.FrameValidator#process(coyote.dx.context.TransactionContext)
   */
  @Override
  public boolean process(TransactionContext context) throws ValidationException {
    boolean retval = true;
    DataFrame frame = context.getWorkingFrame();

    if (frame != null) {
      String value = null;

      for (DataField field : frame.getFields()) {
        if (field.getName() != null && fieldPattern.matcher(field.getName()).matches()) {
          value = field.getStringValue();

          if (isAvoiding()) {
            retval = !valueIsInArray(value, values, isIgnoringCase());
          } else {
            retval = valueIsInArray(value, values, isIgnoringCase());
          }
        }
      }

      // call the superclass fail method to trigger the fireValidationFailed 
      // event and throw  ValidationException if haltOnFail is true
      if (!retval) {
        fail(context, fieldName);
      }
    } else {
      // fail && error
      retval = false;
      fail(context, fieldName, "There is no working frame");
    }

    return retval;
  }




  /**
   * Check to see if the given token is in the array.
   * 
   * @param token the token for which to search
   * @param array the array of values to search
   * @param ignorecase true if the match is case insensitive, false to match case
   * 
   * @return true if the given token is in the given array of values, false otherwise
   */
  private boolean valueIsInArray(String token, String[] array, boolean ignorecase) {
    boolean retval = false;

    if (array != null) {
      if (token != null) {
        for (int x = 0; x < array.length; x++) {
          if (ignorecase) {
            if (token.equalsIgnoreCase(array[x])) {
              retval = true;
              break;
            }
          } else {
            if (token.equals(array[x])) {
              retval = true;
              break;
            }
          }
        }
      } else {
        for (int x = 0; x < array.length; x++) {
          if (array[x] == null) {
            retval = true;
            break;
          }
        }
      }
    } else {
      if (token == null) {
        retval = true;
      } else {
        retval = false;
      }
    }
    return retval;
  }

}
