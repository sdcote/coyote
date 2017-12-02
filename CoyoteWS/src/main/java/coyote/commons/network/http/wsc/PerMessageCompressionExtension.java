package coyote.commons.network.http.wsc;

/**
 * Per-Message Compression Extension.
 * 
 * <p>See <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>
 *
 * @see <a href="https://tools.ietf.org/html/rfc7692">RFC 7692</a>
 */
abstract class PerMessageCompressionExtension extends WebSocketExtension {
  public PerMessageCompressionExtension(final String name) {
    super(name);
  }




  public PerMessageCompressionExtension(final WebSocketExtension source) {
    super(source);
  }




  /**
   * Compress the plain message.
   */
  protected abstract byte[] compress(byte[] plain) throws WebSocketException;




  /**
   * Decompress the compressed message.
   */
  protected abstract byte[] decompress(byte[] compressed) throws WebSocketException;

}
