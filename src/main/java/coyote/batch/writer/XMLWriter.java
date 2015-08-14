/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.writer;

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FrameWriter;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * Writes a data frame as a simple XML string to either standard output 
 * (default) or standard error.
 * 
 * HEADER
 * <?xml version="1.0" encoding="windows-1252"?>
 */
public class XMLWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  private final StringBuilder b = new StringBuilder();

  private String headerText = "<?xml version=\"1.0\">";
  private String footerText = "";
  private String rootElement = "dataset";
  private String rootAttributes = "";
  private String rowElement = "row";
  private String rowAttributes = "";
  private MessageFormat fieldFormat = null;




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    // Super class open always comes first!
    super.open( context );

    String header = getString( ConfigTag.HEADER );
    if ( StringUtil.isNotBlank( header ) ) {
      this.setHeaderText( header );
    }

    String footer = getString( ConfigTag.FOOTER );
    if ( StringUtil.isNotBlank( footer ) ) {
      this.setFooterText( footer );
    }

    String rootE = getString( ConfigTag.ROOT_ELEMENT );
    if ( StringUtil.isNotBlank( rootE ) ) {
      this.setRootElement( rootE );
    }

    // just perform a case insensitive search for the configuration attribute 
    // and do not try to resolve it in the context or as a template...it will 
    // be resolved as a template later.
    String rootA = findString( ConfigTag.ROOT_ATTRIBUTE );
    if ( StringUtil.isNotBlank( rootA ) ) {
      this.setRootAttributes( rootA );
    }

    String rowE = getString( ConfigTag.ROW_ELEMENT );
    if ( StringUtil.isNotBlank( rowE ) ) {
      this.setRowElement( rowE );
    }

    // just perform a case insensitive search for the configuration attribute 
    // and do not try to resolve it in the context or as a template...it will 
    // be resolved as a template later.
    String rowA = findString( ConfigTag.ROW_ATTRIBUTE );
    if ( StringUtil.isNotBlank( rowE ) ) {
      this.setRowAttributes( rowA );
    }

    String format = getString( ConfigTag.FIELD_FORMAT );
    if ( StringUtil.isNotBlank( format ) ) {
      this.setFieldFormat( new MessageFormat( format ) );
    }

    printwriter.write( headerText );
    printwriter.write( StringUtil.LINE_FEED );
    StringBuffer b = new StringBuffer( "<" );
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
   * @see coyote.batch.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

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
    for ( DataField field : frame.getFields() ) {
      if ( fieldFormat != null ) {
        // The args will always be {0}=Field Name, {1}=Field Type, {2}=Field Type Name, {3}=Object Value, {4}=String Value,
        Object[] args = { field.getName(), field.getType(), field.getTypeName(), field.getObjectValue(), field.getStringValue() };
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




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#close()
   */
  @Override
  public void close() throws IOException {

    printwriter.write( "</" + rootElement + ">" );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.write( footerText );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    System.out.println( context.getSymbols().dump() );

    // Super class close always comes last
    super.close();
  }




  /**
   * @return the headerText
   */
  public String getHeaderText() {
    return headerText;
  }




  /**
   * @param headerText the headerText to set
   */
  public void setHeaderText( String headerText ) {
    this.headerText = headerText;
  }




  /**
   * @return the footerText
   */
  public String getFooterText() {
    return footerText;
  }




  /**
   * @param footerText the footerText to set
   */
  public void setFooterText( String footerText ) {
    this.footerText = footerText;
  }




  /**
   * @return the rootElement
   */
  public String getRootElement() {
    return rootElement;
  }




  /**
   * @param rootElement the rootElement to set
   */
  public void setRootElement( String rootElement ) {
    this.rootElement = rootElement;
  }




  /**
   * @return the rootAttributes
   */
  public String getRootAttributes() {
    return rootAttributes;
  }




  /**
   * @param rootAttributes the rootAttributes to set
   */
  public void setRootAttributes( String rootAttributes ) {
    this.rootAttributes = rootAttributes;
  }




  /**
   * @return the rowElement
   */
  public String getRowElement() {
    return rowElement;
  }




  /**
   * @param rowElement the rowElement to set
   */
  public void setRowElement( String rowElement ) {
    this.rowElement = rowElement;
  }




  /**
   * @return the formatting for the field
   */
  public MessageFormat getFieldFormat() {
    return fieldFormat;
  }




  /**
   * @param format the format string of the field to set
   */
  public void setFieldFormat( MessageFormat format ) {
    this.fieldFormat = format;
  }




  /**
   * @return the rowAttributes
   */
  public String getRowAttributes() {
    return rowAttributes;
  }




  /**
   * @param rowAttributes the rowAttributes to set
   */
  public void setRowAttributes( String rowAttributes ) {
    this.rowAttributes = rowAttributes;
  }

}
