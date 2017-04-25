package coyote.nmea;

/**
 * Defines the supported route types.
 */
public enum RouteType {

  /**
   * Active route: complete, all waypoints in route order.
   */
  ACTIVE( 'c'),

  /**
   * Working route: the waypoint you just left, the waypoint you're heading to
   * and then all the rest.
   */
  WORKING( 'w');

  private final char chr;




  private RouteType( char c ) {
    chr = c;
  }




  /**
   * Get the corresponding char indicator of enum.
   * 
   * @return Char
   */
  public char toChar() {
    return chr;
  }




  /**
   * Get the char indicator corresponding to enum.
   * 
   * @param ch Char
   * 
   * @return ReturnType corresponding to specified char.
   */
  public RouteType valueOf( char ch ) {
    for ( RouteType type : values() ) {
      if ( type.toChar() == ch ) {
        return type;
      }
    }
    return valueOf( String.valueOf( ch ) );
  }

}
