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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.dx.db.Database;
import coyote.dx.db.DatabaseConnector;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This runs a set of SQL commands from a file against a database connection.
 *
 * <p>With this task, it is possible to execute a SQL script against a JDBC
 * connection to perform complex database processing. The goal is to fully
 * customize a relational database for subsequent use by other components in
 * the job.
 */
public class RunSql extends AbstractTransformTask implements TransformTask {

  /** The thing we use to get connections to the database */
  private DatabaseConnector connector = null;

  /** The JDBC connection used by this writer to interact with the database */
  private Connection connection;

  /** The number of records we should batch before executing an UPDATE */
  private int batchsize = 0;

  /** Holds the current state of the command being built. */
  private final StringBuffer buffer = new StringBuffer();

  private BufferedReader bufferedReader = null;
  private int linePointer = 0;
  private boolean hasNext = true;
  private int lineToSkip = 0;
  private boolean linesSkipped = false;




  /**
   * @see coyote.dx.task.AbstractTransformTask#close()
   */
  @Override
  public void close() throws IOException {
    closeQuietly(bufferedReader);
    super.close();
  }




  /**
   * Close the given statement and consume any thrown exceptions.
   *
   * @param reader the resource to close.
   */
  private void closeQuietly(final Reader reader) {
    if (reader != null) {
      try {
        reader.close();
      } catch (final Exception ignore) {
        // no exceptions
      }
    }
  }




  public int getBatchSize() {
    try {
      return configuration.getAsInt(ConfigTag.BATCH);
    } catch (final DataFrameException ignore) {}
    return 0;
  }




  /**
   * Return the connector we use for
   * @return the connector
   */
  public DatabaseConnector getConnector() {
    return connector;
  }




  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   *
   * @throws IOException if the next line could not be read
   */
  private String getNextLine() throws IOException {
    if (!linesSkipped) {
      for (int i = 0; i < lineToSkip; i++) {
        bufferedReader.readLine();
        linePointer++;
      }
      linesSkipped = true;
    }
    final String nextLine = bufferedReader.readLine();
    linePointer++;
    if (nextLine == null) {
      hasNext = false;
    }

    return hasNext ? nextLine : null;
  }




  public String getSource() {
    if (configuration.containsIgnoreCase(ConfigTag.SOURCE)) {
      return configuration.getString(ConfigTag.SOURCE);
    }
    return null;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.open(context);

    File sourceFile = new File(getSource());
    if (!sourceFile.isAbsolute()) {
      sourceFile = CDX.resolveFile(sourceFile, getContext());
    }
    Log.debug("Using an absolute source file of " + sourceFile.getAbsolutePath());

    if (sourceFile.exists() && sourceFile.canRead()) {
      try {
        bufferedReader = new BufferedReader(new FileReader(sourceFile));

      } catch (final Exception e) {
        Log.error("Could not create reader: " + e.getMessage());
        context.setError(e.getMessage());
      }
    } else {
      context.setError(LogMsg.createMsg(CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), sourceFile.getAbsolutePath()).toString());
    }

    // If we don't have a connection, prepare to create one
    if (connection == null) {

      // Look for a database connector in the context bound with the name specified in the TARGET attribute
      String target = getConfiguration().getString(ConfigTag.TARGET);
      target = Template.preProcess(target, context.getSymbols());
      final Object obj = getContext().get(target);
      if (obj != null && obj instanceof DatabaseConnector) {
        setConnector((DatabaseConnector)obj);
        Log.debug("Using database connector found in context bound to '" + target + "'");
      }

      if (getConnector() == null) {
        // we have to create a Database based on our configuration
        final Database database = new Database();
        final Config cfg = new Config();

        if (StringUtil.isNotBlank(getString(ConfigTag.TARGET))) {
          cfg.put(ConfigTag.TARGET, getString(ConfigTag.TARGET));
        }

        if (StringUtil.isNotBlank(getString(ConfigTag.DRIVER))) {
          cfg.put(ConfigTag.DRIVER, getString(ConfigTag.DRIVER));
        }

        if (StringUtil.isNotBlank(getString(ConfigTag.LIBRARY))) {
          cfg.put(ConfigTag.LIBRARY, getString(ConfigTag.LIBRARY));
        }

        if (StringUtil.isNotBlank(getString(ConfigTag.USERNAME))) {
          cfg.put(ConfigTag.USERNAME, getString(ConfigTag.USERNAME));
        }

        if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME))) {
          cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME, getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
        }

        if (StringUtil.isNotBlank(getString(ConfigTag.PASSWORD))) {
          cfg.put(ConfigTag.PASSWORD, getString(ConfigTag.PASSWORD));
        }

        if (StringUtil.isNotBlank(getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD))) {
          cfg.put(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD, getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
        }

        setConnector(database);

        try {
          database.setConfiguration(cfg);
          if (Log.isLogging(Log.DEBUG_EVENTS)) {
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_target", getClass().getSimpleName(), database.getTarget()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_driver", getClass().getSimpleName(), database.getDriver()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_library", getClass().getSimpleName(), database.getLibrary()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_user", getClass().getSimpleName(), database.getUserName()));
            Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_password", getClass().getSimpleName(), StringUtil.isBlank(database.getPassword()) ? 0 : database.getPassword().length()));
          }
        } catch (final ConfigurationException e) {
          context.setError("Could not configure database connector: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
      }
    } else {
      Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_existing_connection", getClass().getSimpleName()));
    }

    setBatchSize(getInteger(ConfigTag.BATCH));
    Log.debug(LogMsg.createMsg(CDX.MSG, "Component.using_batch_size", getClass().getSimpleName(), getBatchSize()));

    // validate and cache our batch size
    if (getBatchSize() < 1) {
      batchsize = 0;
    } else {
      batchsize = getBatchSize();
    }
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    do {
      try {
        String nextLine = getNextLine();
        if (StringUtil.isNotBlank(nextLine)) {
          nextLine = nextLine.trim();

          // Handle Multi-line comments /* this is commented out */
          // Handle EOL comments  -- this is commented out
          // Use StringParser to parse comments out of the file
          // SqlReader extends StringParser
          // parser.getNextCommand()

          int delimiter = nextLine.indexOf(';');
          if (delimiter >= 0) {
            buffer.append(' ');
            buffer.append(nextLine.substring(0, delimiter + 1));
            processCommand(buffer.toString().trim(), linePointer);
            buffer.delete(0, buffer.length());
            buffer.append(nextLine.substring(delimiter + 1));
          } else {
            buffer.append(nextLine);
          }
        }
      } catch (final IOException e) {
        throw new TaskException("read error", e);
      }
    }
    while (hasNext);
  }




  private void processCommand(String command, int index) {
    System.out.println(index + ":" + command);
  }




  /**
   * @param value
   */
  public void setBatchSize(final int value) {
    batchsize = value;
    configuration.put(ConfigTag.BATCH, value);
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(final Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    if (StringUtil.isBlank(getSource())) {
      throw new ConfigurationException("Null, empty or blank argument for " + ConfigTag.SOURCE + " configuration parameter");
    }

  }




  /**
   * @param connector the connector to set
   */
  public void setConnector(final DatabaseConnector connector) {
    this.connector = connector;
  }




  /**
   * @param value
   */
  public void setSource(final String value) {
    configuration.put(ConfigTag.SOURCE, value);
  }

}
