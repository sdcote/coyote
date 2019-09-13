/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

import java.util.ArrayList;
import java.util.List;


/**
 * This class encapsulates an Encoded Query.
 *
 * <p>A filter is a list of clauses (field, predicate, value) which are AND'ed together to form a query. Only the
 * records matching all the clauses are returned.</p>
 *
 * <p>It is used to restrict the number of records return by a requests. The following two examples are equivalent.</p>
 *
 * <p>Example 1:</p>
 * <pre>
 * SnowFilter filter = new SnowFilter("category=printer^active=true");
 * </pre>
 * <p>Example 2:</p>
 * <pre>
 * SnowFilter filter = new SnowFilter("category", Predicate.IS, "printer").and("active", Predicate.IS, "true");
 * </pre>
 * <p>
 * TODO: The encoded query provides support for order by. To sort responses
 * <p>based on certain fields, use the ORDERBY and ORDERBYDESC clauses in sysparm_query. For example,</p>
 * <pre>sysparm_query=active=true^ORDERBYnumber^ORDERBYDESCcategory</pre>
 * <p>filters all active records and orders the results in ascending order by number first, and then in descending
 * order by category.</p>
 */
public class SnowFilter {

  final public static String AND = "^";
  final public static String OR = "^OR";
  List<SnowClause> clauses = new ArrayList<SnowClause>();


  /**
   * Default constructor.
   */
  public SnowFilter() {
  }

  /**
   * Create a filter with the given column, predicate and value.
   *
   * @param column    the column to check
   * @param predicate how to check the given column
   * @param value     the value against which the column is to be checked.
   */
  public SnowFilter(final String column, final Predicate predicate, final String value) {
    this.and(column, predicate, value);
  }


  /**
   * Create a filter with the given column, predicate.
   *
   * <p>This is a convenience constructor for those predicates which do not
   * require a value, e.g. IS_NOT_BLANK.</p>
   *
   * @param column    the column to check
   * @param predicate how to check the given column
   */
  public SnowFilter(final String column, final Predicate predicate) {
    this.and(column, predicate, null);
  }


  /**
   * Perform URL encoding.
   *
   * @param string the URL string to encode
   * @return encoded string.
   */
  public static String encode(final String string) {
    byte[] bytes = null;
    bytes = string.getBytes();

    final int len = bytes.length;
    final byte[] encoded = new byte[bytes.length * 3];
    int n = 0;
    boolean noEncode = true;

    for (int i = 0; i < len; i++) {
      final byte b = bytes[i];

      // TODO: Change to a switch and cover all the characters in RFC-3986 (Section 2: Characters) 

      //      if ( b == ' ' ) {
      //        noEncode = false;
      //        encoded[n++] = (byte)'+';
      //      } else {
      if (((b >= 'a') && (b <= 'z')) || ((b >= 'A') && (b <= 'Z')) || ((b >= '0') && (b <= '9')) || (b == '_') || (b == '-') || (b == '.')) {
        encoded[n++] = b;
      } else {
        noEncode = false;
        encoded[n++] = (byte) '%';

        byte nibble = (byte) ((b & 0xf0) >> 4);

        if (nibble >= 10) {
          encoded[n++] = (byte) (('A' + nibble) - 10);
        } else {
          encoded[n++] = (byte) ('0' + nibble);
        }

        nibble = (byte) (b & 0xf);

        if (nibble >= 10) {
          encoded[n++] = (byte) (('A' + nibble) - 10);
        } else {
          encoded[n++] = (byte) ('0' + nibble);
        }
      }
      //}
    }

    if (noEncode) {
      return string;
    }

    return new String(encoded, 0, n);
  }


  /**
   * Adds a datetime range to a filter.
   *
   * @param starting Select records updated on or after this datetime
   * @param ending   Select records updated before this datetime
   * @return The modified original filter
   */
  public SnowFilter addUpdatedFilter(final SnowDateTime starting, final SnowDateTime ending) {
    if (starting != null) {
      this.and(ServiceNow.SYS_UPDATED_ON_FIELD, Predicate.GREATER_THAN_EQUALS, starting.toString());
    }
    if (ending != null) {
      this.and(ServiceNow.SYS_UPDATED_ON_FIELD, Predicate.LESS_THAN, ending.toString());
    }
    return this;
  }


  /**
   * @param clause the clause to add to this filter as an AND operation
   * @return a reference to this filter for method chaining
   */
  public SnowFilter and(final SnowClause clause) {
    clauses.add(clause);
    return this;
  }


  /**
   * @param column    the column to check
   * @param predicate how to check the given column
   * @param value     the value to query
   * @return a reference to this filter for method chaining
   */
  public SnowFilter and(final String column, final Predicate predicate, final String value) {
    return and(new SnowClause(column, predicate, value));
  }


  /**
   * @param column    the column to check
   * @param predicate how to check the given column
   * @return a reference to this filter for method chaining
   */
  public SnowFilter and(final String column, final Predicate predicate) {
    return and(new SnowClause(column, predicate, null));
  }


  public boolean isEmpty() {
    return (clauses.size() == 0);
  }


  /**
   * @param clause a query clause to add to the filter as an OR operation
   * @return a reference to this filter for method chaining
   */
  public SnowFilter or(final SnowClause clause) {
    clause.isOR = true;
    clauses.add(clause);
    return this;
  }


  /**
   * @param column    the name of the field to query
   * @param predicate the predicate
   * @param value     the value to query
   * @return a reference to this filter for method chaining
   */
  public SnowFilter or(final String column, final Predicate predicate, final String value) {
    return or(new SnowClause(column, predicate, value));
  }


  /**
   * @return a URL encoded query string
   */
  public String toEncodedString() {
    return encode(toString());
  }


  @Override
  public String toString() {
    final StringBuffer b = new StringBuffer();

    // If we have clauses to encode...
    if (clauses.size() > 0) {
      SnowClause clause = null;
      // go through each clause and add it to the return buffer
      for (int index = 0; index < clauses.size(); index++) {
        clause = clauses.get(index);

        if (index > 0) {
          b.append('^');
          if (clause.isOR) {
            b.append("OR");
          }
        }

        b.append(clause.field);
        b.append(clause.pred.toString());
        b.append(clause.value);
      }
    }

    return b.toString();
  }


  /**
   * @return the number of clauses
   */
  public int clauseCount() {
    return clauses.size();
  }

  /**
   * A triplet of column, predicate and value.
   */
  public class SnowClause {
    String field;
    Predicate pred;
    String value;
    boolean isOR = false;


    public SnowClause(final String fieldName, final Predicate predicate, final String fieldValue) {
      if ((fieldName != null) && (fieldName.length() > 0)) {
        field = fieldName;
      } else {
        throw new IllegalArgumentException("Null or empty field name");
      }

      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        value = fieldValue;
      } else {
        if (predicate.requiresValue()) {
          throw new IllegalArgumentException("Null or empty field value");
        } else {
          value = "";
        }
      }
      pred = predicate;
    }
  }

}
