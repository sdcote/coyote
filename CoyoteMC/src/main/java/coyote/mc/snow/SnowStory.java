/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;

public class SnowStory extends SnowRecord {
  private static final String CLASSIFICATION = "classification";
  private static final String UNKNOWN = "Unknown";
  private static final String ACTIVE = "active";
  private boolean active = false;


  public SnowStory(DataFrame frame) throws SnowException {
    super(frame);
    active = getFieldAsBoolean(ACTIVE); // cache the value of the active flag
  }

  /**
   * Gets the classification (Feature, Defect, etc.) of this story.
   *
   * @return the classification of this story or "Unknown" if the classification is blank or null;
   */
  public String getClassification() {
    String retval = getFieldValue(CLASSIFICATION);
    if (retval == null || StringUtil.isBlank(retval)) {
      retval = UNKNOWN;
    }
    return retval;
  }

  /**
   * @return the boolean value of the "active" field, or false of the active field does not exist.
   */
  public boolean isActive() {
    return active;
  }

}
