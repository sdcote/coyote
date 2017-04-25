package coyote.nmea;

/**
 * Thrown to indicate that an implementation of a sentence cannot interpret the 
 * contents of requested data field. For example, when a field contains invalid 
 * value that cannot be parsed to expected native data type.
 */
public class ParseException extends DataNotAvailableException {
  private static final long serialVersionUID = -5682651169153308821L;




  /**
   * Constructor with description.
   * 
   * @param msg Description of the Exception
   */
  public ParseException( final String msg ) {
    super( msg );
  }




  /**
   * Constructor with message and cause.
   * 
   * @param msg Description of the Exception
   * @param cause what caused this exception
   */
  public ParseException( final String msg, final Throwable cause ) {
    super( msg, cause );
  }

}
