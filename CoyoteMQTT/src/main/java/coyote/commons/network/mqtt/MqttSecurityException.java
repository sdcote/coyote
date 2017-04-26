package coyote.commons.network.mqtt;

/**
 * Thrown when a client is not authorized to perform an operation, or
 * if there is a problem with the security configuration.
 */
public class MqttSecurityException extends MqttException {
  private static final long serialVersionUID = 300L;




  /**
   * Constructs a new <code>MqttSecurityException</code> with the specified code
   * as the underlying reason.
   * @param reasonCode the reason code for the exception.
   */
  public MqttSecurityException( final int reasonCode ) {
    super( reasonCode );
  }




  /**
   * Constructs a new <code>MqttSecurityException</code> with the specified 
   * code and <code>Throwable</code> as the underlying reason.
   * @param reasonCode the reason code for the exception.
   * @param cause the underlying cause of the exception.
   */
  public MqttSecurityException( final int reasonCode, final Throwable cause ) {
    super( reasonCode, cause );
  }




  /**
   * Constructs a new <code>MqttSecurityException</code> with the specified 
   * <code>Throwable</code> as the underlying reason.
   * @param cause the underlying cause of the exception.
   */
  public MqttSecurityException( final Throwable cause ) {
    super( cause );
  }
}
