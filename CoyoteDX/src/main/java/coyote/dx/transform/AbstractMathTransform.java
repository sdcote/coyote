/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.transform;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dx.TransformException;


/**
 * 
 */
public abstract class AbstractMathTransform extends AbstractFieldTransform {

  protected boolean isFractional(String token) {
    boolean retval = false;
    if (StringUtil.isNotEmpty(token) && token.indexOf(".") > -1) {
      retval = true;
    }
    return retval;
  }




  protected double getAsDouble(DataField field) throws TransformException {
    double retval = 0D;
    if (field != null && field.isNotFrame() && field.isNotNull()) {
      Object val = field.getObjectValue();
      if (val != null) {
        if (val instanceof Double) {
          retval = ((Double)val).doubleValue();
        } else {
          try {
            retval = Double.parseDouble(val.toString());
          } catch (Exception e) {
            throw new TransformException("Value could not be converted into a double");
          }
        }
      } else {
        throw new TransformException("Null field value could not be converted into a double");
      }
    }
    return retval;
  }




  protected long getAsLong(DataField field) throws TransformException {
    long retval = 0L;
    if (field != null && field.isNotFrame() && field.isNotNull()) {
      Object val = field.getObjectValue();
      if (val != null) {
        if (val instanceof Double) {
          retval = ((Long)val).longValue();
        } else {
          try {
            String ls = val.toString();
            retval = Long.parseLong(ls);
          } catch (Exception e) {
            throw new TransformException("Value could not be converted into a long");
          }
        }
      } else {
        throw new TransformException("Null field value could not be converted into a long");
      }
    }
    return retval;
  }




  protected long getLong(String token) throws TransformException {
    long retval = 0L;
    try {
      retval = Long.parseLong(token);
    } catch (Exception e) {
      throw new TransformException("Token '" + token + "' could not be converted into a long");
    }
    return retval;
  }




  protected double getDouble(String token) throws TransformException {
    double retval = 0D;
    try {
      retval = Double.parseDouble(token);
    } catch (Exception e) {
      throw new TransformException("Token '" + token + "' could not be converted into a double");
    }
    return retval;
  }
}
