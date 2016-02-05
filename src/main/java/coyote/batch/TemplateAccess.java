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

import coyote.commons.StringUtil;


/**
 * This provides access to data in the context
 * 
 * <p>This class is designed to provide templates with access to any data in 
 * the transform context and the current transaction context it contains.</p> 
 */
public class TemplateAccess {

  TransformContext context = null;




  public TemplateAccess() {

  }




  /**
   * @param context
   */
  public TemplateAccess( TransformContext context ) {
    if ( context == null )
      throw new IllegalArgumentException( "Cannot create TemplateAccess without a context" );

    this.context = context;
  }




  /**
   * Get the value of a working frame field.
   * 
   * @param fieldname the name of the working frame to retrieve
   * 
   * @return the string value of that field, or null if it is not found
   */
  public String working( String fieldname ) {
    String retval = null;
    if ( StringUtil.isNotBlank( fieldname ) && context.getTransaction() != null && context.getTransaction().getWorkingFrame() != null ) {
      retval = context.getTransaction().getWorkingFrame().getAsString( fieldname );
    }
    return retval;
  }




  /**
   * Get the value of a source frame field.
   * 
   * @param fieldname the name of the source frame to retrieve
   * 
   * @return the string value of that field, or null if it is not found
   */
  public String source( String fieldname ) {
    String retval = null;
    if ( StringUtil.isNotBlank( fieldname ) && context.getTransaction() != null && context.getTransaction().getSourceFrame() != null ) {
      retval = context.getTransaction().getSourceFrame().getAsString( fieldname );
    }
    return retval;
  }

}
