package coyote.dataframe.marshal.xml;

import java.io.Writer;


/**
 * Controls the formatting of the XML output. Use one of the available constants.
 */
public abstract class XmlWriterConfig {

  /**
   * Write XML in its minimal form, without any additional whitespace. 
   * 
   * This is the default.
   */
  public static XmlWriterConfig MINIMAL = new XmlWriterConfig() {
    @Override
    public XmlWriter createWriter( final Writer writer ) {
      return new XmlWriter( writer );
    }
  };

  /**
   * Write XML in its minimal form, with the addition of type information for 
   * each field. 
   */
  public static XmlWriterConfig TYPED = new XmlWriterConfig() {
    @Override
    public TypedXmlWriter createWriter( final Writer writer ) {
      return new TypedXmlWriter( writer );
    }
  };

  /**
   * Write formated XML, with each value on a separate line and an indentation 
   * of two spaces.
   */
  public static XmlWriterConfig FORMATTED = new XmlWriterConfig() {
    @Override
    public XmlWriter createWriter( final Writer writer ) {
      return new FormattedXmlWriter( writer );
    }
  };

  /**
   * Write formated XML, with each value on a separate line, an indentation of 
   * two spaces with type information.
   */
  public static XmlWriterConfig TYPED_FORMATTED = new XmlWriterConfig() {
    @Override
    public TypedFormattedXmlWriter createWriter( final Writer writer ) {
      return new TypedFormattedXmlWriter( writer );
    }
  };




  public abstract XmlWriter createWriter( Writer writer );

}
