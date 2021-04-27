package coyote.commons.network.http.wsc;

/**
 * Payload generator.
 */
public interface PayloadGenerator {

  /**
   * Generate a payload of a frame.
   *
   * <p>Note that the maximum payload length of control frames (e.g. ping 
   * frames) is 125 in bytes. Therefore, the length of a byte array returned 
   * from this method must not exceed 125 bytes.
   *
   * @return A payload of a frame.
   */
  byte[] generate();

}
