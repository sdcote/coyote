/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

/**
 * This provides access to data in the context
 */
public class TemplateAccess {

  TransformContext context = null;




  public TemplateAccess() {

  }




  /**
   * @param context
   */
  public TemplateAccess( TransformContext context ) {
    this.context = context;
  }




  public String working( String fieldname ) {
    String retval = context.getTransaction().getWorkingFrame().getAsString( fieldname );
    
    return retval;
  }




  public String source( String fieldname ) {
    String retval = context.getTransaction().getSourceFrame().getAsString( fieldname );

    return retval;
  }

}
