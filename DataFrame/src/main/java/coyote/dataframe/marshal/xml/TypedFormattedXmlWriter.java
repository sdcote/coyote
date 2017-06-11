package coyote.dataframe.marshal.xml;

import java.io.IOException;
import java.io.Writer;

import coyote.dataframe.DataField;


public class TypedFormattedXmlWriter extends FormattedXmlWriter {

  private static final String TYPE = "type";




  /**
   * @param writer
   */
  TypedFormattedXmlWriter( final Writer writer ) {
    super( writer );
  }




  /**
   * @see coyote.dataframe.marshal.xml.XmlWriter#writeFieldType(coyote.dataframe.DataField)
   */
  @Override
  public void writeFieldType( final DataField field ) throws IOException {
    if ( !field.isFrame() ) {
      writeSpace();
      writeLiteral( TYPE );
      writeEquals();
      writeString( field.getTypeName() );
    }
  }

}
