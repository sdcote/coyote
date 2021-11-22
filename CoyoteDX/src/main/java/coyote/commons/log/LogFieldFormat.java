package coyote.commons.log;

/**
 * Describes the positional field name/type for a log entry.
 */
public class LogFieldFormat {
  String name;
  LogFieldType type;

  LogFieldFormat(String name, LogFieldType type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public LogFieldType getType() {
    return type;
  }
}
