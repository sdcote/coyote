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
package coyote.batch.eval;

import coyote.batch.OperationalContext;
import coyote.commons.eval.VariableSet;


/**
 * Acts as a facade to the contexts providing access to the underlying context 
 * data and converting values to Double values when possible.
 */
public class DoubleVariableSet<T> implements VariableSet<T> {

  /** The operatinal context backing this class */
  OperationalContext context = null;




  /**
   * @return the context backing this object
   */
  public OperationalContext getContext() {
    return context;
  }




  /**
   * @param context the context to us as a source of data
   */
  public void setContext( OperationalContext context ) {
    this.context = context;
  }




  /**
   * @see coyote.commons.eval.VariableSet#get(java.lang.String)
   */
  @Override
  public T get( String variableName ) {
    // TODO Auto-generated method stub
    return null;
  }




  /**
   * Sets a variable value.
   * 
   * @param variableName The variable name
   * @param value The variable value (null to remove a variable from the set).
   */
  public void set( final String variableName, final T value ) {
    // this.varToValue.put( variableName, value );
  }
}
