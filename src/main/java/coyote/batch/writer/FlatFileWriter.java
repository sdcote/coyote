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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FieldDefinition;
import coyote.batch.FrameWriter;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class FlatFileWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  /** The list of fields we are to write in the order they are to be written */
  List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private char padChar = ' ';
  private int recordLength = 0;




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // Now setup our field definitions
    DataFrame fieldcfg = getFrame( ConfigTag.FIELDS );
    if ( fieldcfg != null ) {
      boolean trim = false;// flag to trim values

      // position of the last record
      int last = 0;

      for ( DataField field : fieldcfg.getFields() ) {
        try {
          DataFrame fielddef = (DataFrame)field.getObjectValue();

          // determine if values should be trimmed for this field
          try {
            trim = fielddef.getAsBoolean( ConfigTag.TRIM );
          } catch ( Exception e ) {
            trim = false;
          }

          int alignment = 0; // left alignment
          String align = fielddef.getAsString( ConfigTag.ALIGN );
          if ( StringUtil.isNotBlank( align ) ) {
            if ( align.startsWith( "L" ) || align.startsWith( "l" ) ) {
              alignment = 0;
            } else if ( align.startsWith( "R" ) || align.startsWith( "r" ) ) {
              alignment = 2;
            } else if ( align.startsWith( "C" ) || align.startsWith( "c" ) ) {
              alignment = 1;
            } else {
              log.warn( "Unrecognized {} configuration value of '{}' - defaulting to 'left' alignment", ConfigTag.ALIGN, align );
            }
          }

          fields.add( new FieldDefinition( field.getName(), fielddef.getAsInt( ConfigTag.START ), fielddef.getAsInt( ConfigTag.LENGTH ), fielddef.getAsString( ConfigTag.TYPE ), fielddef.getAsString( ConfigTag.FORMAT ), trim, alignment ) );

          // see how long the record is to be by keeping track of the longest 
          // start position and then adding the field length
          if ( fielddef.getAsInt( ConfigTag.START ) > last ) {
            last = fielddef.getAsInt( ConfigTag.START );
            recordLength = last + fielddef.getAsInt( ConfigTag.LENGTH );
          }
        } catch ( Exception e ) {
          context.setError( "Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage() );
          return;
        }
      }

      log.debug( "There are {} field definitions, record length is {} characters.", fields.size(), recordLength );
    } else {
      context.setError( "There are no fields configured in the writer" );
      return;
    }

  }




  /**
   * @see coyote.batch.writer.AbstractFrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( final DataFrame frame ) {

    StringBuilder line = new StringBuilder( recordLength );
    for ( int i = 0; i < recordLength; i++ ) {
      line.append( padChar );
    }

    for ( FieldDefinition def : fields ) {

      // get the field from the context with the name in the definition

      // format it according to the format in the definition

      String field = StringUtil.fixedLength( "This is the value of the target frame field", def.getLength(), def.getAlignment(), padChar );

      // now insert
      line.insert( def.getStart(), field );
    }

    printwriter.write( frame.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
