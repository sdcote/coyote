/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * A reader which retrieves its DataFrames from its transform context.
 * 
 * <p>The purpose of this reader is to enable the creation of an engine in 
 * memory to which data can be passed programmatically. This allows an engine 
 * to create other engines which can process its data.
 * 
 * <p>This also makes testing other components easier. 
 */
public class ContextReader extends AbstractFrameReader {
  private int counter = 0;
  private int limit = 1;
  private DataFrame frame = null;




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    counter++;
    if(counter>=limit){
      context.setLastFrame( true );
    }
    return frame;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return counter >= limit;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    counter = 0;
    try {
      limit = configuration.getInt( ConfigTag.LIMIT );
    } catch ( NumberFormatException e ) {
      limit = 1;
    }
    
    final Config section = configuration.getSection( ConfigTag.FIELDS );
    if ( section != null ) {
      frame = new DataFrame();
      for ( final DataField field : section.getFields() ) {
        if ( !field.isFrame() ) {
          if ( StringUtil.isNotBlank( field.getName() ) && !field.isNull() ) {
            frame.set( field.getName(), field.getObjectValue() );
          }
        }
      }
    } else {
      String msg = LogMsg.createMsg( CDX.MSG, "Reader.no_fields_specified", getClass().getName() ).toString();
      Log.error( msg );
      context.setError( msg );
    }
  }

}
