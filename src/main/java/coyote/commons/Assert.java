package coyote.commons;

import java.util.Collection;
import java.util.Map;


/**
 * Class with static assertions for data validations.
 */
public final class Assert {

  /**
   * Assert that the given text does not contain the given substring.
   * 
   * <pre class="code">Assert.doesNotContain(name, "poo");</pre>
   * 
   * @param textToSearch the text to search
   * @param substring the substring to find within the text
   */
  public static void doesNotContain( final String textToSearch, final String substring ) {
    doesNotContain( textToSearch, substring, "[Assertion Failure] - this String argument must not contain the substring [" + substring + "]" );
  }




  /**
   * Assert that the given text does not contain the given substring.
   * 
   * <pre class="code">Assert.doesNotContain(name, "poo", "Argument must not contain 'poo'");</pre>
   * 
   * @param textToSearch the text to search
   * @param substring the substring to find within the text
   * @param msg the exception message to use if the assertion fails
   */
  public static void doesNotContain( final String textToSearch, final String substring, final String msg ) {
    if ( StringUtil.hasLength( textToSearch ) && StringUtil.hasLength( substring ) && ( textToSearch.indexOf( substring ) != -1 ) ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that the given String is not empty; it must not be {@code null} and 
   * not an empty String.
   * 
   * <pre class="code">Assert.hasLength(name);</pre>
   * 
   * @param text the String to check
   */
  public static void hasLength( final String text ) {
    hasLength( text, "[Assertion Failure] - this String argument must have length; it must not be null or empty" );
  }




  /**
   * Assert that the given String is not empty; it must not be {@code null} and 
   * not an empty String.
   * 
   * <pre class="code">Assert.hasLength(name, "Name must not be empty");</pre>
   * 
   * @param text the String to check
   * @param msg the exception message to use if the assertion fails
   */
  public static void hasLength( final String text, final String msg ) {
    if ( !StringUtil.hasLength( text ) ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that the given String has valid text content; it must not be 
   * {@code null} and must contain at least one non-whitespace character.
   * 
   * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
   * 
   * @param text the String to check
   */
  public static void hasText( final String text ) {
    hasText( text, "[Assertion Failure] - this String argument must have text; it must not be null, empty, or blank" );
  }




  /**
   * Assert that the given String has valid text content; it must not be 
   * {@code null} and must contain at least one non-whitespace character.
   * 
   * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
   * 
   * @param text the String to check
   * @param msg the exception message to use if the assertion fails
   */
  public static void hasText( final String text, final String msg ) {
    if ( !StringUtil.hasText( text ) ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
   * 
   * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
   * 
   * @param superType the super type to check
   * @param subType the sub type to check
   * 
   * @throws IllegalArgumentException if the classes are not assignable
   */
  public static void isAssignable( final Class superType, final Class subType ) {
    isAssignable( superType, subType, "" );
  }




  /**
   * Assert that {@code superType.isAssignableFrom(subType)} is {@code true}.
   * 
   * <pre class="code">Assert.isAssignable(Number.class, myClass);</pre>
   * 
   * @param superType the super type to check against
   * @param subType the sub type to check
   * @param msg a message which will be prepended to the message produced by 
   * the function itself, and which may be used to provide context. It should 
   * normally end in a ": " or ". " so that the function generate message looks 
   * fine when prepended to it.
   * 
   * @throws IllegalArgumentException if the classes are not assignable
   */
  public static void isAssignable( final Class superType, final Class subType, final String msg ) {
    notNull( superType, "Type to check against must not be null" );
    if ( ( subType == null ) || !superType.isAssignableFrom( subType ) ) {
      throw new IllegalArgumentException( msg + subType + " is not assignable to " + superType );
    }
  }




  /**
   * Assert that the provided object is an instance of the provided class.
   * 
   * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
   * 
   * @param clazz the required class
   * @param obj the object to check
   * 
   * @throws IllegalArgumentException if the object is not an instance of clazz
   */
  public static void isInstanceOf( final Class clazz, final Object obj ) {
    isInstanceOf( clazz, obj, "[Assertion Failure] - Argument must be an instance of " + clazz.getCanonicalName() );
  }




  /**
   * Assert that the provided object is an instance of the provided class.
   * 
   * <pre class="code">Assert.instanceOf(Foo.class, foo);</pre>
   * 
   * @param type the type to check against
   * @param obj the object to check
   * @param msg a message which will be prepended to the message produced by 
   * the function itself, and which may be used to provide context. It should 
   * normally end in a ": " or ". " so that the function generate message looks 
   * fine when prepended to it.
   * 
   * @throws IllegalArgumentException if the object is not an instance of clazz
   */
  public static void isInstanceOf( final Class type, final Object obj, final String msg ) {
    notNull( type, "Type to check against must not be null" );
    if ( !type.isInstance( obj ) ) {
      throw new IllegalArgumentException( msg + "Object of class [" + ( obj != null ? obj.getClass().getName() : "null" ) + "] must be an instance of " + type );
    }
  }




  /**
   * Assert that an object is {@code null}.
   * 
   * <pre class="code">Assert.isNull(value);</pre>
   * 
   * @param object the object to check
   * 
   * @throws IllegalArgumentException if the object is not {@code null}
   */
  public static void isNull( final Object object ) {
    isNull( object, "[Assertion Failure] - the object argument must be null" );
  }




  /**
   * Assert that an object is {@code null} .
   * 
   * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
   * 
   * @param object the object to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the object is not {@code null}
   */
  public static void isNull( final Object object, final String msg ) {
    if ( object != null ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert a number is positive or zero.
   * 
   * <pre class="code">Assert.isPositive(i);</pre>
   * 
   * @param number a number to check
   * 
   * @throws IllegalArgumentException if number is < 0
   */
  public static void isPositive( final int number ) {
    isPositive( number, "[Assertion Failure] - this expression must be true" );
  }




  /**
   * Assert a number is positive or zero.
   * 
   * <pre class="code">Assert.isPositive(i, "The value must be greater than or equal to zero");</pre>
   * 
   * @param number a number to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if number is < 0
   */
  public static void isPositive( final int number, final String msg ) {
    if ( number <= 0 ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert a boolean expression, throwing {@code IllegalArgumentException}
   * if the test result is {@code false}.
   * 
   * <pre class="code">Assert.isTrue(i &gt; 0);</pre>
   * 
   * @param expression a boolean expression
   * 
   * @throws IllegalArgumentException if expression is {@code false}
   */
  public static void isTrue( final boolean expression ) {
    isTrue( expression, "[Assertion Failure] - this expression must be true" );
  }




  /**
   * Assert a boolean expression, throwing {@code IllegalArgumentException}
   * if the test result is {@code false}.
   * 
   * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
   * 
   * @param expression a boolean expression
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if expression is {@code false}
   */
  public static void isTrue( final boolean expression, final String msg ) {
    if ( !expression ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that a string has a specific maximum length; it must not be 
   * {@code null} and must have at most the given number of characters.
   * 
   * <pre class="code">Assert.maxLength(16, text);</pre>
   *
   * @param limit The maximum number of characters the string is allow to have.
   * @param text The string to check
   * 
   * @throws IllegalArgumentException if the text is {@code null} or has a length which exceeds the given limit
   */
  public static void maxLength( final int limit, final String text ) {
    maxLength( limit, text, "[Assertion Failure] - this String argument must not exceed " + limit + " characters in length" );
  }




  /**
   * Assert that a string has a specific maximum length; it must not be 
   * {@code null} and must have at most the given number of characters.
   * 
   * <pre class="code">Assert.maxLength(16, text, "The text cannot be larger than 16 characters");</pre>
   * 
   * @param limit The maximum number of characters the string is allow to have.
   * @param text The string to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the text is {@code null} or has a length which exceeds the given limit
   */
  public static void maxLength( final int limit, final String text, final String msg ) {
    if ( ( text != null ) && ( text.length() > limit ) ) {
      throw new IllegalStateException( msg );
    }
  }




  /**
   * Assert that a string has at least the specified number of characters; it 
   * must not be {@code null} and must have at least the number of characters.
   * 
   * <pre class="code">Assert.minLength(8, text, "The text must contain at least 8 characters");</pre>
   * 
   * @param limit The minimum number of characters the string must have.
   * @param text The string to check
   * 
   * @throws IllegalArgumentException if the text is {@code null} or has a length less than the given limit
   */
  public static void minLength( final int limit, final String text ) {
    maxLength( limit, text, "[Assertion Failure] - this String argument must be at least " + limit + " characters in length" );
  }




  /**
   * Assert that a string has at least the specified number of characters; it 
   * must not be {@code null} and must have at least the number of characters.
   * 
   * <pre class="code">Assert.minLength(8, text, "The text must contain at least 8 characters");</pre>
   * 
   * @param limit The minimum number of characters the string must have.
   * @param text The string to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the text is {@code null} or has a length less than the given limit
   */
  public static void minLength( final int limit, final String text, final String msg ) {
    if ( !StringUtil.hasLength( text ) || ( text.length() < limit ) ) {
      throw new IllegalStateException( msg );
    }
  }




  /**
   * Assert that an array has no null elements.
   * 
   * <p><strong>Note:</strong> Does not complain if the array is empty.</p>
   * 
   * <pre class="code">Assert.noNullElements(array);</pre>
   * 
   * @param array the array to check
   * 
   * @throws IllegalArgumentException if the object array contains a {@code null} element
   */
  public static void noNullElements( final Object[] array ) {
    noNullElements( array, "[Assertion Failure] - this array must not contain any null elements" );
  }




  /**
   * Assert that an array has no null elements.
   * 
   * <p><strong>Note:</strong> Does not complain if the array is empty.</p>
   * 
   * <pre class="code">Assert.noNullElements(array, "The array must have non-null elements");</pre>
   * 
   * @param array the array to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the object array contains a {@code null} element
   */
  public static void noNullElements( final Object[] array, final String msg ) {
    if ( array != null ) {
      for ( final Object element : array ) {
        if ( element == null ) {
          throw new IllegalArgumentException( msg );
        }
      }
    }
  }




  /**
   * Assert that a collection has elements; it must not be {@code null} and 
   * must have at least one element.
   * 
   * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
   * 
   * @param collection the collection to check
   * 
   * @throws IllegalArgumentException if the collection is {@code null} or has no elements
   */
  public static void notEmpty( final Collection collection ) {
    notEmpty( collection, "[Assertion Failure] - this collection must not be empty: it must contain at least 1 element" );
  }




  /**
   * Assert that a collection has elements; it must not be {@code null} and 
   * must have at least one element.
   * 
   * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
   * 
   * @param collection the collection to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the collection is {@code null} or has no elements
   */
  public static void notEmpty( final Collection collection, final String msg ) {
    if ( ( collection == null ) || collection.isEmpty() ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that a Map has entries; it must not be {@code null} and must have 
   * at least one entry.
   * 
   * <pre class="code">Assert.notEmpty(map);</pre>
   * 
   * @param map the map to check
   * 
   * @throws IllegalArgumentException if the map is {@code null} or has no entries
   */
  public static void notEmpty( final Map map ) {
    notEmpty( map, "[Assertion Failure] - this map must not be empty; it must contain at least one entry" );
  }




  /**
   * Assert that a Map has entries; it must not be {@code null} and must have 
   * at least one entry.
   * 
   * <pre class="code">Assert.notEmpty(map, "Map must have entries");</pre>
   * 
   * @param map the map to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the map is {@code null} or has no 
   * entries
   */
  public static void notEmpty( final Map map, final String msg ) {
    if ( ( map == null ) || map.isEmpty() ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that an array has elements; it must not be {@code null} and must 
   * have at least one element.
   * 
   * <pre class="code">Assert.notEmpty(array);</pre>
   * 
   * @param array the array to check
   * 
   * @throws IllegalArgumentException if the object array is {@code null} or 
   * has no elements
   */
  public static void notEmpty( final Object[] array ) {
    notEmpty( array, "[Assertion Failure] - this array must not be empty: it must contain at least 1 element" );
  }




  /**
   * Assert that an array has elements; it must not be {@code null} and must 
   * have at least one element.
   * 
   * <pre class="code">Assert.notEmpty(array, "The array must have elements");</pre>
   * 
   * @param array the array to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the object array is {@code null} or has no elements
   */
  public static void notEmpty( final Object[] array, final String msg ) {
    if ( ( array == null ) || ( array.length == 0 ) ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert that an object is not {@code null}.
   * 
   * <pre class="code">Assert.notNull(clazz);</pre>
   * 
   * @param object the object to check
   * 
   * @throws IllegalArgumentException if the object is {@code null}
   */
  public static void notNull( final Object object ) {
    notNull( object, "[Assertion Failure] - this argument is required; it must not be null" );
  }




  /**
   * Assert that an object is not {@code null}.
   * 
   * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
   * 
   * @param object the object to check
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalArgumentException if the object is {@code null}
   */
  public static void notNull( final Object object, final String msg ) {
    if ( object == null ) {
      throw new IllegalArgumentException( msg );
    }
  }




  /**
   * Assert a boolean expression, throwing {@link IllegalStateException} if the 
   * test result is {@code false}.
   * 
   * <p>Call {@link #isTrue(boolean)} if you wish to throw 
   * {@link IllegalArgumentException} on an assertion failure.</p>
   * 
   * <pre class="code">Assert.state(id == null);</pre>
   * 
   * @param expression a boolean expression
   * 
   * @throws IllegalStateException if the supplied expression is {@code false}
   */
  public static void state( final boolean expression ) {
    state( expression, "[Assertion Failure] - this state invariant must be true" );
  }




  /**
   * Assert a boolean expression, throwing {@code IllegalStateException} if the 
   * test result is {@code false}.
   *  
   * <p>Call {@link #isTrue(boolean)} if you wish to throw 
   * IllegalArgumentException on an assertion failure.</p>
   * 
   * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
   * 
   * @param expression a boolean expression
   * @param msg the exception message to use if the assertion fails
   * 
   * @throws IllegalStateException if expression is {@code false}
   */
  public static void state( final boolean expression, final String msg ) {
    if ( !expression ) {
      throw new IllegalStateException( msg );
    }
  }




  /**
   * Asserts a string is not null, empty ("") and not only whitespace.
   * 
   * @param str the String to check
   * 
   * @throws IllegalArgumentException if the argument is null, empty or 
   * composed entirely of whitespace
   */
  public static void isNotBlank( String str ) {
    if ( isBlankString( str ) ) {
      throw new IllegalArgumentException( "[Assertion Failure] - this value must not be blank" );
    }
  }




  /**
   * Asserts a string is null, empty ("") or composed entirely of whitespace.
   * 
   * @param str the String to check
   * 
   * @throws IllegalArgumentException if the argument is not null, empty or 
   * composed entirely of whitespace
   */
  public static void isBlank( String str ) {
    if ( !isBlankString( str ) ) {
      throw new IllegalArgumentException( "[Assertion Failure] - this value must not contain a value" );
    }
  }




  /**
   * Checks if a string is not null, empty ("") and not only whitespace.
   * 
   * @param str the String to check, may be null
   * 
   * @return <code>true</code> if the String is not empty and not null and not
   *         whitespace
   */
  private static boolean isBlankString( String str ) {
    int strLen;
    if ( str == null || ( strLen = str.length() ) == 0 ) {
      return true;
    }
    for ( int i = 0; i < strLen; i++ ) {
      if ( ( Character.isWhitespace( str.charAt( i ) ) == false ) ) {
        return false;
      }
    }
    return true;
  }




  /**
   * Throws an IllegalArgumentException with the given message if null or
   * blank.
   * 
   * @param arg the string to test
   * @param message the message to send back if the string is null or empty
   */
  public static final void notBlank( final String arg, final String message ) {
    if ( arg == null ) {
      throw new IllegalArgumentException( "Null argument not allowed: " + message );
    }

    if ( arg.trim().equals( "" ) ) {
      throw new IllegalArgumentException( "Blank argument not allowed: " + message );
    }
  }




  /**
   * Private Constructor so no instances of this class
   */
  private Assert() {}

}
