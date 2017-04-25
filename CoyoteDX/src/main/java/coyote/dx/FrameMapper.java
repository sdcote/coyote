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
package coyote.dx;

import coyote.dx.mapper.MappingException;


/**
 * This component is responsible for building the target frame out of the 
 * working frame in a particular transaction.
 * 
 * <p>Mappers allow the marshaling of data with different naming. This is 
 * useful if a field is named one thing in the source system but needs to be 
 * name something else in the target system.</p>
 */
public interface FrameMapper extends ConfigurableComponent {

  /**
   * Take field values from the working frame in the transaction context and 
   * place them in appropriately named fields of the target frame. 
   * 
   * @param context the transaction context containing the working and target 
   *        frames
   *        
   * @throws MappingException if the fields in the working frames do not exist.
   */
  void process( TransactionContext context ) throws MappingException;

}
