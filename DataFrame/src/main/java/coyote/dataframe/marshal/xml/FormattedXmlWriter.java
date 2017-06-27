package coyote.dataframe.marshal.xml;

import java.io.IOException;
import java.io.Writer;


class FormattedXmlWriter extends XmlWriter {

  private final char[] indentChars = { ' ', ' ' };
  private int indent;




  FormattedXmlWriter( final Writer writer ) {
    super( writer );
  }




  @Override
  public void writeFieldClose() throws IOException {
    writeNewLine();
  }




  @Override
  public void writeFieldOpen() throws IOException {
    writeIndent();
  }




  @Override
  public void writeFrameClose() throws IOException {
    writer.write( "</frame>" );
    indent--;
    writeIndent();
  }




  @Override
  public void writeFrameOpen() throws IOException {
    writer.write( "<frame>" );
    indent++;
    writeNewLine();
  }




  public void writeIndent() throws IOException {
    for ( int i = 0; i < indent; i++ ) {
      writer.write( indentChars );
    }
  }




  private void writeNewLine() throws IOException {
    writer.write( '\n' );
  }

}
