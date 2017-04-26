package coyote.commons.network.mqtt;

/**
 * Represents an object used to send ping packets to a MQTT broker every keep 
 * alive interval. 
 */
public interface PingSender {

  /**
   * Initialization method.
   * 
   * @param conn The connection to the server/broker.
   */
  public void init( Connection conn );




  /**
   * Schedule next ping with a specific delay.
   * 
   * @param delay in milliseconds.
   */
  public void schedule( long delay );




  /**
   * Start ping sender.
   *  
   * <p>It will be called after the connection is successful.</p>
   */
  public void start();




  /**
   * Stop ping sender. 
   * 
   * <p>It is called if there are any errors or connection shutdowns.</p>
   */
  public void stop();

}
