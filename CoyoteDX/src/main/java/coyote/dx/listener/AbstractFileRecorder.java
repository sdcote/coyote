/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Base class for context listeners that want to record data to a file.
 * 
 * <p>Note: this is not logging, but a way to write strings to a file.</p>
 */
public abstract class AbstractFileRecorder extends AbstractContextRecorder {

  private static final String STDOUT = "stdout";

  private static final String STDERR = "stderr";

  protected Writer log_writer;

  protected File targetFile = null;




  /**
   * @see coyote.dx.listener.AbstractListener#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String target = getString(ConfigTag.TARGET);
    setTarget(target);
    Log.debug(LogMsg.createMsg(CDX.MSG, "DX.listener_validating_target", getTarget()));

    if (StringUtil.isNotBlank(getTarget())) {

      target = getTarget().trim();

      if (STDOUT.equalsIgnoreCase(target)) {
        log_writer = new BufferedWriter(new OutputStreamWriter(System.out));
      } else if (STDERR.equalsIgnoreCase(target)) {
        log_writer = new BufferedWriter(new OutputStreamWriter(System.err));
      } else {
        // Try to parse the target as a URI, failures result in a null
        if (UriUtil.parse(target) == null) {
          // Windows systems often have a drive letter in fully qualified filenames
          if (target.charAt(1) == ':') {
            // convert it to a file URI
            File f = new File(target);
            URI u = f.toURI();
            setTarget(u.toString());
          }
        }

        try {
          URI uri = new URI(getTarget());

          // Make sure we have a complete path to the target file
          File dest = new File(UriUtil.getFilePath(uri));

          // if not absolute, use the current working directory
          if (!dest.isAbsolute()) {
            dest = new File(getJobDirectory(), UriUtil.getFilePath(uri));
          }

          // make any directories as necessary
          dest.getParentFile().mkdirs();

          // 
          targetFile = dest;
          Log.debug(LogMsg.createMsg(CDX.MSG, "DX.listener_using_target", targetFile.toString()));

          // Create the writer
          log_writer = new OutputStreamWriter(new FileOutputStream(targetFile.toString(), false));

        } catch (final URISyntaxException e) {
          context.setError("Invalid target URI (" + e.getMessage() + ") - '" + getTarget() + "'");
        } catch (FileNotFoundException e) {
          context.setError("Could not write to target URI (" + e.getMessage() + ") - '" + getTarget() + "'");
        } catch (Exception e) {
          context.setError("Processing exception (" + e.getMessage() + ") during open ");
          e.printStackTrace();
        }
      }

    } else {
      Log.error("No target specified");
      context.setError(getClass().getName() + " could not determine 'target' for recording events");
    }

  }




  protected void write(String text) {
    if (targetFile != null && !targetFile.exists()) {
      try {
        log_writer = new OutputStreamWriter(new FileOutputStream(targetFile.toString(), false));
      } catch (final Exception ex) {
        System.err.println("Could not recreate " + targetFile.getAbsolutePath() + " - " + ex.getMessage());
        if (log_writer != null) {
          try {
            log_writer.close();
          } catch (final Exception ignore) {} finally {
            log_writer = null;
          }
        }
      }
    }

    if (log_writer == null) {
      return;
    }

    try {
      log_writer.write(text);
      log_writer.flush();
    } catch (final IOException ioe) {} catch (final Exception e) {
      context.setError(this.getClass().getName() + " context logging error: " + e + ":" + e.getMessage());
    }
  }




  /**
   * @see coyote.dx.listener.AbstractListener#close()
   */
  @Override
  public void close() throws IOException {

    if (log_writer != null) {
      try {
        log_writer.close();
      } catch (final Exception ignore) {} finally {
        log_writer = null;
      }
    }

    super.close();
  }




  /**
   * @return the target file for this listener
   */
  public File getTargetFile() {
    return targetFile;
  }

}
