/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataFrameException;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Base class for all frame writers writing frames to files
 */
public abstract class AbstractFrameFileWriter extends AbstractFrameWriter implements FrameWriter {

  protected static final String STDOUT = "STDOUT";
  protected static final String STDERR = "STDERR";
  protected int rowNumber = 0;
  protected PrintWriter printwriter = null;




  /**
   * @see coyote.dx.ConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    // Check if we are to append data to existing files
    if (cfg.contains(ConfigTag.APPEND)) {
      try {
        setAppendFlag(cfg.getAsBoolean(ConfigTag.APPEND));
      } catch (final DataFrameException e) {
        Log.info(LogMsg.createMsg(CDX.MSG, "Writer.append_flag_is_not_valid " + cfg.getAsString(ConfigTag.APPEND)));
      }
    }
    Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.append_flag_is_set_as", isAppending()));
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    if (printwriter != null) {
      try {
        printwriter.flush();
        printwriter.close();
      } catch (Exception e) {
        Log.debug("Exception closing writer:" + e.getMessage());
      } finally {
        printwriter = null;
      }
    }
  }




  /**
   * @return the print writer used for output
   */
  public PrintWriter getPrintwriter() {
    return printwriter;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.open(context);

    // if we don't already have a printwriter, set one up based on the configuration
    if (printwriter == null) {
      // check for a target in our configuration
      final String target = getString(ConfigTag.TARGET);
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", getClass().getSimpleName(), target));

      // Make sure we have a target
      if (StringUtil.isNotBlank(target)) {

        // Try to parse the target as a URI, failures result in a null
        final URI uri = UriUtil.parse(target);

        File targetFile = null;

        // Check to see if it is STDOUT or STDERR
        if (StringUtil.equalsIgnoreCase(STDOUT, target)) {
          printwriter = new PrintWriter(System.out);
        } else if (StringUtil.equalsIgnoreCase(STDERR, target)) {
          printwriter = new PrintWriter(System.err);
        } else if (uri != null) {
          if (UriUtil.isFile(uri)) {
            targetFile = UriUtil.getFile(uri);

            if (targetFile == null) {
              Log.warn(LogMsg.createMsg(CDX.MSG, "Writer.The target '{%s}' does not represent a file", target));
            }
          } else {
            // if all we have is a filename, there is not scheme to check...
            // check that there is a scheme, if not then assume a filename!
            if (uri.getScheme() == null) {
              targetFile = new File(target);
            }
          }
        } else {
          targetFile = new File(target);
        }

        // if not absolute, use the current job directory
        if (!targetFile.isAbsolute()) {
          targetFile = new File(getJobDirectory(), targetFile.getPath());
        }
        Log.debug("Using a target file of " + targetFile.getAbsolutePath());

        try {
          final Writer fwriter = new FileWriter(targetFile, isAppending());
          printwriter = new PrintWriter(fwriter, isAppending());
        } catch (final Exception e) {
          Log.error("Could not create writer: " + e.getMessage());
          context.setError(e.getMessage());
        }

      } else {
        Log.error("No target specified");
        context.setError(getClass().getName() + " could not determine target");
      }
    }

  }




  /**
   * Set the PrintWriter used for output
   * 
   * @param writer the print writer to set
   */
  public void setPrintwriter(final PrintWriter writer) {
    printwriter = writer;
  }




  /**
   * Set whether or not the writer should append output to existing files.
   * 
   * @param flag true to instruct the writer to append data to files, false to overwrite existing data.
   */
  public void setAppendFlag(final boolean flag) {
    configuration.put(ConfigTag.APPEND, flag);
  }




  /**
   * @return true indicates the writer will append data to existing files, false to overwrite existing data.
   */
  public boolean isAppending() {
    try {
      return configuration.getAsBoolean(ConfigTag.APPEND);
    } catch (final DataFrameException e) {
      return false;
    }
  }

}
