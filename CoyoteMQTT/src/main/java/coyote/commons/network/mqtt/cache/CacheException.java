package coyote.commons.network.mqtt.cache;

import coyote.commons.network.mqtt.MqttException;


/**
 * This exception is thrown by the implementor of the cache interface if there 
 * is a problem reading from or writing data to the cache.
 */
public class CacheException extends MqttException {

  private static final long serialVersionUID = -2708544368790026381L;




  /**
   * Constructs a new {@code CacheException}
   */
  public CacheException() {
    super( CLIENT_EXCEPTION );
  }




  /**
   * Constructs a new {@code CacheException} with the specified code as the 
   * underlying reason.
   * 
   * @param reasonCode the reason code for the exception.
   */
  public CacheException( final int reasonCode ) {
    super( reasonCode );
  }




  /**
   * Constructs a new {@code CacheException} with the specified {@code 
   * Throwable} as the underlying reason.
   * 
   * @param reason the reason code for the exception.
   * @param cause the underlying cause of the exception.
   */
  public CacheException( final int reason, final Throwable cause ) {
    super( reason, cause );
  }




  /**
   * Constructs a new {@code CacheException} with the specified {@code 
   * Throwable} as the underlying reason.
   * 
   * @param cause the underlying cause of the exception.
   */
  public CacheException( final Throwable cause ) {
    super( cause );
  }

}
