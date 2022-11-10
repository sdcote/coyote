/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.dx.eval.Evaluator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This defines a contract for components that use an Evaluator to determine
 * if processing should occur.
 */
public interface ConditionalComponent extends ConfigurableComponent {


    /**
     * Return the conditional expression from the configuration.
     *
     * @return the condition which must evaluate to true before the aggregator
     * is to execute.
     */
    String getCondition();


    /**
     * Access the Evaluator this component uses for evaluation of the condition.
     *
     * @return the currently set evaluator.
     */
    Evaluator getEvaluator();


    /**
     * Set the evaluator this component uses for conditional operation.
     *
     * @param evaluator the evaluator to set.
     */
    void setEvaluator(Evaluator evaluator);


    /**
     * Check to see if the currently set condition evaluates to true with the
     * currently set context.
     *
     * @return true if the condition evaluates to true, false otherwise.
     */
    boolean conditionIsSatisfied();

}
