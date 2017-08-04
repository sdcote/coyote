/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataFrameException;


/**
 * FileAppender is an implementation of Logger that extends LoggerBase and defines
 * event() to write the event to a Writer.
 */
public class FileAppender extends AbstractLogger {

  public static final String APPEND_TAG = "append";

  protected Writer log_writer;
  protected File targetFile = null;
  protected long nextCycle = Long.MAX_VALUE;
  protected boolean append = true;




  /**
   * Construct a LoggerBase with an initial mask value of zero (i.e. does not log
   * any events).
   */
  public FileAppender() {
    super(0);
  }




  /**
   * Construct a LoggerBase that writes (appends) to the specified File with an 
   * initial mask value of zero (i.e. does not log any events).
   *
   * @param file The file.
   */
  public FileAppender(final File file) {
    this(file, 0, true);
  }




  /**
   * Construct a LoggerBase that writes to the specified File with an initial 
   * mask value of zero (i.e. does not log any events) and either appends or 
   * overwrites the file.
   *
   * @param file The file.
   * @param appendflag true to append data to the file, false to overwrite
   */
  public FileAppender(final File file, final boolean appendflag) {
    this(file, 0, appendflag);
  }




  /**
   * Construct a LoggerBase that writes to the specified File with an initial 
   * mask value and either appends or overwrites the file.
   *
   * @param file The file.
   * @param mask The initial mask value.
   * @param appendflag true to append data to the file, false to overwrite
   */
  public FileAppender(final File file, final long mask, boolean appendflag) {
    super(mask);
    append = appendflag;

    try {
      if (log_writer == null) {
        targetFile = file;
        log_writer = new OutputStreamWriter(new FileOutputStream(file.toString(), append));

        final byte[] header = getFormatter().initialize();

        if (header != null) {
          log_writer.write(new String(header));
        }

        initialized = true;
      }
    } catch (final Exception e) {
      System.err.println("Could not attach logger to file \"" + file.getAbsolutePath() + "\". Reason:\"" + e.getMessage() + "\".");

      log_writer = null;
    }
  }




  /**
   * If enabled, log an event of the specified category to the underlying
   * Writer.
   *
   * @param category The category.
   * @param event The event.
   * @param cause The exception that caused the log entry. Can be null.
   */
  public void append(final String category, final Object event, final Throwable cause) {
    if (!targetFile.exists()) {
      try {
        log_writer = new OutputStreamWriter(new FileOutputStream(targetFile.toString(), append));

        final byte[] header = getFormatter().initialize();

        if (header != null) {
          log_writer.write(new String(header));
        }
      } catch (final Exception ex) {
        System.err.println("Could not recreate " + targetFile.getAbsolutePath() + " - " + ex.getMessage());
        if (log_writer != null) {
          try {
            log_writer.close();
          } catch (final Exception ignore) {}
        }

        log_writer = null;
      }
    }

    if (log_writer != null) {

      try {
        synchronized (formatter) {
          log_writer.write(formatter.format(event, category, cause));
          log_writer.flush();
        }
      } catch (final IOException ioe) {
        // normal during shutdown sequences - but what about other times?
        // maybe we should consider refactoring this
      } catch (final Exception e) {
        System.err.println(this.getClass().getName() + " formatting error: " + e + ":" + e.getMessage() + StringUtil.LINE_FEED + ExceptionUtil.stackTrace(e));
      }
    }
  }




  /**
   * Return the writer.
   *
   * @return the writer used by this logger
   */
  public Writer getWriter() {
    return log_writer;
  }




  /**
   * Initialize the logger.
   * 
   * @see coyote.loader.log.AbstractLogger#initialize()
   */
  public void initialize() {
    if (!initialized) {
      // have the logger init the target and categories from properties for us
      super.initialize();

      // see if the configuration contains the append flag
      if (config.contains(FileAppender.APPEND_TAG)) {
        try {
          append = config.getAsBoolean(FileAppender.APPEND_TAG);
        } catch (DataFrameException ignore) {}
      }

      // check to see if we are enabled, if so, then prepare the log writer
      if (getMask() != 0) {
        prepareWriter();
      }
    }
  }




  /**
   * Overrides the enablement of this logger by first ensuring the log_writer 
   * is created before restoring the log mask.
   */
  @Override
  public synchronized void enable() {

    if (log_writer == null) {
      prepareWriter();
    }
    super.enable();
  }




  private void prepareWriter() {
    try {
      if ((target != null) && UriUtil.isFile(target)) {
        // Make sure we have a complete path to the target file
        File dest = new File(UriUtil.getFilePath(target));

        if (!dest.isAbsolute()) {
          dest = new File(System.getProperty("user.dir"), UriUtil.getFilePath(target));
        }

        dest.getParentFile().mkdirs();

        targetFile = dest;

        // Create the writer
        log_writer = new OutputStreamWriter(new FileOutputStream(targetFile.toString(), append));

        final byte[] header = getFormatter().initialize();

        if (header != null) {
          log_writer.write(new String(header));
        }

        initialized = true;
      } else {
        throw new Exception("URI schema '" + target.getScheme() + "' does not specify a file");
      }
    } catch (final Exception e) {
      System.err.println("Log Initialization Error: " + getClass().getName() + " could not attach logger to target '" + target + "'. Reason: \"" + e.getMessage() + "\".");
      e.printStackTrace();

      log_writer = null;
      targetFile = null;

      disable();
    }

  }




  /**
   * Set the writer.
   *
   * @param writer The new writer.
   */
  public void setWriter(final Writer writer) {
    log_writer = writer;
  }




  /**
   * Terminate the logger.
   * 
   * @see coyote.loader.log.Logger#terminate()
   */
  public void terminate() {
    try {
      final byte[] footer = getFormatter().terminate();

      if (footer != null) {
        log_writer.write(new String(footer));
      }

      log_writer.flush();
      log_writer.close();
    } catch (final Exception e) {}
    finally {
      initialized = false;
    }
  }




  /**
   * @return the append
   */
  public boolean isAppending() {
    return append;
  }




  /**
   * @param flag true to append log entries to the file, false to overwrite the file
   */
  public void setAppending(boolean flag) {
    append = flag;
  }
}