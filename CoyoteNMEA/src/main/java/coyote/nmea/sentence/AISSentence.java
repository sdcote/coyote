package coyote.nmea.sentence;

import coyote.nmea.Sentence;


/**
 * Base interface for all AIS sentences (Automatic Identification System).
 * 
 * <p>AIS does not refer to NMEA sentence type, but another system/standard 
 * that transmits it's messages using NMEA 0183.
 * 
 * <p>AIS sentences are parsed in two phases and they all share the same NMEA
 * sentence layout, so there is no dedicated interfaces for each AIS sentence
 * type (VDM, VDO etc). Example:
 * <pre>!AIVDM,1,1,,B,177KQJ5000G?tO`K&gt;RA1wUbN0TKH,0*5C</pre>
 */
public interface AISSentence extends Sentence {

  /**
   * Number of fragments in the currently accumulating message.
   * 
   * @return number of fragments.
   */
  public int getNumberOfFragments();




  /**
   * Returns the fragment number of this sentence (1-based).
   * 
   * @return fragment index
   */
  public int getFragmentNumber();




  /**
   * Returns the sequential message ID for multi-sentence messages.
   * 
   * @return sequential message ID
   */
  public String getMessageId();




  /**
   * Returns the radio channel information of the messsage.
   * 
   * @return radio channel id
   */
  public String getRadioChannel();




  /**
   * Returns the raw 6-bit decoded message.
   * 
   * @return message body
   */
  public String getPayload();




  /**
   * Returns the number of fill bits required to pad the data payload to a 6
   * bit boundary, ranging from 0 to 5.
   * 
   * Equivalently, subtracting 5 from this tells how many least significant
   * bits of the last 6-bit nibble in the data payload should be ignored.
   * 
   * @return number of fill bits
   */
  public int getFillBits();




  /**
   * Tells if the AIS message is being delivered over multiple sentences.
   * 
   * @return true if this sentence is part of a sequence
   */
  public boolean isFragmented();




  /**
   * Tells if this is the first fragment in message sequence.
   * 
   * @return true if first fragment in sequence
   */
  public boolean isFirstFragment();




  /**
   * Tells if this is the last fragment in message sequence.
   * 
   * @return true if last part of a sequence
   */
  public boolean isLastFragment();




  /**
   * <p>
   * Tells if given sentence is part of message sequence.
   * 
   * <p>
   * Sentences are considered to belong in same sequence when the given
   * sentence meets the following conditions:
   * 
   * <ul>
   * <li>Same number of fragments, higher fragment #, same channel and same
   * message id</li>
   * <li>Same number of fragments, next fragment #, and either same channel or
   * same message id</li>
   * </ul>
   * 
   * @param sentence AISSentence to compare with.
   * @return true if this and given sentence belong in same sequence
   */
  public boolean isPartOfMessage( AISSentence sentence );

}
