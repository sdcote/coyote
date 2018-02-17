/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransformContext;
import coyote.dx.task.AbstractTransformTask;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * Base class for listeners which execute a task when a condition is met.
 */
public abstract class AbstractMonitoringListener extends AbstractListener implements ContextListener {

  private static final String CONFIG = "Config";
  /** Constant to assist in determining the full class name of tasks */
  private static final String TASK_PKG = AbstractTransformTask.class.getPackage().getName();

  private TransformTask monitorTask = null;
  private TransformContext taskContext = null;




  /**
   * @see coyote.dx.listener.AbstractListener#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    taskContext = new TransformContext();

    // populate the task context
    for (String key : context.getKeys()) {
      taskContext.set(key, context.get(key));
    }

    // populate the task synbol table with a deep copy of our table 
    taskContext.setSymbols((SymbolTable)context.getSymbols().clone());
  }




  /**
   * Execute the configured task.
   * 
   * <p>The given frame is used to populate the symbol table and context to 
   * assist in customizing the operation to the monitoring event. The frame 
   * normally contains the fields of the frame triggering the event and other
   * field particular to the monitor and the nature of the event. Refer to the
   * documentation of the monitor for details. 
   * 
   * @param frame containing context and symbol data the task
   */
  @SuppressWarnings("unchecked")
  protected void executeTask(DataFrame frame) {
    TransformTask task = getOrCreateTask();
    try {
      if (task.isEnabled()) {
        task.open(taskContext);
      }
    } catch (Exception e) {
      taskContext.setError(e.getMessage());
    }

    if (taskContext.isNotInError()) {
      if (task.isEnabled()) {

        // set the frame in the context and in the symbol table
        for (DataField field : frame.getFields()) {
          taskContext.set(field.getName(), field.getValue());
          taskContext.getSymbols().put(field.getName(), field.getStringValue());
        }

        try {
          task.execute();
        } catch (TaskException e) {
          e.printStackTrace();
        } finally {
          try {
            task.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      } else {
        Log.debug("skipping disabled monitor task " + task.getClass().getSimpleName());
      }
    } else {
      Log.debug("Monitor task could not be opened: " + taskContext.getErrorMessage());
    }
  }




  private TransformTask getOrCreateTask() {
    TransformTask retval = monitorTask;
    if (retval == null) {
      Config taskConfig = getConfiguration().getSection(CONFIG);
      String className = getString(ConfigTag.TASK);
      if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
        className = TASK_PKG + "." + className;
      }
      Object object = CDX.createComponent(className, taskConfig);
      if (object != null) {
        if (object instanceof TransformTask) {
          monitorTask = (TransformTask)object;
          retval = monitorTask;
          Log.debug(getClass().getSimpleName() + " created task");
        } else {
          Log.error(getClass().getSimpleName() + "could not create task: class '" + object.getClass().getName() + "' is not a transform task");
        }
      } else {
        Log.error(getClass().getSimpleName() + "could not create task: could not locate '" + className + "'");
      }
    }
    return retval;
  }

}
