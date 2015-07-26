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

          fields.add( new FieldDefinition( field.getName(), fielddef.getAsInt( ConfigTag.START ), fielddef.getAsInt( ConfigTag.LENGTH ), fielddef.getAsString( ConfigTag.TYPE ), fielddef.getAsString( ConfigTag.FORMAT ), trim ) );

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

    printwriter.write( frame.toString() );
    printwriter.write( StringUtil.LINE_FEED );
    printwriter.flush();

    // Increment the row number
    rowNumber++;

  }

}
