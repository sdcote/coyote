package coyote.commons.network.mqtt.protocol;

/**
 * Abstract super-class of all acknowledgment messages.
 */
public abstract class AbstractAckMessage extends AbstractMessage {

  public AbstractAckMessage( final byte type ) {
    super( type );
  }




  @Override
  protected byte getMessageInfo() {
    return 0;
  }




  /**
   * @return String representation of the wire message
   */
  @Override
  public String toString() {
    return super.toString() + " msgId " + msgId;
  }
}