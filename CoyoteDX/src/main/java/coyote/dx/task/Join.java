/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * Joins CSV, JSON and/or XML files together into one.
 * 
 * <p>The goal is to read any number of file/formats into one unified file for
 * subsequent processing.
 * 
 * <p>The files are read twice, once to determine their structure and a second 
 * time to process data. This first pass is required since JSON and XML files 
 * will often have varying structures and a composite structure will be needed 
 * to determine the rules for processing. Then, after a strategy is 
 * determined, each file will be read and if its record matches the strategy, 
 * it will be written to the target. 
 * 
 * <p>CSV must contain headers. In fact, only the first line of CSV files are 
 * read in when determining a strategy and therefore must be the header.
 * 
 * <p>CSV in this toolkit is characher separated format, therefore any 
 * character can be used as a delimiter including tabs and pipes.
 */
public class Join extends AbstractFileTask implements TransformTask {
  private File targetFile = null;
  private List<InputFile> inputFiles = new ArrayList<InputFile>();




  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // get each of the input files and their formats

    // get the target file and its format

    // determine the type of join to perform Left, Right, Inner, Left Outer, Right Outer, Full Outer, etc.

  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    // Make sure each of the files exist and processing can begin
  }




  /**
  * @see coyote.dx.task.AbstractTransformTask#performTask()
  */
  @Override
  protected void performTask() throws TaskException {

    // read in each of the input files and determine their structure.  

    // read each file and pass it through the strategy

    // close the target file

  }

  private class InputFile {
    File input = null;
    String format = null;
    List<String> keys = new ArrayList<String>();




    InputFile() {

    }

  }
}
