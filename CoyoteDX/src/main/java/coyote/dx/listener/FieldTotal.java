/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.math.BigDecimal;

import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;


/**
 * Track the running total for a particular column and place the result in 
 * both the context and the symbol table for other components to use.
 * 
 * <p>Calculations are performed after the mapping operation, so the field 
 * name is expected to represent the final mapped name In the target frame
 * the writers use to generate records. 
 * 
 * <p>The name of the field is used as the base of the context and symbol 
 * table key with the suffix of {@code .total} appended.
 */
public class FieldTotal extends AbstractFieldListener implements ContextListener {

  private static final String SUFFIX = ".total";
  BigDecimal total = new BigDecimal(0D);
  String endName = "";




  /**
   * @see coyote.dx.listener.AbstractListener#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    endName = getFieldName() + SUFFIX;
  }




  /**
   * @param txnContext
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void process(TransactionContext txnContext) {
    BigDecimal value = getFieldValue(txnContext);
    if (value != null) {
      total = total.add(value, MATH_CONTEXT);
      getContext().getSymbols().put(endName, total.doubleValue());
      getContext().set(endName, total.doubleValue());
    }
  }

}
