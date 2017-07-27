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

import java.util.List;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;


/**
 * A reader which retrieves its DataFrames from its transform context.
 * 
 * <p>The purpose of this reader is to enable the creation of an engine in 
 * memory to which data can be passed programmatically. This allows an engine 
 * to create other engines which can process its data. This also makes testing 
 * other components easier.
 * 
 * <p>A single DataFrame or an array of DataFrames should be placed in the 
 * engines TransformContext with a key (field name) of "ContextInput". This 
 * can be overridden in the reader's configuration by specifying the name of 
 * the field in the "source" configuration parameter. Note that the name of 
 * the field is context sensitive while the "source" attribute is not. A List 
 * of DataFrames is supported, but that will cause som inefficiencies as each 
 * DataFrame will be copied to a private array resulting in an extral copy 
 * operation and a duplication of DataFrames in memory. 
 */
public class ContextReader extends AbstractFrameReader {
  private int counter = 0;
  public static final String DEFAULT_CONTEXT_FIELD = "ContextInput";
  private String contextFieldName = DEFAULT_CONTEXT_FIELD;
  private DataFrame[] frames = null;




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    DataFrame retval = null;
    if ( counter < frames.length ) {
      retval = frames[counter];
    }
    counter++;
    if ( counter == frames.length ) {
      context.setLastFrame( true );
    }
    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return counter >= frames.length;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    super.open( context );

    String fieldName = configuration.getString( "source" );
    if ( StringUtil.isNotBlank( fieldName ) ) {
      contextFieldName = fieldName.trim();
    }

    Object dataobj = getContext().get( contextFieldName );
    if ( dataobj instanceof DataFrame ) {
      frames = new DataFrame[1];
      frames[0] = (DataFrame)dataobj;
    } else if ( dataobj instanceof DataFrame[] ) {
      frames = (DataFrame[])dataobj;
    } else if ( dataobj instanceof List ) {
      List list = (List)dataobj;
      frames = new DataFrame[list.size()];
      for ( int x = 0; x < list.size(); x++ ) {
        Object frm = list.get( x );
        if ( frm instanceof DataFrame ) {
          frames[x] = (DataFrame)frm;
        } else {
          Log.warn( "Context Reader found " + frm.getClass().getName() + " in element " + x + " of data list - Skipping. Expect a null frame to be read." );
        }
      }

    }

  }

}
