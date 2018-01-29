/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Read a text file into the context as a set of name-value pairs.
 *
 * <p>This allows a job to be populated with context variables at runtime.
 *
 * <p>The file is assumed to be a simple text file containing name-value
 * pairs. Each line is expected to contain one pair delimited with an equals
 * '=' sign. Other characters can be specified as a delimiter.
 * 
 * <p>This task can be configured as follows:<pre>
 * "ReadIntoContext": { "source": "nvdcve-1.0-recent.meta", "delimiter": ":" }</pre>
 * with {@code source} being the file to read and {@code delimiter} being the
 * string which separates names from values.
 */
public class ReadIntoContext extends AbstractFileTask {

  private static final String DEFAULT_DELIMITER = "=";




  /**
   * Divide the line into two strings based on the given delimiter.
   *
   * @param line the line to split
   * @param delimiter the delimiter
   *
   * @return an array of 2 elements, the first is the key, the second is the
   *         value. If there is no delimiter, the first element will be the
   *         entire string and the second element will be null.
   */
  private String[] divide(final String line, final String delimiter) {
    final String[] retval = new String[2];
    final int indx = line.indexOf(delimiter);
    if (indx != -1) {
      retval[0] = line.substring(0, indx);
      if (indx < line.length()) {
        retval[1] = line.substring(indx + 1, line.length());
      }
    } else {
      retval[0] = line;
    }
    return retval;
  }




  /**
   * @return the string which delimits the key from the value or the default
   *         value if no delimiter configuration attribute is found.
   */
  private String getDelimiter() {
    final String separator = getString(ConfigTag.DELIMITER);
    if (StringUtil.isNotBlank(separator)) {
      return separator;
    }
    return DEFAULT_DELIMITER;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void performTask() throws TaskException {
    final String delimiter = getDelimiter();
    final String source = getSourceOrFile();
    if (StringUtil.isNotBlank(source)) {
      Log.debug("Using a filename of '" + source + "'");;
      final File file = getExistingFile(source);
      if (file == null) {
        throw new TaskException("Cannot read file into context - does not exist: " + source);
      } else {
        Log.debug("Using absolute filename of '" + file.getAbsolutePath() + "'");;
      }
      if (file.exists()) {
        if (file.canRead()) {
          if (file.length() > 0) {
            final String[] lines = FileUtil.textToArray(file);
            for (final String line : lines) {
              final String[] kvp = divide(line, delimiter);
              if (StringUtil.isNotEmpty(kvp[1])) {
                getContext().set(kvp[0], kvp[1]);
                if (isPopulatingSymbols()) {
                  getContext().getSymbols().put(kvp[0], kvp[1]);
                }
                Log.debug("Recording '" + kvp[0] + "' in contex as '" + kvp[1] + "'");
              } else {
                Log.warn("No delimiter for line: '" + line + "'");
              }
            }
          } else {
            Log.warn(LogMsg.createMsg(CDX.MSG, "%s did not read any data from %s - empty file (%s)", getClass().getSimpleName(), source, file.getAbsolutePath()));
          }
        } else {
          final String msg = LogMsg.createMsg(CDX.MSG, "Task.failed_file_cannot_be_read", getClass().getSimpleName(), source, file.getAbsolutePath()).toString();
          if (haltOnError) {
            throw new TaskException(msg);
          } else {
            Log.error(msg);
            return;
          }
        }
      } else {
        final String msg = LogMsg.createMsg(CDX.MSG, "Task.failed_file_does_not_exist", getClass().getSimpleName(), source, file.getAbsolutePath()).toString();
        if (haltOnError) {
          throw new TaskException(msg);
        } else {
          Log.error(msg);
          return;
        }
      }
    } else {
      final String msg = LogMsg.createMsg(CDX.MSG, "%s failed: No data in %s configuration attribute", getClass().getSimpleName(), ConfigTag.SOURCE).toString();
      if (haltOnError) {
        throw new TaskException(msg);
      } else {
        Log.error(msg);
        return;
      }
    }
  }




  /**
   * @return
   */
  private boolean isPopulatingSymbols() {
    boolean retval = false;
    if (getConfiguration().containsIgnoreCase(ConfigTag.SET_SYMBOL)) {
      retval = getBoolean(ConfigTag.SET_SYMBOL);
    }
    return retval;
  }

}
