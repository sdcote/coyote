package coyote.nmea.io;

/**
 * This is the base class for all data readers contributing shared logic.
 */
public abstract class AbstractDataReader implements DataReader {
  private final SentenceReader parent;




  /**
   * Creates a new instance linked to a particular {@link SentenceReader}.
   * 
   * @param parent {@link SentenceReader} that owns this reader
   */
  public AbstractDataReader( SentenceReader parent ) {
    this.parent = parent;
  }




  /**
   * @return the parent  {@link SentenceReader} to which this data reader belongs.
   */
  protected SentenceReader getParent() {
    return this.parent;
  }

}
