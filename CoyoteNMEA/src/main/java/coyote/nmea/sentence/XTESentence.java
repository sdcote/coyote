package coyote.nmea.sentence;

import coyote.nmea.DataStatus;
import coyote.nmea.Direction;
import coyote.nmea.FaaMode;
import coyote.nmea.Sentence;

/**
 * Measured cross-track error when navigating towards waypoint.
 */
public interface XTESentence extends Sentence {

	/**
	 * Returns the Loran-C cycle lock status, not used for GPS.
	 */
	DataStatus getCycleLockStatus();

	/**
	 * Returns the cross-track error magnitude/distance.
	 * 
	 * @return Cross-track error distance in nautical miles
	 */
	double getMagnitude();

	/**
	 * Returns the FAA mode. Optional, NMEA 2.3 and later.
	 * 
	 * @return FaaMode
	 */
	FaaMode getMode();

	/**
	 * Returns the signal/fix status.
	 * 
	 * @return DataStatus
	 */
	DataStatus getStatus();

	/**
	 * Returns the direction in which to steer in order to get back on route.
	 * 
	 * @return {@link Direction#LEFT} or {@link Direction#RIGHT}
	 */
	Direction getSteerTo();

	/**
	 * Sets the Loran-C cycle lock status. Not used for GPS, may be omitted or
	 * {@link DataStatus#VOID}.
	 * 
	 * @param status DataStatus to set
	 */
	void setCycleLockStatus(DataStatus status);

	/**
	 * Sets the cross-track error magnitude/distance.
	 * 
	 * @param distance Cross-track error distance in nautical miles
	 */
	void setMagnitude(double distance);

	/**
	 * Sets the FAA mode. Optional, NMEA 2.3 and later.
	 * 
	 * @param mode FaaMode to set
	 */
	void setMode(FaaMode mode);

	/**
	 * Sets the signal/fix status.
	 * 
	 * @param status DataStatus to set
	 */
	void setStatus(DataStatus status);

	/**
	 * Set direction in which to steer in order to get back on route.
	 * 
	 * @param direction {@link Direction#RIGHT} or {@link Direction#LEFT}
	 */
	void setSteerTo(Direction direction);

}