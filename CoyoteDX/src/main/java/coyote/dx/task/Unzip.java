/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.io.IOException;

import coyote.commons.StringUtil;
import coyote.commons.ZipUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Unzip the given file.
 */
public class Unzip extends AbstractFileTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String source = getSourceOrFile();
    if (StringUtil.isNotBlank(source)) {
      Log.debug(getClass().getSimpleName() + " using a filename of '" + source + "'");;
      final File file = getExistingFile(source);
      Log.debug(getClass().getSimpleName() + " using absolute filename of '" + file.getAbsolutePath() + "'");;

      if (file.exists()) {
        if (file.canRead()) {
          if (file.length() > 0) {
            try {
              ZipUtil.unzip(file, getDirectory());
            } catch (IOException e) {
              throw new TaskException("Could not unzip file: " + e.getMessage(), e);
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
   * @return the target directory for the unzipped files
   */
  private File getDirectory() {
    String directory = getString(ConfigTag.DIRECTORY);
    if (StringUtil.isNotBlank(directory)) {
      File retval = new File(directory);
      if (!retval.isAbsolute()) {
        retval = new File(getJobDir(), directory);
      }
      return retval;
    } else {
      return new File(getJobDir());
    }
  }

}
