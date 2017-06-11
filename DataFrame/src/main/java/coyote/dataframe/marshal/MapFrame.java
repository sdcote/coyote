package coyote.dataframe.marshal;

import java.util.HashMap;
import java.util.Map;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


public class MapFrame {

  /**
   * Create a DataFrame from the given map.
   * 
   * <p>If a map contains other maps, then the DataFrame will contain other 
   * DataFrames.
   * 
   * @param source
   * 
   * @return A dataframe version of the given Map.
   */
  @SuppressWarnings("rawtypes")
  public DataFrame marshal( Map<?, ?> source ) {
    DataFrame retval = new DataFrame();

    if ( source != null ) {

      for ( Map.Entry<?, ?> entry : source.entrySet() ) {

        String key = entry.getKey().toString();

        Object value = entry.getValue();

        if ( value != null ) {
          if ( value instanceof Map ) {
            value = marshal( (Map)value );
          }
          retval.add( key, value );
        }

      }
    }
    return retval;
  }




  /**
   * Return the given dataframe as a map of values.
   * 
   * <p>The hierarchy is represented as a Map of Maps.
   * 
   * <p>Only types supported by DataFrame will be placed in the Map.
   * 
   * @param frame The frame from which data is to be retrieved
   * 
   * @return A map of objects
   */
  @SuppressWarnings("rawtypes")
  public Map<?, ?> marshal( DataFrame frame ) {
    Map retval = new HashMap();
    if ( frame != null )
      retval = recurse( frame, retval );
    return retval;
  }




  /**
   * Recurse into the a dataframe, building a Map as it goes.
   * 
   * @param frame The frame being recursed into, providing data for the Map 
   * @param map The Map into which values are placed.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static Map recurse( DataFrame frame, Map<?, ?> map ) {
    Map retval = new HashMap();
    if ( frame != null ) {

      for ( int x = 0; x < frame.getFieldCount(); x++ ) {
        final DataField field = frame.getField( x );
        String fname = field.getName();

        if ( fname == null )
          fname = "field" + x;

        if ( field.isFrame() )
          retval.put( fname, recurse( (DataFrame)field.getObjectValue(), map ) );
        else
          retval.put( fname, field.getObjectValue() );

      } // for each field

    } // frame !null

    return retval;
  }

}
