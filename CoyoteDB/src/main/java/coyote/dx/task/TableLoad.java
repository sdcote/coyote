/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.reader.CSVReader;
import coyote.dx.reader.JSONReader;
import coyote.dx.writer.JdbcWriter;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * This wraps a FrameReader and a JdbcWriter together to perform the loading of 
 * a file into a database.
 * 
 * <p>There are just some things that are best perfomed in a database and 
 * through the use of lighweight databases such as H2 and SQLlite it is 
 * possible to stage large amounts of data on disk without consuming large 
 * amounts of memory. The data can then be used in reader, writers, transforms 
 * and other components without replicating functionality in the framework.
 * 
 * <p>It is possible to create a database for a specific task and then remove 
 * it from disk once the job is complete with post-processing tasks. This 
 * allows for some rather dynamic processing as CSV files can be generated in 
 * other jobs before this job is executed, allowing for the creating of JDBC
 * databases with fresh data. 
 */
public class TableLoad extends AbstractTransformTask implements TransformTask {
  private static final String XML = "XML";
  private static final String JSON = "JSON";
  private static final String CSV = "CSV";
  FrameReader reader = null;
  JdbcWriter writer = null;




  public String getTable() {
    if (configuration.containsIgnoreCase(ConfigTag.TABLE)) {
      return configuration.getString(ConfigTag.TABLE);
    }
    return null;
  }




  public String getSource() {
    if (configuration.containsIgnoreCase(ConfigTag.SOURCE)) {
      return configuration.getString(ConfigTag.SOURCE);
    }
    return null;
  }




  public String getFormat() {
    if (configuration.containsIgnoreCase(ConfigTag.FORMAT)) {
      return configuration.getString(ConfigTag.FORMAT);
    }
    return null;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (StringUtil.isBlank(getTable())) {
      throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.TABLE + " configuration parameter");
    }

    if (StringUtil.isBlank(getSource())) {
      throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.SOURCE + " configuration parameter");
    }

    // We can guess the type of file from its content so only check for validity if it exists 
    if (configuration.containsIgnoreCase(ConfigTag.FORMAT)) {
      if (StringUtil.isBlank(getString(ConfigTag.FORMAT))) {
        throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.FORMAT + " configuration parameter");
      } else {
        String format = getString(ConfigTag.FORMAT);
        if (!(format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("json") || format.equalsIgnoreCase("xml"))) {
          throw new ConfigurationException("Invalid argument for " + ConfigTag.FORMAT + " configuration parameter");
        }
      }
    }

  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    File sourceFile = new File(getSource());
    if (!sourceFile.isAbsolute()) {
      sourceFile = CDX.resolveFile(sourceFile, getContext());
    }
    Log.debug("Using an absolute source file of " + sourceFile.getAbsolutePath());

    String format = getFormat();
    if (StringUtil.isBlank(format)) {
      format = probeFile(sourceFile);
    }
    Log.debug("Using a format of " + format.toUpperCase());

    if (format.equalsIgnoreCase(CSV)) {
      reader = new CSVReader();
      super.getConfiguration().set(ConfigTag.HEADER, true);
    } else if (format.equalsIgnoreCase(JSON)) {
      reader = new JSONReader();
    } else {
      context.setError("Unsupported file format of '" + format + "'");
    }

    try {
      reader.setConfiguration(getConfiguration());
    } catch (ConfigurationException e) {
      context.setError("Error configuring reader: " + e.getMessage());
      Log.error("Problems configuring reader", e);
    }
    reader.open(context);

    writer = new JdbcWriter();
    try {
      writer.setConfiguration(getConfiguration());
      writer.setAutoAdjust(true);
      if (!getConfiguration().containsIgnoreCase(ConfigTag.BATCH)) {
        writer.setBatchSize(2); // set the default batch size to 100 inserts 
      }
    } catch (ConfigurationException e) {
      context.setError("Error configuring writer: " + e.getMessage());
      Log.error("Problems configuring writer", e);
    }
    writer.open(context);
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    super.performTask();

    TransactionContext context = new TransactionContext(getContext());
    while (!reader.eof()) {
      DataFrame frame = reader.read(context);
      if (frame != null) {
        writer.write(frame);
      }
    }
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#close()
   */
  @Override
  public void close() throws IOException {
    try {
      reader.close();
    } catch (Exception ignore) {
      // not interested
    }

    try {
      writer.close();
    } catch (Exception ignore) {
      // not interested
    }

    super.close();
  }




  private static String probeFile(File sourceFile) {
    String retval = CSV;
    if (sourceFile.exists() && sourceFile.canRead()) {
      try (FileInputStream fin = new FileInputStream(sourceFile); BufferedReader myInput = new BufferedReader(new InputStreamReader(fin))) {
        String line;
        while ((line = myInput.readLine()) != null) {
          line = line.replaceAll("\\s+", "");
          if (StringUtil.isNotEmpty(line)) {
            if (line.startsWith("<")) {
              retval = XML;
              break;
            } else if (line.startsWith("{") || line.startsWith("[")) {
              retval = JSON;
              break;
            } else {
              retval = CSV;
              break;
            }
          } // consume empty lines
        }
      } catch (final Exception ignore) {
        // this will be caught later
      }
    }
    return retval;
  }

}
