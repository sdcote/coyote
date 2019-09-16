/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import coyote.dataframe.DataFrame;

import java.text.ParseException;
import java.util.Date;

public class SnowSprint extends SnowRecord {

  private static final String SCHEDULED_START_DATE = "schedule_start_date";
  private static final String SCHEDULED_END_DATE = "schedule_end_date";

  /**
   * Create a SnowSprint record from the given frame.
   *
   * @param frame the source of the data for this record
   * @throws SnowException if the naming of fields or their data is incorrect
   */
  public SnowSprint(DataFrame frame) throws SnowException {
    super(frame);
  }

  public boolean isCurrent() {
    boolean retval = false;
    SnowDateTime now = new SnowDateTime(new Date());
    try {
      SnowDateTime start = getDateTime(SCHEDULED_START_DATE);
      SnowDateTime end = getDateTime(SCHEDULED_END_DATE);
      if (start != null && end != null && now.compareTo(start) >= 0 && now.compareTo(end) < 0) {
        retval = true;
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return retval;
  }

}
