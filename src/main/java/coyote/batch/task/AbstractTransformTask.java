/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.task;

import java.io.IOException;

import coyote.batch.AbstractConfigurableComponent;
import coyote.batch.ConfigTag;
import coyote.batch.ConfigurationException;
import coyote.batch.TaskException;
import coyote.batch.TransformContext;
import coyote.batch.TransformTask;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;


/**
 * This is the base class for pre and post processing tasks.
 * 
 * <p>Values in the configuration are first used as keys to the context. If 
 * there is no value with that name in the context, the task uses the value as 
 * a literal argument. The primary use case is to just use the literal value in 
 * the configuration, but this context look-up gives the tasks the ability to 
 * get dynamic values from the context which were placed there by other 
 * components during runtime operation.</p>
 */
public abstract class AbstractTransformTask extends AbstractConfigurableComponent implements TransformTask {
  protected boolean haltOnError = true;
  protected boolean enabled = true;




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
   * @see coyote.batch.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // if there is an enabled flag, set it; otherwise default to true
    if ( contains( ConfigTag.ENABLED ) ) {
      setEnabled( getBoolean( ConfigTag.ENABLED ) );
    }

  }




  /**
   * @see coyote.batch.Component#open(coyote.batch.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    this.context = context;

    // If there is a halt on error flag, then set it, otherwise keep the 
    // default value of true    
    if ( contains( ConfigTag.HALT_ON_ERROR ) ) {
      setHaltOnError( getBoolean( ConfigTag.HALT_ON_ERROR ) );
    }

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
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }




  /**
   * @return true if this task is enabled to run, false if the tasks is not to be executed
   */
  @Override
  public boolean isEnabled() {
    return enabled;
  }




  /**
   * @param flag true to enable this task, false to prevent it from being executed.
   */
  @Override
  public void setEnabled( boolean flag ) {
    this.enabled = flag;
  }




  /**
   * @see coyote.batch.TransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    // TODO Auto-generated method stub

  }

}
