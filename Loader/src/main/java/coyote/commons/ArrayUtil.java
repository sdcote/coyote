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
  private static final Class BYTE_ARRAY = ( new byte[0] ).getClass();

  /** An empty immutable <code>Object</code> array. */
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

  /** An empty immutable <code>Class</code> array. */
  public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

  /** An empty immutable <code>String</code> array. */
  public static final String[] EMPTY_STRING_ARRAY = new String[0];

  /** An empty immutable <code>long</code> array. */
  public static final long[] EMPTY_LONG_ARRAY = new long[0];

  /** An empty immutable <code>Long</code> array. */
  public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];

  /** An empty immutable <code>int</code> array. */
  public static final int[] EMPTY_INT_ARRAY = new int[0];

  /** An empty immutable <code>Integer</code> array. */
  public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];

  /** An empty immutable <code>short</code> array. */
  public static final short[] EMPTY_SHORT_ARRAY = new short[0];

  /** An empty immutable <code>Short</code> array. */
  public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];

  /** An empty immutable <code>byte</code> array. */
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  /** An empty immutable <code>Byte</code> array. */
  public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];

  /** An empty immutable <code>double</code> array. */
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];

  /** An empty immutable <code>Double</code> array. */
  public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];

  /** An empty immutable <code>float</code> array. */
  public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

  /** An empty immutable <code>Float</code> array. */
  public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];

  /** An empty immutable <code>boolean</code> array. */
  public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

  /** An empty immutable <code>Boolean</code> array. */
  public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

  /** An empty immutable <code>char</code> array. */
  public static final char[] EMPTY_CHAR_ARRAY = new char[0];

  /** An empty immutable <code>Character</code> array. */
  public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];




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




  /**
   * Return a new array that is a copy of the array plus a new integer element.
   *
   * <p>The component type of the array must be the same as that type of the
   * element.</p>
   *
   * @param array An array
   * @param element The element to append.
   *
   * @return a new array that is a copy of the array plus a new integer element.
   */
  public static int[] addElement( int[] array, int element ) {
    int length = array.length;
    int[] newarray = new int[length + 1];
    System.arraycopy( array, 0, newarray, 0, length );

    newarray[length] = element;

    return newarray;
  }




  /**
   * Return a new array that is a copy of the array plus a new long element.
   *
   * @param array An array
   * @param element The element to append.
   *
   * @return a new array that is a copy of the array plus a new long element.
   */
  public static long[] addElement( long[] array, long element ) {
    long[] newarray = new long[array.length + 1];
    System.arraycopy( array, 0, newarray, 0, array.length );

    newarray[array.length] = element;

    return newarray;
  }




  /**
   * Perform an intersection of two long arrays.
   *
   * <p>This will return an array of longs values that are contained in both
   * arrays.</p>
   *
   * @param primary The primary array containing primitive long values.
   * @param secondary The primary secondary containing primitive long values.
   *
   * @return the intersecting values of the primary and secondary arrays.
   */
  public static long[] intersect( long[] primary, long[] secondary ) {
    long[] intersect = new long[0];

    for ( int x = 0; x < primary.length; x++ ) {
      for ( int y = 0; y < secondary.length; y++ ) {
        if ( primary[x] == secondary[y] ) {
          long[] newarray = new long[intersect.length + 1];
          System.arraycopy( intersect, 0, newarray, 0, intersect.length );

          newarray[intersect.length] = secondary[y];
          intersect = newarray;
        }
      }
    }

    return intersect;
  }




  /**
   * Return a new array that is the union of the elements in array1 and array2.
   *
   * <p>The component types of both arrays must be the same.</p>
   *
   * @param array1 The first array.
   * @param array2 The second array.
   *
   * @return a new array that is the union of the elements in array1 and array2
   */
  public static Object addElements( Object array1, Object array2 ) {
    int length1 = Array.getLength( array1 );
    int length2 = Array.getLength( array2 );
    Object newarray = Array.newInstance( array1.getClass().getComponentType(), length1 + length2 );
    System.arraycopy( array1, 0, newarray, 0, length1 );
    System.arraycopy( array2, 0, newarray, length1, length2 );

    return newarray;
  }




  /**
   * Return a new array that is a copy of the array plus a new element.
   *
   * <p>The component type of the array must be the same as that type of the
   * element.</p>
   *
   * @param array An array
   * @param element The element to append.
   *
   * @return a new array that is a copy of the array plus a new element.
   */
  public static Object insertElement( Object array, Object element ) {
    int length = Array.getLength( array );
    Object newarray = Array.newInstance( array.getClass().getComponentType(), length + 1 );
    System.arraycopy( array, 0, newarray, 1, length );
    Array.set( newarray, 0, element );

    return newarray;
  }




  /**
   * Remove the given object from the given array.
   *
   * @param array
   * @param element
   *
   * @return a new array with the element removed
   */
  public static Object removeElement( Object array, Object element ) {
    int length = Array.getLength( array );

    for ( int i = 0; i < length; i++ ) {
      if ( element.equals( Array.get( array, i ) ) ) {
        return removeElementAt( array, i );
      }
    }

    return array;
  }




  /**
   * Remove the element at the given position from the given array.
   *
   * @param oldarray
   * @param index
   *
   * @return a new array with the element removed
   */
  public static Object removeElementAt( Object oldarray, int index ) {
    int length = Array.getLength( oldarray );
    Object newarray = Array.newInstance( oldarray.getClass().getComponentType(), length - 1 );
    System.arraycopy( oldarray, 0, newarray, 0, index );
    System.arraycopy( oldarray, index + 1, newarray, index, length - index - 1 );

    return newarray;
  }




  /**
   * Find where a particular integer is within an array of integers.
   *
   * <p>Works like the String method of the same name.</p>
   *
   * @param value the value for which to search
   * @param array the array to search
   *
   * @return the index of the given value in the given array
   */
  public static int indexOf( int value, int[] array ) {
    for ( int i = 0; i < array.length; i++ ) {
      if ( array[i] == value ) {
        return i;
      }
    }

    return -1;
  }




  /**
   * Find where a particular object is within an array of objects.
   *
   * <p>Works like the String method of the same name.</p>
   *
   * @param value the value for which to search
   * @param array the array to search
   *
   * @return the index of the given value in the given array
   */
  public static int indexOf( Object value, Object[] array ) {
    for ( int i = 0; i < array.length; i++ ) {
      if ( equals( array[i], value ) ) {
        return i;
      }
    }

    return -1;
  }




  /**
   * Returns true if the value object is contained within the array.
   *
   * <p>NOTE that this is not a test for reference equality, but rather value
   * equality.</p>
   *
   * @param value
   * @param array
   *
   * @return true if the value object is contained within the array, false otherwise.
   */
  public static boolean contains( Object value, Object[] array ) {
    return ( indexOf( value, array ) != -1 );
  }




  /**
   * @param array1
   * @param array2
   *
   * @return true if both arrays are equivalent false otherwise
   */
  public static boolean equals( Object[] array1, Object[] array2 ) {
    if ( array1.length != array2.length ) {
      return false;
    }

    for ( int i = 0; i < array1.length; i++ ) {
      if ( !equals( array1[i], array2[i] ) ) {
        return false;
      }
    }

    return true;
  }




  /**
   * @param object1
   * @param object2
   *
   * @return true if both objects are equivalent, false otherwise.
   */
  public static boolean equals( Object object1, Object object2 ) {
    if ( ( object1 == null ) && ( object2 == null ) ) {
      return true;
    } else if ( ( object1 == null ) || ( object2 == null ) ) {
      return false;
    } else {
      return object1.equals( object2 );
    }
  }




  /**
   * compare two byte arrays
   *
   * @param array1
   * @param array2
   *
   * @return true if bot arrays are equivalent, false otherwise.
   */
  public static boolean equals( byte[] array1, byte[] array2 ) {
    if ( array1.length != array2.length ) {
      return false;
    }

    for ( int i = 0; i < array1.length; i++ ) {
      byte b1 = array1[i];
      byte b2 = array2[i];

      if ( b1 != b2 ) {
        return false;
      }
    }

    return true;
  }




  /**
   * Take an array of primitive values and return an array of Objects
   * representing those primitives.
   *
   * <p>For example take a byte[] and convert it to a Byte[] or an int[] and
   * return an Integer[].</p>
   *
   * <p>If the primitive array argument contains no elements, then the returned
   * array will contain no elements, ie. it will be an empty array and NOT
   * null.</p>
   *
   * @param obj the array of primitives to use to populate the return values
   *
   * @return An array of Object of the appropriate type or null if the argument 
   *         was not an array of primitives.
   */
  public static Object[] convertFromPrimitives( Object obj ) {
    if ( obj instanceof byte[] ) {
      Byte[] retval = new Byte[( (byte[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Byte( ( (byte[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof char[] ) {
      Character[] retval = new Character[( (char[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Character( ( (char[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof boolean[] ) {
      Boolean[] retval = new Boolean[( (boolean[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Boolean( ( (boolean[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof short[] ) {
      Short[] retval = new Short[( (short[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Short( ( (short[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof int[] ) {
      Integer[] retval = new Integer[( (int[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Integer( ( (int[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof long[] ) {
      Long[] retval = new Long[( (long[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Long( ( (long[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof float[] ) {
      Float[] retval = new Float[( (float[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Float( ( (float[])obj )[i] );
      }

      return retval;
    } else if ( obj instanceof double[] ) {
      Double[] retval = new Double[( (double[])obj ).length];

      for ( int i = 0; i < retval.length; i++ ) {
        retval[i] = new Double( ( (double[])obj )[i] );
      }

      return retval;
    }

    return null;
  }

}
