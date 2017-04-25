package coyote.nmea.sentence;

import java.util.List;

import coyote.nmea.SatelliteInfo;
import coyote.nmea.Sentence;


/**
 * Detailed GPS satellite data; satellites in view, satellite elevation, 
 * azimuth and signal noise ratio (SNR). 
 * 
 * <p>GSV sentences are transmitted typically in groups of two or three 
 * sentences, depending on the number of satellites in view. Each GSV sentence 
 * may contain information about up to four satellites. The last sentence in 
 * sequence may contain empty satellite information fields. The empty fields 
 * may also be omitted, depending on the device model and manufacturer.
 * 
 * <p>Example:<pre>
 * $GPGSV,3,1,12,02,72,356,34,05,51,206,37,12,47,244,40,06,37,065,*7E
 * $GPGSV,3,2,12,51,37,214,34,25,35,293,28,19,30,122,21,29,19,311,22*71
 * $GPGSV,3,3,12,09,19,056,27,20,08,221,19,17,07,127,,23,05,034,*78</pre>
 */
public interface GSVSentence extends Sentence {

  /**
   * Get the number of satellites in view.
   * 
   * @return Satellite count
   */
  int getSatelliteCount();




  /**
   * Get the satellites information.
   * 
   * @return List of SatelliteInfo objects.
   */
  List<SatelliteInfo> getSatelliteInfo();




  /**
   * Get the total number of sentences in GSV sequence.
   * 
   * @return Number of sentences
   */
  int getSentenceCount();




  /**
   * Get the index of this sentence in GSV sequence.
   * 
   * @return Sentence index
   */
  int getSentenceIndex();




  /**
   * Tells if this is the first sentence in GSV sequence.
   * 
   * @return true if first, otherwise false.
   * 
   * @see #getSentenceCount()
   * @see #getSentenceIndex()
   */
  boolean isFirst();




  /**
   * Tells if this is the last sentence in GSV sequence. 
   * 
   * <p>This is a convenience method for comparison of<pre>
   * ({@link #getSentenceCount()} == {@link #getSentenceIndex()})</pre>.
   * 
   * @return {@code true} if first, otherwise {@code false}.
   */
  boolean isLast();




  /**
   * Set the number of satellites in view.
   * 
   * @param count Satellite count
   * 
   * @throws IllegalArgumentException If specified number is negative
   */
  void setSatelliteCount( int count );




  /**
   * Set the satellite information.
   * 
   * @param info List of SatelliteInfo objects, size from 0 to 4.
   * 
   * @throws IllegalArgumentException If specified list size is greater than
   *         the maximum allowed number of satellites per sentence (4).
   */
  void setSatelliteInfo( List<SatelliteInfo> info );




  /**
   * Set the total number of sentences in GSV sequence.
   * 
   * @param count Number of sentences
   * 
   * @throws IllegalArgumentException If specified count is negative
   */
  void setSentenceCount( int count );




  /**
   * Set the index of this sentence in GSV sequence.
   * 
   * @param index Sentence index to set
   * 
   * @throws IllegalArgumentException If specified index is negative
   */
  void setSentenceIndex( int index );

}
