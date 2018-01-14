/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.util.List;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This task searches for a set of files in a directory and runs a job with the 
 * filename set in the context.
 * 
 * <p>The FileJob enables the ability to scan a directory and run a job for 
 * each of the files found in that directory. This is a common use case for 
 * jobs which store job data in a directory and a FileJob to process them when
 * those files appear. This is usefule for buffering jobs during an outage and
 * processing them when the system comes back up.
 * 
 * <p>A group of FIleJobs can be set up to "watch" a dedicated directory and a 
 * "dispatching" FileJob can take files out of a main directory and pass each 
 * file to one of the "watched" directories enabling a fan-out pattern of 
 * workers. This allows the "dispatcher" to be throttled and even disabled to 
 * suspend processing while the jobs collect in the "dispatching" directory. 
 * The "dispatcher" can then be started at a later time, sending jobs to other
 * directories for processing.
 * 
 * <p>Another use case is the analytics of data in batches. Each batch of data 
 * can be set in a separate file and and an analytic job run on each batch 
 * with the results being appended (i.e. aggregated) to a file for later 
 * reporting.
 * 
 * <p>When using the FileJob task as the root of processing, it is often 
 * useful to use the {@code Repeat} Job parameter to have the job run 
 * continually. When combined with the  {@code Sleep} task, the FileTaks can
 * run continually in a single-threaded manner, keeping the targeted directory
 * clean of files, regularly dispatching them to other directories for other
 * jobs to process.
 * 
 * <p>This task can be configured thusly:<pre>
 * "FileJob" : { 
 *   "directory": "\datadir", 
 *   "file": "somejob.json", 
 *   "name": "jobname", 
 *   "pattern": "([^\\s]+(\\.(?i)(csv))$)", 
 *   "context": "TargetFile", 
 *   "recurse": false 
 * }</pre>
 * 
 * <p>The {@code directory} is the path to the directory from which a file 
 * listing is taken. It is mandatory and always best to specify an absolute 
 * path to eliminate confusion.
 * 
 * <p>The {@code pattern} is the RegEx pattern filenames art to match. If not
 * specified, all files in the directory will be returned.
 * 
 * <p>The {@code context} configuration attribute specified the name of the 
 * context variable the filename will be placed. If omitted, 'FileJob' will be 
 * used.
 * 
 * <p>The {@code recurse} parameter instructs the task to recurse all 
 * subdirectories when locating files. The default is {@code false} indicating 
 * only the files in the specified directory will be returned (if they match 
 * the pattern).
 * 
 * <p>The {@code file} parameter specifies the data transfer job configuration 
 * to run. This is a required field as it specifies the job to run for each 
 * matching file.
 * 
 * <p>The {@code name} parameter specifies the name to use for the job. This
 * allows for the publication of data in different locations than those 
 * specified in the configuration file or the default values. 
 */
public class FileJob extends RunJob implements TransformTask {

  private static final String DEFAULT_KEY = "FileJob";
  private File directory = null;
  private long runCount=0;;




  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // Check for the directory
    final String directoryName = getString(ConfigTag.DIRECTORY);
    if (StringUtil.isNotBlank(directoryName)) {
      File dir = new File(directoryName);
      if (dir.isDirectory()) {
        if (dir.exists()) {
          if (dir.canRead()) {
            directory = dir;
            Log.debug(getClass().getSimpleName() + " using a directory value of " + getDirectory());
          } else {
            throw new ConfigurationException("Value specified in '" + ConfigTag.DIRECTORY + "' configuration attribute is not readable: " + directoryName);
          }
        } else {
          throw new ConfigurationException("'" + ConfigTag.DIRECTORY + "' configuration value does not exist: " + directoryName);
        }
      } else {
        throw new ConfigurationException("Value specified in '" + ConfigTag.DIRECTORY + "' configuration attribute is not a directory: " + directoryName);
      }
    } else {
      throw new ConfigurationException("Required configuration attribute '" + ConfigTag.DIRECTORY + "' not specified in configuration.");
    }

  }




  
  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    runCount = 0;
  }





  @Override
  protected void performTask() throws TaskException {

    List<File> flist = FileUtil.getFiles(getDirectory(), getPattern(), isRecursive());
    for (File file : flist) {
      getContext().set(getContextKey(), file.getAbsolutePath());
      super.performTask();
      runCount++;
    }
  }




  
  private String getContextKey() {
    String retval = getString(ConfigTag.CONTEXT);
    if (StringUtil.isBlank(retval)) {
      retval = DEFAULT_KEY;
    }
    return retval;
  }




  
  private boolean isRecursive() {
    return getBoolean(ConfigTag.RECURSE);
  }




  
  private String getPattern() {
    return getString(ConfigTag.PATTERN);
  }




  
  private File getDirectory() {
    return directory;
  }





  /**
   * @return the number of times the job was executed
   */
  public long getRunCount() {
    return runCount;
  }

}
