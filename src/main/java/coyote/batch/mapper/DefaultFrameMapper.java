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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurationException;
import coyote.batch.FrameMapper;
import coyote.batch.TransactionContext;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;


/**
 * 
 */
public class DefaultFrameMapper extends AbstractFrameMapper implements FrameMapper {

  /** The logger for the class */
  final Logger log = LoggerFactory.getLogger( getClass() );




  /**
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // Retrieve the Fields section from the mapper configuration
    DataFrame mapFrame = null;
    try {
      mapFrame = frame.getAsFrame( ConfigTag.FIELDS );
    } catch ( DataFrameException e ) {
      mapFrame = null;
    }

    if ( mapFrame != null ) {
      for ( DataField field : mapFrame.getFields() ) {
        if ( StringUtil.isNotBlank( field.getName() ) && field.getValue().length > 0 ) {
          fieldMap.put( field.getName(), field.getObjectValue().toString() );
        }
      }
    } else {
      log.warn( "No {} mapping section in Mapper configuration", ConfigTag.FIELDS );
    }

  }




  /**
   * @see coyote.batch.FrameMapper#process(coyote.batch.TransactionContext)
   */
  @Override
  public void process( TransactionContext context ) throws MappingException {

    if ( fieldMap.size() > 0 ) {
      for ( String workingName : fieldMap.keySet() ) {

        if ( context.getWorkingFrame().contains( workingName ) ) {
          DataField targetField = (DataField)context.getWorkingFrame().getField( workingName ).clone();
          targetField.setName( fieldMap.get( workingName ) );
          context.getTargetFrame().getFields().add( targetField );
        } else {
          log.warn( "The field '{}' is not in the working frame => {}", workingName, context.getWorkingFrame().toString() );
        }
      }
    } else {
      // if no field map, just perform a straight clone of the working frame
      context.setTargetFrame( (DataFrame)context.getWorkingFrame().clone() );
    }

  }

}
