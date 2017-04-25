package coyote.nmea;



/**
 * This exception is thrown when the data appear to be a sentence but the ID is 
 * not found in the sentence implementation. 
 */
public class UnsupportedSentenceException extends RuntimeException {

  private static final long serialVersionUID = 4892969421375060369L;
  private Sentence sentence = null;




  public UnsupportedSentenceException( String msg, Sentence sentence ) {
    super( msg );
    this.sentence = sentence;
  }




  /**
   * @return the sentence
   */
  public Sentence getSentence() {
    return sentence;
  }

}
