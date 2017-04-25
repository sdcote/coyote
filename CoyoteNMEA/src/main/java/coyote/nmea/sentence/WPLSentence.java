package coyote.nmea.sentence;

import coyote.nmea.DataNotAvailableException;
import coyote.nmea.ParseException;
import coyote.nmea.Sentence;
import coyote.nmea.Waypoint;

/**
 * Destination waypoint location and ID. 
 * 
 * <p>This sentence is transmitted by some (e.g. Garmin) GPS models in GOTO 
 * mode. Example:<pre>
 * $GPWPL,5536.200,N,01436.500,E,RUSKI*1F</pre>
 */
public interface WPLSentence extends Sentence {

	/**
	 * Get the destination waypoint.
	 * 
	 * @return Waypoint
	 * 
	 * @throws DataNotAvailableException If any of the waypoint related data is 
	 *         not available.
	 * @throws ParseException If any of the waypoint related fields contain 
	 *         unexpected or illegal value.
	 */
	Waypoint getWaypoint();

	/**
	 * Set the destination waypoint.
	 * 
	 * @param wp Waypoint to set
	 */
	void setWaypoint(Waypoint wp);

}
