/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.task;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
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
   * @return the condition which must evaluate to true before the task is to 
   *         execute.
   */
  public String getCondition() {
    if ( configuration.containsIgnoreCase( ConfigTag.CONDITION ) ) {
      return configuration.getString( ConfigTag.CONDITION );
    }
    return null;
  }




  /**
   * @return true if the task is to generate an error and exit when an error 
   *         occurs, false the task will just exit without setting the context 
   *         to an error state and aborting the transform process.
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
  public void setHaltOnError( boolean flag ) {
    this.haltOnError = flag;
  }




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config cfg ) throws ConfigurationException {
    super.setConfiguration( cfg );

    // If there is a halt on error flag, then set it, otherwise keep the 
    // default value of true    
    if ( contains( ConfigTag.HALT_ON_ERROR ) ) {
      setHaltOnError( getBoolean( getConfiguration().getFieldIgnoreCase( ConfigTag.HALT_ON_ERROR ).getName() ) );
    }

  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    this.context = context;
    evaluator.setContext( context );

    // Look for a conditional statement the task may use to control if it is 
    // to execute or not
    if ( StringUtil.isNotBlank( getCondition() ) ) {
      try {
        evaluator.evaluateBoolean( getCondition() );
      } catch ( final IllegalArgumentException e ) {
        context.setError( "Invalid boolean expression in writer: " + e.getMessage() );
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

    if ( isEnabled() ) {
      if ( getCondition() != null ) {
        try {
          if ( evaluator.evaluateBoolean( getCondition() ) ) {
            performTask();
          }
        } catch ( final IllegalArgumentException e ) {
          Log.error( LogMsg.createMsg( CDX.MSG, "Task.boolean_evaluation_error", getCondition(), e.getMessage() ) );
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
   * @throws TaskException
   */
  protected void performTask() throws TaskException {
    // do nothing method
  }




  /**
   * Resolve the argument.
   * 
   * <p>This will try to retrieve the value from the transform context using 
   * the given value as it may be a reference to a context property.</p>
   * 
   * <p>If no value was found in the look-up, then the value is treated as a 
   * literal and will be returned as the argument.</p>
   * 
   * <p>Regardless of whether or not the value was retrieved from the 
   * transform context as a reference value, the value is resolved as a 
   * template using the symbol table in the transform context. This allows for 
   * more dynamic values during the operation of the entire transformation 
   * process.</p>
   * 
   * @param value the value to resolve (or use as a literal)
   * 
   * @return the resolved value of the argument. 
   */
  protected String resolveArgument( String value ) {
    String retval = null;

    // lookup the value in the transform context
    String cval = context.getAsString( value );

    // If the lookup failed, just use the value
    if ( StringUtil.isBlank( cval ) ) {
      cval = value;
    }

    // in case it is a template, resolve it to the context's symbol table
    if ( StringUtil.isNotBlank( cval ) ) {
      retval = Template.resolve( cval, context.getSymbols() );
    }
    return retval;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {}

}
