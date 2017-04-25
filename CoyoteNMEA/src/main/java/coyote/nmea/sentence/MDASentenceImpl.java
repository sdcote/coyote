package coyote.nmea.sentence;

import coyote.nmea.TalkerId;


/**
 * Meteorological Composite - Barometric pressure, air and water temperature,
 * humidity, dew point and wind speed and direction relative to the surface of
 * the earth.
 */
class MDASentenceImpl extends AbstractSentence implements MDASentence {

  public static final String MDA_SENTENCE_ID = "MDA";

  /**
   * Barometric pressure, inches of mercury, to the nearest 0,01 inch.
   */
  private static int PRIMARY_BAROMETRIC_PRESSURE = 0;

  /**
   * I = inches of mercury (inHg) P = pascal (1 bar = 100000 Pa = 29,53 inHg).
   */
  private static int PRIMARY_BAROMETRIC_PRESSURE_UNIT = 1;

  /**
   * Barometric pressure, bars, to the nearest .001 bar.
   */
  private static int SECONDARY_BAROMETRIC_PRESSURE = 2;

  /**
   * B = bars.
   */
  private static int SECONDARY_BAROMETRIC_PRESSURE_UNIT = 3;

  /**
   * Air temperature, degrees C, to the nearest 0,1 degree C.
   */
  private static int AIR_TEMPERATURE = 4;

  /**
   * C = degrees C.
   */
  private static int AIR_TEMPERATURE_UNIT = 5;

  /**
   * Water temperature, degrees C.
   */
  private static int WATER_TEMPERATURE = 6;

  /**
   * C = degrees C.
   */
  private static int WATER_TEMPERATURE_UNIT = 7;

  /**
   * Relative humidity, percent, to the nearest 0,1 percent.
   */
  private static int RELATIVE_HUMIDITY = 8;

  /**
   * Absolute humidity, percent .
   */
  private static int ABSOLUTE_HUMIDITY = 9;

  /**
   * Dew point, degrees C, to the nearest 0,1 degree C.
   */
  private static int DEW_POINT = 10;

  /**
   * C = degrees C.
   */
  private static int DEW_POINT_UNIT = 11;

  /**
   * Wind direction, degrees True, to the nearest 0,1 degree.
   */
  private static int WIND_DIRECTION_TRUE = 12;

  /**
   * T = true
   */
  private static int WIND_DIRECTION_TRUE_UNIT = 13;

  /**
   * Wind direction, degrees Magnetic, to the nearest 0,1 degree.
   */
  private static int WIND_DIRECTION_MAGNETIC = 14;

  /**
   * M = magnetic.
   */
  private static int WIND_DIRECTION_MAGNETIC_UNIT = 15;

  /**
   * Wind speed, knots, to the nearest 0,1 knot.
   */
  private static int WIND_SPEED_KNOTS = 16;

  /**
   * N = knots.
   */
  private static int WIND_SPEED_KNOTS_UNIT = 17;

  /**
   * Wind speed, meters per second, to the nearest 0,1 m/s.
   */
  private static int WIND_SPEED_METERS = 18;

  /**
   * M = meters per second
   */
  private static int WIND_SPEED_METERS_UNIT = 19;




  /**
   * Creates a new instance of an MWV sentence.
   * 
   * @param nmea
   *            MWV sentence String
   */
  public MDASentenceImpl( String nmea ) {
    super( nmea, MDA_SENTENCE_ID );
  }




