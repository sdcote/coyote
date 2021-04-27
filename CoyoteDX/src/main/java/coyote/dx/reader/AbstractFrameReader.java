/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.io.IOException;

import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public abstract class AbstractFrameReader extends AbstractConfigurableComponent implements FrameReader, ConfigurableComponent {
  protected long readLimit = 0;
  protected volatile long recordCounter = 0;




  /**
   * @return true of the reader has a limit on the number of records it reads, 
   *         false otherwise
   */
  protected boolean isLimitingReads() {
    return readLimit > 0;
  }




  /**
   * @return the number of reads to which the reader is limited.
   */
  protected long getReadLimit() {
    return readLimit;
  }




  /**
   * @param limit the number of reads to which the reader is limited.
   */
  protected void setReadLimit(long limit) {
    if (limit < 0) {
      readLimit = 0;
    } else {
      readLimit = limit;
    }
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(final TransformContext context) {
    super.context = context;
    if (getConfiguration().containsIgnoreCase(ConfigTag.LIMIT)) {
      readLimit = getLong(ConfigTag.LIMIT);
    }
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.limit_is", readLimit));
  }




  /**
   * @see coyote.dx.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // no-op implementation
  }




  /**
   * @return the source URI from which the reader will read
   */
  public String getSource() {
    return configuration.getAsString(ConfigTag.SOURCE);
  }

}
