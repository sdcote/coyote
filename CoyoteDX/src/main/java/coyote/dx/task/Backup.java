/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.io.IOException;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;


/**
 * Make a copy of a file in the given file using a generation naming 
 * scheme effectively rotating the files.
 * 
 * <p>This task will create a copy of the file and append a number to the end 
 * of the file name. The latest backup is always 1; as all the files are named 
 * according to the age of their generation. For example FILE.1 is renamed to 
 * FILE.2 before the backup is created as FILE.1 so no data is lost.</p>
 *
 * <p>If a limit is specified, then only that number of generations will be 
 * preserved. If limit is less than the current number of generations, the 
 * excess files will be removed. A limit of 0 (zero) is effectively 
 * interpreted as no limit. Any negative value is interpreted as zero. The 
 * default limit is zero.
 */
public class Backup extends AbstractFileTask {

  private static final int DEFAULT_LIMIT = 0;




  public String getFilename() {
    if (configuration.containsIgnoreCase(ConfigTag.FILE)) {
      return configuration.getString(ConfigTag.FILE);
    }
    return null;
  }




  public int getLimit() {
    if (configuration.containsIgnoreCase(ConfigTag.LIMIT)) {
      return configuration.getInt(ConfigTag.LIMIT);
    }
    return DEFAULT_LIMIT;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    String filename = getFilename();
    if (StringUtil.isNotBlank(filename)) {
      File file = new File(filename);
      try {
        FileUtil.createGenerationalBackup(file, getLimit());
      } catch (IOException e) {
        throw new TaskException("Could not backup file: " + e.getMessage(), e);
      }
    }
  }

}
