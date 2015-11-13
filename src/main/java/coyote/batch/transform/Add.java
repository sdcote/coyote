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
package coyote.batch.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.FrameTransform;
import coyote.batch.TransformContext;
import coyote.batch.TransformException;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;


/**
 * This adds a field to the working frame.
 * 
 * <p>The configuration is 
 * <tt>"Add" : { "Name" : "cust_item_usage", "Value" : "0001.00" }</tt>
 */
public class Add extends AbstractFrameTransform implements FrameTransform {
  private static final Logger LOG = LoggerFactory.getLogger( Add.class );

  // The name of the field we are to transform
  private String fieldName = null;
  private String fieldValue = null;




  /**
   * @see coyote.batch.transform.AbstractFrameTransform#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    // get the name of the field to add
    String name = findString( ConfigTag.NAME );

    if ( StringUtil.isBlank( name ) ) {
      context.setError( "Add transform must contain a field name" );
    } else {
      fieldName = name.trim();
    }

    String value = findString( ConfigTag.VALUE );
    if ( value == null ) {
      LOG.warn( "Add transform will add a null {} field to the working frames.", fieldName );
    } else {
      fieldValue = value;
    }

  }




  /**
   * @see coyote.batch.FrameTransform#process(coyote.dataframe.DataFrame)
   */
  @Override
  public DataFrame process( DataFrame frame ) throws TransformException {
    frame.add( fieldName, super.resolveArgument( fieldValue ) );
    return frame;
  }

}
