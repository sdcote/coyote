/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.commons.CommandLineProcess;
import coyote.commons.CommandLineProcess.Result;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;


/**
 * Runs a process on the host operating system.
 * 
 * <p>This can be a script or other program and the command line is templated 
 * so all the symbols can be used in the invocation.
 */
public class Process extends AbstractTransformTask {

  protected static final String COMMAND = "Command";
  protected static final String EXIT_CODE = "ExitCode";
  protected static final String START = "Start";
  protected static final String DURATION = "Duration";
  protected static final String OUTPUT = "Output";
  protected static final String ERROR = "Error";
  private static final String DELIMITER = ".";




  /**
  * @see coyote.dx.task.AbstractTransformTask#performTask()
  */
  @Override
  protected void performTask() throws TaskException {

    final String command = getString(COMMAND);
    final String name = getString(ConfigTag.NAME);
    final String commandLine = Template.resolve(command, getContext().getSymbols());

    if (StringUtil.isNotBlank(command)) {
      Result result = CommandLineProcess.exec(commandLine);

      if (Log.isLogging(Log.DEBUG_EVENTS)) {
        Log.debug("Executed '" + commandLine + "' - exit code: " + result.getExitCode() + "  " + result.getDuration() + "ms");
        StringBuffer b = new StringBuffer("STDOUT:\n");
        String[] output = result.getOutput();
        for (int x = 0; x < output.length; x++) {
          b.append(output[x]);
          if (x + 1 < output.length) {
            b.append("\n");
          }
        }
        Log.debug(b);
      }

      String contextKey;
      if (StringUtil.isBlank(name)) {
        contextKey = command.split("\\s+")[0];
      } else {
        contextKey = name;
      }
      getContext().set(contextKey + DELIMITER + EXIT_CODE, result.getExitCode());
      getContext().set(contextKey + DELIMITER + DURATION, result.getDuration());
      getContext().set(contextKey + DELIMITER + START, result.getStartTime());
      getContext().set(contextKey + DELIMITER + COMMAND, result.getCommand());
      getContext().set(contextKey + DELIMITER + OUTPUT, result.getOutput());
      getContext().set(contextKey + DELIMITER + ERROR, result.getError());

      if (result.getExitCode() != 0) {
        StringBuffer b = new StringBuffer("Error running '");
        b.append(commandLine);
        b.append("'");
        String[] errors = result.getError();
        if (errors.length > 0) {
          b.append(" STDERR:\n");
          for (int x = 0; x < errors.length; x++) {
            b.append(errors[x]);
            b.append("\n");
          }
        } else {
          b.append(", (no data on STDERR)");
        }

        if (haltOnError) {
          throw new TaskException(b.toString());
        } else {
          Log.error(b);
          return;
        }
      }
    } else {
      Log.notice("Process had no command to execute.");
    }
  }

}
