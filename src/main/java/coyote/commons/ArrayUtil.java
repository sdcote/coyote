/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons;

import java.lang.reflect.Array;


/**
 * Various sundry array utilities
 */
public final class ArrayUtil {

  /**
   * Private constructor because everything is static
   */
  private ArrayUtil() {}




  /**
   * Return a new array that is a copy of the array plus a new element.
   *
   * <p>The component type of the array must be the same as that type of the
   * element.</p>
   *
   * @param array An array
   * @param element The element to append.
   *
   * @return the array with the new element
   */
  public static Object addElement( Object array, Object element ) {
    int length = Array.getLength( array );
    Object newarray = Array.newInstance( array.getClass().getComponentType(), length + 1 );
    System.arraycopy( array, 0, newarray, 0, length );
    Array.set( newarray, length, element );

    return newarray;
  }

}
