/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.writer;

import java.io.IOException;
import java.text.MessageFormat;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Writes a data frame as a simple XML string to either standard output 
 * (default) or standard error.
 * 
 * HEADER
 * &lt;xml version="1.0" encoding="windows-1252"?&gt;
 */
public class XMLWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  private final StringBuilder b = new StringBuilder();

  private String headerText = "<?xml version=\"1.0\">";
  private String footerText = "";
  private String rootElement = "dataset";
  private String rootAttributes = "";
  private String rowElement = "row";
  private String rowAttributes = "";
  private MessageFormat fieldFormat = null;




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#close()
   */
  @Override
  public void close() throws IOException {

    printwriter.write( "</" + rootElement + ">" );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.write( footerText );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Super class close always comes last
    super.close();
  }




  /**
   * @return the formatting for the field
   */
  public MessageFormat getFieldFormat() {
    return fieldFormat;
  }




  /**
   * @return the footerText
   */
  public String getFooterText() {
    return footerText;
  }




  /**
   * @return the headerText
   */
  public String getHeaderText() {
    return headerText;
  }




  /**
   * @return the rootAttributes
   */
  public String getRootAttributes() {
    return rootAttributes;
  }




  /**
   * @return the rootElement
   */
  public String getRootElement() {
    return rootElement;
  }




  /**
   * @return the rowAttributes
   */
  public String getRowAttributes() {
    return rowAttributes;
  }




  /**
   * @return the rowElement
   */
  public String getRowElement() {
    return rowElement;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( final TransformContext context ) {
    // Super class open always comes first!
    super.open( context );

    final String header = getString( ConfigTag.HEADER );
    if ( StringUtil.isNotBlank( header ) ) {
      setHeaderText( header );
    }

    final String footer = getString( ConfigTag.FOOTER );
    if ( StringUtil.isNotBlank( footer ) ) {
      setFooterText( footer );
    }

    final String rootE = getString( ConfigTag.ROOT_ELEMENT );
    if ( StringUtil.isNotBlank( rootE ) ) {
      setRootElement( rootE );
    }

    // just perform a case insensitive search for the configuration attribute 
    // and do not try to resolve it in the context or as a template...it will 
    // be resolved as a template later.
    final String rootA = findString( ConfigTag.ROOT_ATTRIBUTE );
    if ( StringUtil.isNotBlank( rootA ) ) {
      setRootAttributes( rootA );
    }

    final String rowE = getString( ConfigTag.ROW_ELEMENT );
    if ( StringUtil.isNotBlank( rowE ) ) {
      setRowElement( rowE );
    }

    // just perform a case insensitive search for the configuration attribute 
    // and do not try to resolve it in the context or as a template...it will 
    // be resolved as a template later.
    final String rowA = findString( ConfigTag.ROW_ATTRIBUTE );
    if ( StringUtil.isNotBlank( rowE ) ) {
      setRowAttributes( rowA );
    }

    final String format = getString( ConfigTag.FIELD_FORMAT );
    if ( StringUtil.isNotBlank( format ) ) {
      setFieldFormat( new MessageFormat( format ) );
    }

    printwriter.write( headerText );
    printwriter.write( StringUtil.LINE_FEED );
    final StringBuffer b = new StringBuffer( "<" );
    b.append( rootElement );
    if ( StringUtil.isNotBlank( rootAttributes ) ) {
      b.append( " " );
      b.append( Template.resolve( rootAttributes, context.getSymbols() ).trim() );
    }
    b.append( ">" );
    printwriter.write( b.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

  }




  /**
   * @param format the format string of the field to set
   */
  public void setFieldFormat( final MessageFormat format ) {
    fieldFormat = format;
  }




  /**
   * @param footerText the footerText to set
   */
  public void setFooterText( final String footerText ) {
    this.footerText = footerText;
  }




  /**
   * @param headerText the headerText to set
   */
  public void setHeaderText( final String headerText ) {
    this.headerText = headerText;
  }




  /**
   * @param rootAttributes the rootAttributes to set
   */
  public void setRootAttributes( final String rootAttributes ) {
    this.rootAttributes = rootAttributes;
  }




  /**
   * @param rootElement the rootElement to set
   */
  public void setRootElement( final String rootElement ) {
    this.rootElement = rootElement;
  }




  /**
   * @param rowAttributes the rowAttributes to set
   */
  public void setRowAttributes( final String rowAttributes ) {
    this.rowAttributes = rowAttributes;
  }




  /**
   * @param rowElement the rowElement to set
   */
  public void setRowElement( final String rowElement ) {
    this.rowElement = rowElement;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true...
        if ( evaluator.evaluateBoolean( expression ) ) {
          writeFrame( frame );
        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( CDX.MSG, "Writer.boolean_evaluation_error", expression, e.getMessage() ) );
      }
    } else {
      // Unconditionally writing frame
      writeFrame( frame );
    }

  }




  /**
   * This is where we actually write the frame.
   * 
   * @param frame the frame to be written
   */
  private void writeFrame( final DataFrame frame ) {

    // Clear out our buffer
    b.delete( 0, b.length() );

    // Start a new row
    b.append( "<" );
    b.append( rowElement );
    if ( StringUtil.isNotBlank( rowAttributes ) ) {
      b.append( " " );
      b.append( Template.resolve( rowAttributes, context.getSymbols() ).trim() );
    }

    b.append( ">" );
    b.append( StringUtil.LINE_FEED );

    // If we have a formatter, use it to format the row
    for ( final DataField field : frame.getFields() ) {
      if ( fieldFormat != null ) {
        // The args will always be {0}=Field Name, {1}=Field Type, {2}=Field Type Name, {3}=Object Value, {4}=String Value,
        final Object[] args = { field.getName(), field.getType(), field.getTypeName(), field.getObjectValue(), field.getStringValue() };
        b.append( fieldFormat.format( args ) );
      } else {
        b.append( "<" );
        b.append( field.getName() );
        b.append( ">" );
        b.append( field.getStringValue() );
        b.append( "</" );
        b.append( field.getName() );
        b.append( ">" );
      }
      b.append( StringUtil.LINE_FEED );
    }

    b.append( "</" );
    b.append( rowElement );
    b.append( ">" );

    printwriter.write( b.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
