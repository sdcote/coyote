/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.testdata.credential;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import coyote.testdata.GenerationStrategy;
import coyote.testdata.Row;


/**
 * 
 */
public class PasswordStrategy implements GenerationStrategy {

  private static final String LCaseChars = "abcdefgijkmnopqrstwxyz";
  private static final String UCaseChars = "ABCDEFGHJKLMNPQRSTWXYZ";
  private static final String NumericChars = "23456789"; // 1 and 0 are too close to l and O
  private static final String SpecialChars = "*$-+?_&=!%{}/";
  private static SecureRandom random;

  // Default generation criteria
  private int _min = 6;
  private int _max = 16;
  private int _lower = 2;
  private int _upper = 2;
  private int _digits = 2;
  private int _specials = 1;




  /**
   * Generate a secure password matching size and content criteria.
   * 
   * @param minLength Minimum length of the password
   * @param maxLength Maximum length of the password
   * @param minLCaseCount minimum number of lower case characters (negative indicates not to include lower case characters)
   * @param minUCaseCount minimum number of upper case characters (negative indicates not to include upper case characters)
   * @param minNumCount minimum number of digits (negative indicates not to include digits)
   * @param minSpecialCount minimum number of special characters  (negative indicates not to include special characters)
   * 
   * @return A rather secure password string.
   */
  public static String generatePassword( final int minLength, final int maxLength, final int minLCaseCount, final int minUCaseCount, final int minNumCount, final int minSpecialCount ) {
    char[] randomString;

    final Map<String, Integer> charGroupsUsed = new HashMap<String, Integer>();
    charGroupsUsed.put( "lcase", minLCaseCount );
    charGroupsUsed.put( "ucase", minUCaseCount );
    charGroupsUsed.put( "num", minNumCount );
    charGroupsUsed.put( "special", minSpecialCount );

    // Allocate appropriate memory for the password.
    if ( minLength < maxLength ) {
      randomString = new char[random.nextInt( maxLength - minLength ) + minLength];
    } else {
      randomString = new char[minLength];
    }

    int requiredCharactersLeft = minLCaseCount + minUCaseCount + minNumCount + minSpecialCount;

    // Build the password.
    for ( int i = 0; i < randomString.length; i++ ) {
      String selectableChars = "";

      // if we still have plenty of characters left to achieve our minimum requirements.
      if ( requiredCharactersLeft < ( randomString.length - i ) ) {
        // choose from any group at random

        // do not include those with negative minimums as this is a
        // flag indicating not to include them
        if ( minLCaseCount >= 0 )
          selectableChars = selectableChars + LCaseChars;

        if ( minUCaseCount >= 0 )
          selectableChars = selectableChars + UCaseChars;

        if ( minNumCount >= 0 )
          selectableChars = selectableChars + NumericChars;

        if ( minSpecialCount >= 0 )
          selectableChars = selectableChars + SpecialChars;

      } else {
        // choose only from a group that we need to satisfy a minimum for.
        for ( final Entry<String, Integer> charGroup : charGroupsUsed.entrySet() ) {
          if ( charGroup.getValue() > 0 ) {
            switch ( charGroup.getKey() ) {
              case "lcase":
                selectableChars += LCaseChars;
                break;
              case "ucase":
                selectableChars += UCaseChars;
                break;
              case "num":
                selectableChars += NumericChars;
                break;
              case "special":
                selectableChars += SpecialChars;
                break;
            }
          }
        }
      }

      // Now that the string is built, get the next random character.
      final char nextChar = selectableChars.charAt( random.nextInt( selectableChars.length() - 1 ) );

      // Tack it onto our password.
      randomString[i] = nextChar;

      // force it to a string
      final String seq = "" + nextChar;

      int count = 0;
      // Now figure out where it came from, and decrement the appropriate minimum value.
      if ( LCaseChars.contains( seq ) ) {
        count = charGroupsUsed.get( "lcase" );
        charGroupsUsed.put( "lcase", --count );
        if ( count >= 0 ) {
          requiredCharactersLeft--;
        }
      } else if ( UCaseChars.contains( seq ) ) {

        count = charGroupsUsed.get( "ucase" );
        charGroupsUsed.put( "ucase", --count );
        if ( count >= 0 ) {
          requiredCharactersLeft--;
        }

      } else if ( NumericChars.contains( seq ) ) {
        count = charGroupsUsed.get( "num" );
        charGroupsUsed.put( "num", --count );
        if ( count >= 0 ) {
          requiredCharactersLeft--;
        }

      } else if ( SpecialChars.contains( seq ) ) {

        count = charGroupsUsed.get( "special" );
        charGroupsUsed.put( "special", --count );
        if ( count >= 0 ) {
          requiredCharactersLeft--;
        }

      }

    }
    return new String( randomString );
  }




  /**
   * Default password generation strategy.
   */
  public PasswordStrategy() {
    try {
      random = SecureRandom.getInstance( "SHA1PRNG" );
    } catch ( final NoSuchAlgorithmException e ) {
      e.printStackTrace();
    }

  }




  /**
   * Create a password strategy with specific criteria.
   * 
   * @param min Minimum length of the password
   * @param max Maximum length of the password
   * @param lower minimum number of lower case characters (negative indicates not to include lower case characters)
   * @param upper minimum number of upper case characters (negative indicates not to include upper case characters)
   * @param digits minimum number of digits (negative indicates not to include digits)
   * @param specials minimum number of special characters  (negative indicates not to include special characters)
   */
  public PasswordStrategy( final int min, final int max, final int lower, final int upper, final int digits, final int specials ) {
    this();
    _min = min;
    _max = max;
    _lower = lower;
    _upper = upper;
    _digits = digits;
    _specials = specials;
  }




  /**
   * @see coyote.testdata.GenerationStrategy#getData(coyote.testdata.Row)
   */
  @Override
  public Object getData( final Row row ) {
    return generatePassword( _min, _max, _lower, _upper, _digits, _specials );
  }
}
