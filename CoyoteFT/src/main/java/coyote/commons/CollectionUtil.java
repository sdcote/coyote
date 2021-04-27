package coyote.commons;

import java.util.Collection;
import java.util.Map;


public class CollectionUtil {
  /**
   * Null-safe check if the specified collection is empty.
   * 
   * <p>Null returns true.</p>
   * 
   * @param coll the collection to check, may be null
   * 
   * @return true if empty or null
   */
  public static boolean isEmpty( Collection coll ) {
    return ( coll == null || coll.isEmpty() );
  }




  /**
   * Null-safe check if the specified collection is not empty.
   * 
   * <p>Null returns false.</p>
   * 
   * @param coll the collection to check, may be null
   * 
   * @return true if non-null and non-empty
   */
  public static boolean isNotEmpty( Collection coll ) {
    return !isEmpty( coll );
  }




  /**
   * Null-safe check if the specified map is empty.
   * 
   * <p>Null returns true.</p>
   * 
   * @param map the map to check, may be null
   * 
   * @return true if empty or null
   */
  public static boolean isEmpty( Map map ) {
    return ( map == null || map.isEmpty() );
  }




  /**
   * Null-safe check if the specified map is not empty.
   * 
   * <p>Null returns false.</p>
   * 
   * @param map the map to check, may be null
   * 
   * @return true if non-null and non-empty
   */
  public static boolean isNotEmpty( Map map ) {
    return !isEmpty( map );
  }

}
