package coyote.commons.network.http.wsc;

/**
 * Simply create a payload containing a long value representing how many times
 * this generator generated a payload.
 * 
 * @see PingSender
 * @see PongSender
 */
class CounterPayloadGenerator implements PayloadGenerator {
  private long count;




  /**
   * @see coyote.commons.network.http.wsc.PayloadGenerator#generate()
   */
  @Override
  public byte[] generate() {
    return WebSocketUtil.getBytesUTF8(String.valueOf(increment()));
  }




  /**
   * @return the number of times this method was called. Overflows to 1, not 0.
   */
  private long increment() {
    count = Math.max(count + 1, 1);
    return count;
  }
  
}
