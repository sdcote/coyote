/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;


/**
 * This is a utility class which assists components paginate through data by
 * incrementing a set of variables and exposing them as symbols which can be 
 * merged into symbol tables allowing for the creation of dynamic templates 
 * that can be used to query through data a batch at a time.
 * 
 * <p>Each pagination has a name and multiple paginations can be created for a 
 * component. The Pagination generates symbols with the name prepended to the
 * element of the pagination. The {@code batch} pagination will have the 
 * variables of {@code batch.start} indicating the first record of the batch,
 * {@code batch.size}, which is the absolute value of the increment and {@code 
 * batch.end} which is the last record in the batch.   
 */
public class Pagination {
  protected static final String DEFAULT_NAME = "page";
  private volatile long offset = 0;
  private final String name;
  private final long step;
  private final long start;




  public Pagination(long step) {
    this(DEFAULT_NAME, 0, step);
  }




  public Pagination(long start, long step) {
    this(DEFAULT_NAME, start, step);
  }




  public Pagination(String name, long step) {
    this(name, 0, step);
  }




  public Pagination(String name, long start, long step) {
    if (StringUtil.isBlank(name)) {
      this.name = DEFAULT_NAME;
    } else {
      this.name = name;
    }
    this.start = start;
    this.step = step;
    reset();
  }




  /**
   * @return the current pointer
   */
  public long getOffset() {
    return offset;
  }




  /**
   * @param offset the record position in the pagination to set
   */
  public synchronized void setOffset(long offset) {
    this.offset = offset;
  }




  /**
   * @return the name of this pagination
   */
  public String getName() {
    return name;
  }




  /**
   * @return the step value (positive or negative)
   */
  public long getStep() {
    return step;
  }




  /**
   * @return the starting position in the batch (usually 0)
   */
  public long getStart() {
    return start;
  }




  /**
   * @return the ending position in the batch
   */
  public long getEnd() {
    return offset + step;
  }




  /**
   * Set the pagination to the next page
   */
  public synchronized void step() {
    offset += step;
  }




  /**
   * Reset the pagination to the beginning, start value.
   */
  public synchronized void reset() {
    this.offset = start;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("Pagination: '");
    b.append(getName());
    b.append("' start:").append(getStart());
    b.append(" step:").append(getStep());
    return b.toString();
  }




  /**
   * @return the symbol table of this pagination variables.
   */
  @SuppressWarnings("unchecked")
  public synchronized SymbolTable toSymbolTable() {
    SymbolTable retval = new SymbolTable();
    retval.put(name + ".start", getOffset());
    retval.put(name + ".size", getStep());
    retval.put(name + ".end", getEnd());
    return retval;
  }

}
