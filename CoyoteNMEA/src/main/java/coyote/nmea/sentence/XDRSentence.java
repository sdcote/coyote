package coyote.nmea.sentence;

import java.util.List;

import coyote.nmea.Measurement;
import coyote.nmea.Sentence;


/**
 * Transducer measurements. 
 * 
 * <p>Measurements are delivered in sets containing four fields; transducer 
 * type, measurement value, unit of measurement and transducer name. There may 
 * be any number of sets like this, each describing a sensor. Notice that 
 * inserting too many measuments in one sentence may result in exceeding the 
 * maximum sentence length (82 chars).
 * 
 * @see Measurement
 */
public interface XDRSentence extends Sentence {

  /**
   * Adds specified measurement in sentence placing it last. 
   * 
   * <p>Multiple measurements are inserted in the order given.
   * 
   * @param m Measurements to add.
   */
  void addMeasurement( Measurement... m );




  /**
   * @return List of measurements, ordered as they appear in sentence.
   */
  List<Measurement> getMeasurements();




  /**
   * Set single measurement. 
   * 
   * <p>Overwrites all existing values and adjusts the number of data fields to 
   * minimum required by one measurement (4).
   * 
   * @param m Measurement to set.
   */
  void setMeasurement( Measurement m );




  /**
   * Set multiple measurements in the given order. 
   * 
   * <p>Overwrites all existing values and adjusts the number of data fields as 
   * required by given measurements.
   * 
   * @param measurements List of measurements to set.
   */
  void setMeasurements( List<Measurement> measurements );
}
