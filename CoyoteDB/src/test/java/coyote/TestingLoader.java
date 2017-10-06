/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dx.CDX;
import coyote.dx.TransformEngine;
import coyote.dx.TransformEngineFactory;
import coyote.loader.AbstractLoader;
import coyote.loader.ConfigTag;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This mimics a Job or Service loader in CoyoteDX.
 * 
 * <p>The intent is to use readers in Jobs to test the state of the context 
 * and exit in error if the environment is unacceptable.
 */
public class TestingLoader extends AbstractLoader {
  private TransformEngine engine = null;




  @Override
  public void configure(Config cfg) throws ConfigurationException {
    super.configure(cfg);
    List<Config> jobs = cfg.getSections(coyote.dx.ConfigTag.JOB);
    if (jobs.size() > 0) {
      Config job = jobs.get(0);
      if (StringUtil.isBlank(job.getName())) {
        if (StringUtil.isNotBlank(cfg.getName())) {
          job.setName(cfg.getName());
        } else {
          String cfguri = System.getProperty(ConfigTag.CONFIG_URI);
          if (StringUtil.isNotBlank(cfguri)) {
            try {
              job.setName(UriUtil.getBase(new URI(cfguri)));
            } catch (URISyntaxException ignore) {}
          }
        }
      }

      engine = TransformEngineFactory.getInstance(job);
      if (StringUtil.isBlank(engine.getName())) {
        Log.notice(LogMsg.createMsg(CDX.MSG, "Job.unnamed_engine_configured"));
      } else {
        Log.notice(LogMsg.createMsg(CDX.MSG, "Job.engine_configured", engine.getName()));
      }
    } else {
      Log.notice(LogMsg.createMsg(CDX.MSG, "Job.no_job_section"));
    }

  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {
    if (engine != null) {
      Log.info(LogMsg.createMsg(CDX.MSG, "Job.running", engine.getName(), engine.getClass().getSimpleName()));
      try {
        engine.run();
      } catch (final Exception e) {
        Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.exception_running_engine", e.getClass().getSimpleName(), e.getMessage(), engine.getName(), engine.getClass().getSimpleName()));
        Log.fatal(ExceptionUtil.toString(e));
        if (Log.isLogging(Log.DEBUG_EVENTS)) {
          Log.debug(ExceptionUtil.stackTrace(e));
        }
      } finally {
        try {
          engine.close();
        } catch (final IOException ignore) {}
        Log.info(LogMsg.createMsg(CDX.MSG, "Job.completed", engine.getName(), engine.getClass().getSimpleName()));
      }
    } else {
      Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.no_engine"));
    }
  }




  /**
   * @see coyote.loader.thread.ThreadJob#terminate()
   */
  @Override
  public void terminate() {
    Log.info("Terminating");
    super.terminate();
  }




  /**
   * @return the instance of the engine we just ran. 
   */
  public TransformEngine getEngine() {
    return engine;
  }

}
