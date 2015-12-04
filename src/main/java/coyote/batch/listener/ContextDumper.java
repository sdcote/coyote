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
package coyote.batch.listener;

import coyote.batch.OperationalContext;
import coyote.batch.TransformContext;
import coyote.commons.StringUtil;


/**
 * This records the data in the context at the beginning and the end of a 
 * transformation.
 * 
 * <p>This is primarily a development and debugging tool to assist in the use 
 * of template strings.</p> 
 */
public class ContextDumper extends FileRecorder {

  /**
   * @see coyote.batch.listener.AbstractListener#onStart(coyote.batch.OperationalContext)
   */
  @Override
  public void onStart( OperationalContext context ) {
    if ( context instanceof TransformContext ) {
      StringBuffer b = new StringBuffer( "Context at beginning of Transformation:" );
      b.append( StringUtil.LINE_FEED );
      b.append( context.dump() );
      b.append( StringUtil.LINE_FEED );
      b.append( "Symbol Table:" );
      b.append( StringUtil.LINE_FEED );
      b.append( context.getSymbols().dump() );
      b.append( StringUtil.LINE_FEED );
      b.append( "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
    }
  }




  /**
   * @see coyote.batch.listener.AbstractListener#onEnd(coyote.batch.OperationalContext)
   */
  @Override
  public void onEnd( OperationalContext context ) {
    if ( context instanceof TransformContext ) {
      StringBuffer b = new StringBuffer( "Context at end of Transformation:" );
      b.append( StringUtil.LINE_FEED );
      b.append( context.dump() );
      b.append( StringUtil.LINE_FEED );
      b.append( "Symbol Table:" );
      b.append( StringUtil.LINE_FEED );
      b.append( context.getSymbols().dump() );
      b.append( StringUtil.LINE_FEED );
      b.append( "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" );
      b.append( StringUtil.LINE_FEED );
      write( b.toString() );
    }
  }

}
