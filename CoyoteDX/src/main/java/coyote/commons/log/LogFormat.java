package coyote.commons.log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the current parsing mode and LogFields for interpreting the
 * positional tokens in a log entry.
 */
public class LogFormat {
  private ParsingMode mode;
  List<LogFieldFormat> fields = new ArrayList<>();

  private LogFormat(){ }

  public LogFormat(ParsingMode mode) {
    this.mode = mode;
  }


  /**
   * @return the number of entries in this format
   */
  public int size() {
    return fields.size();
  }


  /**
   * Access the LogField description for the given location.
   *
   * <p>When you have the 3rd token in the log entry, you can call this method
   * to see what the name and type of the third entry should be.</p>
   *
   * @param index The position in the entry to lookup
   * @return the LogField that describes the given positional field in the log entry.
   */
  public LogFieldFormat get(int index) {
    LogFieldFormat retval = null;
    if (index < fields.size()) retval = fields.get(index);
    return retval;
  }

  public void add(LogFieldFormat field){
    fields.add(field);
  }

  public ParsingMode getMode() { return mode;}

}
