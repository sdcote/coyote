/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is the base class for pre and post processing tasks.
 * 
 * <p>Subclasses should override  {@link #performTask()} and let this class 
 * handle determining if the task should run based on the conditional 
 * statements (if one is set) and the enabled flag. This way, all conditional 
 * checks are handled uniformly across all tasks.
 * 
 * <p>Values in the configuration are first used as keys to the context. If 
 * there is no value with that name in the context, the task uses the value as 
 * a literal argument. The primary use case is to just use the literal value in 
 * the configuration, but this context look-up gives the tasks the ability to 
 * get dynamic values from the context which were placed there by other 
 * components during runtime operation.
 */
public abstract class AbstractTransformTask extends AbstractConfigurableComponent implements TransformTask {
  protected boolean haltOnError = true;
  protected Evaluator evaluator = new Evaluator();




  /**
   * Return the conditional expression from the configuration.
   * 
   * @return the condition which must evaluate to true before the task is to 
   *         execute.
   */
  public String getCondition() {
    if (configuration.containsIgnoreCase(ConfigTag.CONDITION)) {
      return configuration.getString(ConfigTag.CONDITION);
    }
    return null;
  }




  /**
   * Determine if errors should cause the task processing to terminate.
   * 
   * @return true if the task is to generate an error (throw TaskException) 
   *         and exit when an error occurs, false the task will just exit 
   *         without setting the context to an error state and aborting the 
   *         transform process.
   */
  public boolean haltOnError() {
    return haltOnError;
  }




  /**
   * Control if the task should set the context to an error state, aborting the 
   * transformation run or simply exit the task and let the rest of the 
   * components run.
   * 
   * @param flag true to abort the transform on error, false to just exit the task
   */
  public void setHaltOnError(boolean flag) {
    this.haltOnError = flag;
  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // If there is a halt on error flag, then set it, otherwise keep the 
    // default value of true    
    if (contains(ConfigTag.HALT_ON_ERROR)) {
      setHaltOnError(getBoolean(getConfiguration().getFieldIgnoreCase(ConfigTag.HALT_ON_ERROR).getName()));
    }

  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    setContext(context);
    evaluator.setContext(context);

    if (StringUtil.isNotBlank(getCondition())) {
      try {
        evaluator.evaluateBoolean(getCondition());
      } catch (final IllegalArgumentException e) {
        context.setError("Invalid boolean expression in task: " + e.getMessage());
      }
    }
  }




  /**
   * Subclasses should probably override {@link #performTask()} instead of 
   * this method so as to enable this class to handle conditional checks.
   * 
   * @see coyote.dx.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {

    if (isEnabled()) {
      if (getCondition() != null) {
        try {
          if (evaluator.evaluateBoolean(getCondition())) {
            performTask();
          } else {
            if (Log.isLogging(Log.DEBUG_EVENTS)) {
              Log.debug(LogMsg.createMsg(CDX.MSG, "Task.boolean_evaluation_false", getCondition()));
            }
          }
        } catch (final IllegalArgumentException e) {
          Log.error(LogMsg.createMsg(CDX.MSG, "Task.boolean_evaluation_error", getCondition(), e.getMessage()));
        }
      } else {
        performTask();
      }
    }
  }




  /**
   * This method is called by the AbstractTransformTask when the 
   * {@link #execute()} method is called but only if the enabled flag is set 
   * and any set conditional expression is matched or there is no conditional 
   * expression.
   * 
   * <p>Overriding this task instead of {@link #execute()} allows the 
   * AbstractTransfromTask to handle all checks in a uniform manner for all 
   * subclasses.
   * 
   * @throws TaskException if there were problems performing the task
   */
  protected void performTask() throws TaskException {
    // do nothing method
  }




  /**
   * Close this task.
   * 
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // subclass should override this to perform clean-up
  }

}