  /**
   * Creates a new empty instance of a MWV sentence.
   * 
   * @param talker Talker id to set
   */
  public MDASentenceImpl( TalkerId talker ) {
    super( talker, MDA_SENTENCE_ID, 20 );
    setCharValue( AIR_TEMPERATURE_UNIT, 'C' );
    setCharValue( WATER_TEMPERATURE_UNIT, 'C' );
    setCharValue( DEW_POINT_UNIT, 'C' );
    setCharValue( WIND_DIRECTION_TRUE_UNIT, 'T' );
    setCharValue( WIND_DIRECTION_MAGNETIC_UNIT, 'M' );
    setCharValue( WIND_SPEED_KNOTS_UNIT, 'K' );
    setCharValue( WIND_SPEED_METERS_UNIT, 'M' );
    setCharValue( PRIMARY_BAROMETRIC_PRESSURE_UNIT, 'I' );
    setCharValue( SECONDARY_BAROMETRIC_PRESSURE_UNIT, 'B' );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getAbsoluteHumidity()
   */
  @Override
  public double getAbsoluteHumidity() {
    if ( hasValue( ABSOLUTE_HUMIDITY ) ) {
      return getDoubleValue( ABSOLUTE_HUMIDITY );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getAirTemperature()
   */
  @Override
  public double getAirTemperature() {
    if ( hasValue( AIR_TEMPERATURE ) ) {
      return getDoubleValue( AIR_TEMPERATURE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getDewPoint()
   */
  @Override
  public double getDewPoint() {
    if ( hasValue( DEW_POINT ) ) {
      return getDoubleValue( DEW_POINT );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getMagneticWindDirection()
   */
  @Override
  public double getMagneticWindDirection() {
    if ( hasValue( WIND_DIRECTION_MAGNETIC ) ) {
      return getDoubleValue( WIND_DIRECTION_MAGNETIC );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getPrimaryBarometricPressure()
   */
  @Override
  public double getPrimaryBarometricPressure() {
    if ( hasValue( PRIMARY_BAROMETRIC_PRESSURE ) ) {
      return getDoubleValue( PRIMARY_BAROMETRIC_PRESSURE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getPrimaryBarometricPressureUnit()
   */
  @Override
  public char getPrimaryBarometricPressureUnit() {
    return getCharValue( PRIMARY_BAROMETRIC_PRESSURE_UNIT );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getRelativeHumidity()
   */
  @Override
  public double getRelativeHumidity() {
    if ( hasValue( RELATIVE_HUMIDITY ) ) {
      return getDoubleValue( RELATIVE_HUMIDITY );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getSecondaryBarometricPressure()
   */
  @Override
  public double getSecondaryBarometricPressure() {
    if ( hasValue( SECONDARY_BAROMETRIC_PRESSURE ) ) {
      return getDoubleValue( SECONDARY_BAROMETRIC_PRESSURE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getSecondaryBarometricPressureUnit()
   */
  @Override
  public char getSecondaryBarometricPressureUnit() {
    return getCharValue( SECONDARY_BAROMETRIC_PRESSURE_UNIT );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getTrueWindDirection()
   */
  @Override
  public double getTrueWindDirection() {
    if ( hasValue( WIND_DIRECTION_TRUE ) ) {
      return getDoubleValue( WIND_DIRECTION_TRUE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getWaterTemperature()
   */
  @Override
  public double getWaterTemperature() {
    if ( hasValue( WATER_TEMPERATURE ) ) {
      return getDoubleValue( WATER_TEMPERATURE );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getWindSpeed()
   */
  @Override
  public double getWindSpeed() {
    if ( hasValue( WIND_SPEED_METERS ) ) {
      return getDoubleValue( WIND_SPEED_METERS );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#getWindSpeedKnots()
   */
  @Override
  public double getWindSpeedKnots() {
    if ( hasValue( WIND_SPEED_KNOTS ) ) {
      return getDoubleValue( WIND_SPEED_KNOTS );
    } else {
      return Double.NaN;
    }
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setAbsoluteHumidity(double)
   */
  @Override
  public void setAbsoluteHumidity( double humitidy ) {
    setDoubleValue( ABSOLUTE_HUMIDITY, humitidy );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setAirTemperature(double)
   */
  @Override
  public void setAirTemperature( double temp ) {
    setDoubleValue( AIR_TEMPERATURE, temp );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setDewPoint(double)
   */
  @Override
  public void setDewPoint( double dewPoint ) {
    setDoubleValue( DEW_POINT, dewPoint );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setMagneticWindDirection(double)
   */
  @Override
  public void setMagneticWindDirection( double direction ) {
    setDoubleValue( WIND_DIRECTION_MAGNETIC, direction );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setPrimaryBarometricPressure(double)
   */
  @Override
  public void setPrimaryBarometricPressure( double pressure ) {
    setDoubleValue( PRIMARY_BAROMETRIC_PRESSURE, pressure );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setPrimaryBarometricPressureUnit(char)
   */
  @Override
  public void setPrimaryBarometricPressureUnit( char unit ) {
    setCharValue( PRIMARY_BAROMETRIC_PRESSURE_UNIT, unit );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setRelativeHumidity(double)
   */
  @Override
  public void setRelativeHumidity( double humidity ) {
    setDoubleValue( RELATIVE_HUMIDITY, humidity );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setSecondaryBarometricPressure(double)
   */
  @Override
  public void setSecondaryBarometricPressure( double pressure ) {
    setDoubleValue( SECONDARY_BAROMETRIC_PRESSURE, pressure );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setSecondaryBarometricPressureUnit(char)
   */
  @Override
  public void setSecondaryBarometricPressureUnit( char unit ) {
    setCharValue( SECONDARY_BAROMETRIC_PRESSURE_UNIT, unit );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setTrueWindDirection(double)
   */
  @Override
  public void setTrueWindDirection( double direction ) {
    setDoubleValue( WIND_DIRECTION_TRUE, direction );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setWaterTemperature(double)
   */
  @Override
  public void setWaterTemperature( double temp ) {
    setDoubleValue( WATER_TEMPERATURE, temp );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setWindSpeed(double)
   */
  @Override
  public void setWindSpeed( double speed ) {
    setDoubleValue( WIND_SPEED_METERS, speed );
  }




  /**
   * @see coyote.nmea.sentence.MDASentence#setWindSpeedKnots(double)
   */
  @Override
  public void setWindSpeedKnots( double speed ) {
    setDoubleValue( WIND_SPEED_KNOTS, speed );
  }
}
