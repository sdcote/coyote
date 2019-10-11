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
import coyote.dataframe.DataFrameException;

public class SnowStory extends SnowRecord {
  private static final String CLASSIFICATION = "classification";
  private static final String UNKNOWN = "Unknown";
  private static final String ACTIVE = "active";
  private static final String TYPE = "type";
  private static final String POINTS = "story_points";
  private static final String FEATURE = "Feature";
  private static final String DEVELOPMENT = "Development";
  private static final String DEFECT = "Defect";
  private boolean active = false;


  public SnowStory(DataFrame frame) throws SnowException {
    super(frame);
    active = getFieldAsBoolean(ACTIVE); // cache the value of the active flag
  }

  /**
   * Gets the classification (Feature, Defect, etc.) of this story.
   *
   * <p>Note this is for version 1 of the agile module only. Subsequent versions use the "type" field.</p>
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
   * Gets the type of story (Feature, Defect, etc.)  this is.
   *
   * @return the type of this story or "Unknown" if the classification is blank or null;
   */
  public String getType() {
    String retval = getFieldValue(TYPE);
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

  /**
   * @return true if the type is "Defect", false otherwise.
   */
  public boolean isDefect() {
    String type = getFieldValue(TYPE);
    if (type != null) return type.toLowerCase().contains(DEFECT.toLowerCase());
    return false;
  }

  /**
   * @return true if the type is "Feature" or "Development", false otherwise.
   */
  public boolean isFeature() {
    String type = getFieldValue(TYPE);
    if (type != null) {
      return (type.toLowerCase().contains(FEATURE.toLowerCase()) || type.toLowerCase().contains(DEVELOPMENT.toLowerCase()));
    }
    return false;
  }

  /**
   * @return the number of points for this story or 0 if null or empty.
   */
  public int getPoints() {
    int retval = 0;
    if (this.contains(POINTS)) {
      try {
        retval = getAsInt(POINTS);
      } catch (DataFrameException e) {
        // expected for unpointed stories
      }
    }
    return retval;
  }

}
