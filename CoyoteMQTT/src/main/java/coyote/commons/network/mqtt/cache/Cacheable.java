package coyote.commons.network.mqtt.cache;

/**
 * Represents an object used to pass data to be cached using {@link 
 * coyote.commons.network.mqtt.cache.ClientCache ClientCache} interface.
 * 
 * <p>When data is passed across the interface the header and payload are 
 * separated, so that unnecessary message copies may be avoided. For example, 
 * if a 10 MB payload was published it would be inefficient to create a byte 
 * array a few bytes larger than 10 MB and copy the MQTT message header and 
 * payload into a contiguous byte array.</p>
 * 
 * <p>When the request to persist data is made a separate byte array and offset
 * is passed for the header and payload. Only the data between offset and 
 * length need be persisted. So for example, a message to be persisted consists 
 * of a header byte array starting at offset 1 and length 4, plus a payload 
 * byte array starting at offset 30 and length 40000. There are three ways in 
 * which the persistence implementation may return data to the client on 
 * recovery:<ul>
 * <li>It could return the data as it was passed in originally, with the same 
 * byte arrays and offsets.</li>
 * <li>It could safely just persist and return the bytes from the offset for 
 * the specified length. For example, return a header byte array with offset 0 
 * and length 4, plus a payload byte array with offset 0 and length 40000</li>
 * <li>It could return the header and payload as a contiguous byte array with 
 * the header bytes preceding the payload. The contiguous byte array should be 
 * set as the header byte array, with the payload byte array being null. For 
 * example, return a single byte array with offset 0 and length 40004. This is 
 * useful when recovering from a file where the header and payload could be 
 * written as a contiguous stream of bytes.</li></ul>  
 */
public interface Cacheable {

  /**
   * Returns the header bytes in an array.
   * 
   * <p>The bytes start at {@link #getHeaderOffset()} and continue for {@link 
   * #getHeaderLength()}.</p>
   * 
   * @return the header bytes. 
   */
  public byte[] getHeaderBytes() throws CacheException;




  /**
   * Returns the length of the header.
   * 
   * @return the header length
   */
  public int getHeaderLength() throws CacheException;




  /**
   * Returns the offset of the header within the byte array returned by {@link 
   * #getHeaderBytes()}.
   * 
   * @return the header offset.
   */
  public int getHeaderOffset() throws CacheException;




  /**
   * Returns the payload bytes in an array.
   * 
   * <p>The bytes start at {@link #getPayloadOffset()}* and continue for {@link 
   * #getPayloadLength()}.</p>
   * 
   * @return the payload bytes.  
   */
  public byte[] getPayloadBytes() throws CacheException;




  /**
   * @return the payload length.
   */
  public int getPayloadLength() throws CacheException;




  /**
   * Returns the offset of the payload within the byte array returned by {@link 
   * #getPayloadBytes()}.
   * 
   * @return the payload offset.
   */
  public int getPayloadOffset() throws CacheException;
}
