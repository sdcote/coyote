package coyote.commons.network.mqtt;

/**
 * Holds the set of options that govern the behavior of off-line (disconnected) 
 * caching of messages.
 */
public class DisconnectedBufferOptions {

  /** The default size of the disconnected buffer */
  public static final int DISCONNECTED_BUFFER_SIZE_DEFAULT = 5000;
  public static final boolean DISCONNECTED_BUFFER_ENABLED_DEFAULT = false;
  public static final boolean PERSIST_DISCONNECTED_BUFFER_DEFAULT = false;
  public static final boolean DELETE_OLDEST_MESSAGES_DEFAULT = false;
  private int bufferSize = DISCONNECTED_BUFFER_SIZE_DEFAULT;
  private boolean bufferEnabled = DISCONNECTED_BUFFER_ENABLED_DEFAULT;
  private boolean persistBuffer = PERSIST_DISCONNECTED_BUFFER_DEFAULT;
  private boolean deleteOldestMessages = DELETE_OLDEST_MESSAGES_DEFAULT;




  /**
   * Constructs a new <code>DisconnectedBufferOptions</code> object using the
   * default values.
   *
   * <p>The defaults are:<ul>
   * <li>The disconnected buffer is disabled</li>
   * <li>The buffer holds 5000 messages</li>
   * <li>The buffer is not persisted</li>
   * <li>Once the buffer is full, old messages are not deleted</li>
   * </ul>
   */
  public DisconnectedBufferOptions() {}




  public int getBufferSize() {
    return bufferSize;
  }




  public boolean isBufferEnabled() {
    return bufferEnabled;
  }




  public boolean isDeleteOldestMessages() {
    return deleteOldestMessages;
  }




  public boolean isPersistBuffer() {
    return persistBuffer;
  }




  public void setBufferEnabled( final boolean enabled ) {
    bufferEnabled = enabled;
  }




  public void setBufferSize( final int size ) {
    if ( size < 1 ) {
      throw new IllegalArgumentException();
    }
    bufferSize = size;
  }




  public void setDeleteOldestMessages( final boolean delete ) {
    deleteOldestMessages = delete;
  }




  public void setPersistBuffer( final boolean persist ) {
    persistBuffer = persist;
  }

}
