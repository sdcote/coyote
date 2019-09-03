/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

/**
 * https://docs.servicenow.com/bundle/jakarta-servicenow-platform/page/use/common-ui-elements/reference/r_OpAvailableFiltersQueries.html?title=Operators_Available_for_Filters_and_Queries
 */
public enum Predicate {
  BETWEEN("BETWEEN", 2),
  CONTAINS("LIKE", 1),
  DIFFERENT_FROM("NSAMEAS", 1),
  DOES_NOT_CONTAIN("NOT LIKE", 1),
  ENDS_WITH("ENDSWITH", 1),
  GREATER_THAN(">", 1),
  GREATER_THAN_EQUALS(">=", 1),
  IN("IN", 1),
  IS("=", 1),
  IS_ANYTHING("ANYTHING", 0),
  IS_EMPTY("ISEMPTY", 0),
  IS_EMPTY_STRING("EMPTYSTRING", 0),
  IS_NOT("!=", 1),
  IS_NOT_EMPTY("ISNOTEMPTY", 0),
  KEYWORDS("123TEXTQUERY321", 1),
  LESS_THAN("<", 1),
  LESS_THAN_EQUALS("<=", 1),
  NOT_IN("NOT IN", 1),
  SAME_AS("SAMEAS", 1),
  STARTS_WITH("STARTSWITH", 1);

  private final String value;
  private final int argumentCount;


  Predicate(final String value, final int args) {
    this.value = value;
    argumentCount = args;
  }


  /**
   * Return a predicate by its name.
   *
   * @param name the name of the predicate to return (case insensitive)
   * @return the predicate with that name
   */
  public static Predicate getPredicateByName(String name) {
    if (name != null) {
      for (Predicate type : Predicate.values()) {
        if (name.equalsIgnoreCase(type.toString())) {
          return type;
        }
      }
    }
    return null;
  }


  /**
   * @see Enum#toString()
   */
  @Override
  public String toString() {
    return value;
  }


  /**
   * @return true if the predicate requires a value argument
   */
  public boolean requiresValue() {
    return argumentCount > 0;
  }


  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }


  /**
   * @return the argumentCount
   */
  public int getArgumentCount() {
    return argumentCount;
  }

}
