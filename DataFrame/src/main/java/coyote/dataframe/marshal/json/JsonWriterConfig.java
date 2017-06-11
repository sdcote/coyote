package coyote.dataframe.marshal.json;

import java.io.Writer;


/**
 * Controls the formatting of the JSON output. Use one of the available constants.
 */
public abstract class JsonWriterConfig {

  /**
   * Write JSON in its minimal form, without any additional whitespace. This is the default.
   */
  public static JsonWriterConfig MINIMAL = new JsonWriterConfig() {
    @Override
    public JsonWriter createWriter( final Writer writer ) {
      return new JsonWriter( writer );
    }
  };

  /**
   * Generate a nicely formatted (and indented) JSON string from the given data 
   * frame.
   */
  public static JsonWriterConfig FORMATTED = new JsonWriterConfig() {
    @Override
    public JsonWriter createWriter( final Writer writer ) {
      return new FormattedJsonWriter( writer );
    }
  };




  public abstract JsonWriter createWriter( Writer writer );

}
