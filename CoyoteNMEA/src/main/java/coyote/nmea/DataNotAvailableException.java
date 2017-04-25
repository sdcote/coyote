package coyote.nmea;

/**
 * Thrown to indicate that requested data is not available. For example, when
 * invoking a getter for sentence data field that contains no value.
 */
public class DataNotAvailableException extends RuntimeException {

  private static final long serialVersionUID = 8161114908731458261L;




  /**
   * Constructor
   * 
   * @param msg Exception message
   */
  public DataNotAvailableException( final String msg ) {
    super( msg );
  }




  /**
   * Constructor
   * 
   * @param msg Exception message
   * @param cause what caused the exception
   */
  public DataNotAvailableException( final String msg, final Throwable cause ) {
    super( msg, cause );
  }

}
