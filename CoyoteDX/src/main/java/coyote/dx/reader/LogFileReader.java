/*
 * Copyright (c) 2020 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.log.LogEntryMapper;
import coyote.commons.log.ParsingMode;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;

import java.io.IOException;


/**
 * Text reader that parses each line into tokens and maps those tokens to fields based on the format and parsing mode.
 */
public class LogFileReader extends AbstractFrameStreamFileReader implements FrameReader, ConfigurableComponent {
  private LogEntryMapper mapper = null;


  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    String mode = getString(ConfigTag.MODE);
    if (StringUtil.isNotBlank(mode)) {
      ParsingMode parsingMode = ParsingMode.getModeByName(mode);
      if (parsingMode == null) {
        String msg = "Invalid log parsing mode of '" + mode + "'";
        Log.error(msg);
        context.setError(msg);
      }
    } else {
      String msg = "No log parsing mode specified";
      Log.error(msg);
      context.setError(msg);
    }

    String format = getString(ConfigTag.FORMAT);
    if (StringUtil.isBlank(format)) {
      String msg = "No log parsing format specified";
      Log.error(msg);
      context.setError(msg);
    }

  }


  /**
   * @see FrameReader#read(TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = null;

    String logLine = null;
    try {
      logLine = reader.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (StringUtil.isNotBlank(logLine)) {
      retval = mapper.mapToFrame(logLine);
    }


    return retval;
  }


  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    if (reader != null) {
      reader.close();
    }
  }


  /**
   * @see coyote.dx.Component#open(TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    try {
      mapper = new LogEntryMapper(getString(ConfigTag.MODE), getString(ConfigTag.FORMAT));
    } catch (Exception e) {
      e.printStackTrace();
    }


  }


}
