/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.testdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 */
public abstract class AbstractRow implements Row {

  protected final Map<String, Object> columns = new HashMap<String, Object>();
  protected static final String EMPTY_STRING = "";




  @Override
  public abstract Object clone();




  /**
   * @see coyote.testdata.Row#containsColumn(java.lang.String)
   */
  @Override
  public boolean containsColumn( final String name ) {
    return columns.containsKey( name );
  }




  /**
   * @see coyote.testdata.Row#get(java.lang.String)
   */
  @Override
  public Object get( final String name ) {
    return columns.get( name );
  }




  /**
   * @see coyote.testdata.Row#getColumnNames()
   */
  @Override
  public List<String> getColumnNames() {
    final List<String> retval = new ArrayList<String>();
    for ( final String name : columns.keySet() ) {
      retval.add( name );
    }
    return retval;
  }




  /**
   * @see coyote.testdata.Row#getStringValue(java.lang.String)
   */
  @Override
  public String getStringValue( final String name ) {
    final Object value = get( name );
    if ( value != null ) {
      return value.toString();
    } else if ( containsColumn( name ) ) {
      return EMPTY_STRING;
    } else {
      return null;
    }

  }




  /**
   * @see coyote.testdata.Row#set(java.lang.String, java.lang.Object)
   */
  @Override
  public void set( final String name, final Object value ) {
    columns.put( name, value );
  }

}
