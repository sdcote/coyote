package coyote.mc.snow;

import coyote.dataframe.DataFrame;

public class SnowIncident extends SnowRecord {
  private static final String ACTIVE = "active";
  /** Cached value of the active flag since it is used often. */
  private boolean active;


  public SnowIncident(DataFrame frame) throws SnowException {
    super(frame);
    active = getFieldAsBoolean(ACTIVE);
  }

  /**
   * @return the boolean value of the "active" field, or false of the active field does not exist.
   */
  public boolean isActive() {
    return active;
  }

}
