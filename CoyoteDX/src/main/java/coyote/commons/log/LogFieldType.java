package coyote.commons.log;

import coyote.commons.StringUtil;

/**
 * The supported data types for log fields.
 */
public enum LogFieldType {
  /**
   * Date/Time
   */
  DATE("Date"),
  /**
   * Numeric
   */
  NUMBER("Number"),
  /**
   * Character string
   */
  TEXT("Text");

  private final String name;


  LogFieldType(final String name) {
    this.name = name;
  }

  /**
   * Get the column type by name.
   *
   * @param name name of the column to return
   * @return the column type with the given mane or null if not found.
   */
  public static LogFieldType getFieldTypeByName(final String name) {
    LogFieldType retval = null;
    if (name != null) {
      for (final LogFieldType type : LogFieldType.values()) {
        if (StringUtil.equalsIgnoreCase(name, type.toString())) {
          retval = type;
          break;
        }
      }
    }
    return retval;
  }

  public String getName() {
    return name;
  }


  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }

}
