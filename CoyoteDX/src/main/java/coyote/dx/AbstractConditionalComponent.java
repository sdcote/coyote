/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.File;


/**
 * This is the base class for all components that use an Evaluator to determine
 * if processing should occur.
 */
public abstract class AbstractConditionalComponent extends AbstractConfigurableComponent implements ConditionalComponent {
    private Evaluator evaluator = new Evaluator();
    private String cachedCondition = null;


    /**
     * Return the conditional expression from the configuration.
     *
     * @return the condition which must evaluate to true before the component
     * is to execute.
     */
    @Override
    public String getCondition() {
        if (cachedCondition == null && configuration.containsIgnoreCase(ConfigTag.CONDITION)) {
            cachedCondition = configuration.getString(ConfigTag.CONDITION);
        }
        return cachedCondition;
    }


    /**
     * Access the Evaluator this component uses for evaluation of the condition.
     *
     * @return the currently set evaluator.
     */
    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }


    /**
     * Set the evaluator this component uses for conditional operation.
     *
     * <p>If the given evaluator is null, a new evaluator is created. This is
     * to prevent null pointer exceptions.</p>
     *
     * <p>If the given evaluator does not contain a context, and the current
     * evaluator does, the currently set context will be placed in the given
     * evaluator.</p>
     *
     * @param evaluator the evaluator to set.
     */
    @Override
    public void setEvaluator(Evaluator evaluator) {
        if (evaluator != null) {
            TransformContext context = this.evaluator.getContext();
            this.evaluator = evaluator;
            if (this.evaluator.getContext() == null && context != null) this.evaluator.setContext(context);
        } else {
            this.evaluator = new Evaluator();
        }
    }


    /**
     * Set the context in the evaluator, so it can resolve variables.
     *
     * <p>If no evaluator is set, this operation is ignored.</p>
     *
     * @param context the context to set.
     */
    public void setConditionalContext(TransformContext context) {
        if (evaluator != null) evaluator.setContext(context);
    }


    /**
     * Check to see if the currently set condition evaluates to true with the
     * currently set context.
     *
     * @return true if the condition evaluates to true, false otherwise.
     */
    @Override
    public boolean conditionIsSatisfied() {
        return getEvaluator().evaluateBoolean(getCondition());
    }

}
