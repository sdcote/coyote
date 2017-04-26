package coyote.commons.network.mqtt;

/**
 * Provides a mechanism to track the delivery progress of a message.
 * 
 * <p>Used to track the the delivery progress of a message when a publish is 
 * executed in a non-blocking manner (run in the background)</p>
 *  
 * @see MqttTokenImpl
 */
public class MqttDeliveryTokenImpl extends MqttTokenImpl implements MqttDeliveryToken {

  public MqttDeliveryTokenImpl() {
    super();
  }

}
