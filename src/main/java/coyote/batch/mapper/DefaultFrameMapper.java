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
package coyote.batch.mapper;

import coyote.batch.Batch;
import coyote.batch.ConfigTag;
import coyote.batch.FrameMapper;
import coyote.batch.TransactionContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is the default mapper which simply copies fields from the source frame 
 * with one name to the target frame with another.
 */
public class DefaultFrameMapper extends AbstractFrameMapper implements FrameMapper {

  /**
   * Expects a configuration in the form of "Fields" : { "SourceField" : "TargetField" }
   *  
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // Retrieve the "fields" section from the configuration frame
    DataFrame mapFrame = null;
    try {
      mapFrame = frame.getAsFrame( ConfigTag.FIELDS );
    } catch ( DataFrameException e ) {
      mapFrame = null;
    }

    // If we found the "fields" frame...
    if ( mapFrame != null ) {
      // For each name:value pair, setup a soure:target mapping
      for ( DataField field : mapFrame.getFields() ) {
        if ( StringUtil.isNotBlank( field.getName() ) && field.getValue().length > 0 ) {
          fields.add( new SourceToTarget( field.getName(), field.getObjectValue().toString() ) );
        }
      }
    } else {
      Log.warn( LogMsg.createMsg( Batch.MSG, "Mapper.No %s section in Mapper configuration", ConfigTag.FIELDS ) );
    }

  }




  /**
   * @see coyote.batch.FrameMapper#process(coyote.batch.TransactionContext)
   */
  @Override
  public void process( TransactionContext context ) throws MappingException {

    if ( fields.size() > 0 ) {
      // for each frame in the list (insertion order)
      for ( SourceToTarget mapping : fields ) {

        DataField targetField;

        if ( context.getWorkingFrame().contains( mapping.getSourceName() ) ) {

          // clone the named field from the working frame
          targetField = (DataField)context.getWorkingFrame().getField( mapping.getSourceName() ).clone();

          // re-name the field to that of the target frame
          targetField.setName( mapping.getTargetName() );

        } else {
          // apparently there is no working field named with the source name. 
          // This is normal, the value could just be missing for this record 
          // only. Create a new null frame with the desired name
          targetField = new DataField( mapping.getTargetName(), null );
        }

        // place the mapped field in the target data frame for writing 
        context.getTargetFrame().getFields().add( targetField );
      }
    } else {
      // if no field map, just perform a straight clone of the working frame
      context.setTargetFrame( (DataFrame)context.getWorkingFrame().clone() );
    }

  }

}
