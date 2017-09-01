/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformEngine;
import coyote.dx.TransformEngineFactory;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 *
 */
public class RunJob extends AbstractTransformTask {
  private static final String JSON_EXT = ".json";




  /**
   * Confirm the configuration URI
   *
   * @throws TaskException
   */
  private URI confirmConfigurationLocation(final String cfgLoc) throws TaskException {
    URI cfgUri = null;
    final StringBuffer errMsg = new StringBuffer(LogMsg.createMsg(CDX.MSG, "Task.runjob.confirming_cfg_location", cfgLoc) + StringUtil.CRLF);

    if (StringUtil.isNotBlank(cfgLoc)) {

      // create a URI out of it
      try {
        cfgUri = new URI(cfgLoc);
      } catch (final URISyntaxException e) {
        // This can happen when the location is a filename
      }

      // No URI implies a file
      if ((cfgUri == null) || StringUtil.isBlank(cfgUri.getScheme())) {

        final File localfile = new File(cfgLoc);

        if (!localfile.isAbsolute()) {
          cfgUri = checkCurrentDirectory(cfgLoc, errMsg);
          if (cfgUri == null) {
            cfgUri = checkHomeDirectory(cfgLoc, errMsg);
          }
          if (cfgUri == null) {
            cfgUri = checkHomeCfgDirectory(cfgLoc, errMsg);
          }
          if (cfgUri == null) {
            cfgUri = checkWorkDirectory(cfgLoc, errMsg);
          }
          if (cfgUri == null) {
            cfgUri = checkAppCfgDirectory(cfgLoc, errMsg);
          }
        }

        if (cfgUri == null) {
          errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
          if (haltOnError) {
            throw new TaskException(errMsg.toString());
          } else {
            Log.error(errMsg.toString());
          }
        } else {
          final File test = UriUtil.getFile(cfgUri);
          if (!test.exists() || !test.canRead()) {
            errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_file_not_readable", test.getAbsolutePath()) + StringUtil.CRLF);
            if (haltOnError) {
              throw new TaskException(errMsg.toString());
            } else {
              Log.error(errMsg.toString());
            }
          } else {
            Log.info(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_reading_from_file", test.getAbsolutePath()));
          }
        }
      } else {
        Log.info(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_reading_from_network"));
      }
    } else {
      System.err.println(LogMsg.createMsg(CDX.MSG, "Task.runjob.no_config_uri_defined"));
    }
    return cfgUri;
  }




  private URI checkAppCfgDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    URI retval = null;
    final String path = System.getProperties().getProperty(Loader.APP_HOME);
    if (StringUtil.isNotBlank(path)) {
      final String appDir = FileUtil.normalizePath(path);
      final File homeDir = new File(appDir);
      final File configDir = new File(homeDir, "cfg");
      if (configDir.exists()) {
        if (configDir.isDirectory()) {
          final File cfgFile = new File(configDir, cfgLoc);
          final File alternativeFile = new File(configDir, cfgLoc + JSON_EXT);

          if (cfgFile.exists()) {
            retval = FileUtil.getFileURI(cfgFile);
          } else {
            if (alternativeFile.exists()) {
              retval = FileUtil.getFileURI(alternativeFile);
            } else {
              errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.no_common_cfg_file", cfgFile.getAbsolutePath()) + StringUtil.CRLF);
              errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
            }
          }
        } else {
          errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_dir_is_not_directory", appDir) + StringUtil.CRLF);
        }
      } else {
        errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_dir_does_not_exist", appDir) + StringUtil.CRLF);
      }
    }
    return retval;
  }




  private URI checkWorkDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    URI retval = null;
    if (getContext() != null && getContext().getEngine() != null) {
      File wrkDir = getContext().getEngine().getWorkDirectory();
      if (wrkDir != null) {
        if (wrkDir.exists()) {
          if (wrkDir.isDirectory()) {
            final File cfgFile = new File(wrkDir, cfgLoc);
            final File alternativeFile = new File(wrkDir, cfgLoc + JSON_EXT);

            if (cfgFile.exists()) {
              retval = FileUtil.getFileURI(cfgFile);
            } else {
              if (alternativeFile.exists()) {
                retval = FileUtil.getFileURI(alternativeFile);
              } else {
                errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.no_work_dir_file", cfgFile.getAbsolutePath()) + StringUtil.CRLF);
                errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
              }
            }
          } else {
            errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.wrk_dir_is_not_directory", wrkDir) + StringUtil.CRLF);
          }
        } else {
          errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.wrk_dir_does_not_exist", wrkDir) + StringUtil.CRLF);
        }

      } else {
        errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.work_dir_not_set_in_engine") + StringUtil.CRLF);
      }
    } else {
      errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.no_reference_to_engine") + StringUtil.CRLF);
    }
    return retval;
  }




  private URI checkHomeCfgDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    return null;
  }




  private URI checkHomeDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    return null;
  }




  private URI checkCurrentDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    URI retval = null;
    File localfile = new File(cfgLoc);
    File alternativeFile = new File(cfgLoc + JSON_EXT);

    if (localfile.exists()) {
      retval = FileUtil.getFileURI(localfile);
    } else {
      if (alternativeFile.exists()) {
        retval = FileUtil.getFileURI(alternativeFile);
      } else {
        errMsg.append(LogMsg.createMsg(CDX.MSG, "Task.runjob.no_local_cfg_file", localfile.getAbsolutePath()) + StringUtil.CRLF);
      }
    }
    return retval;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    final String filename = getString(ConfigTag.FILE);
    Log.debug("Reading configuration file " + filename);
    final URI cfgUri = confirmConfigurationLocation(filename);
    Log.debug("Calculated URI of " + cfgUri);

    if (cfgUri != null) {
      try {
        final Config jobConfig = Config.read(cfgUri);
        if (StringUtil.isBlank(jobConfig.getName())) {
          jobConfig.setName(UriUtil.getBase(cfgUri));
        }

        final Config engineConfig = jobConfig.getSection(ConfigTag.JOB);

        final TransformEngine engine = TransformEngineFactory.getInstance(engineConfig.toString());

        final String jobName = getString(ConfigTag.NAME);
        if (StringUtil.isNotBlank(jobName)) {
          engine.setName(jobName);
        }

        String contextKey = getString(ConfigTag.CONTEXT);
        if (StringUtil.isBlank(contextKey)) {
          contextKey = engine.getName();
        }

        // Set the engine's work directory to this task's job directory  
        engine.setWorkDirectory(getJobDirectory());

        Config params = getConfiguration().getSection(ConfigTag.PARAMETERS);
        if (params != null) {
          TransformContext childContext = new TransformContext();
          for (DataField field : params.getFields()) {
            if (field.getType() == DataField.STRING) {
              
              // TODO: Parameters need to be resolved. They may represent context values or symbols
              
              // preprocess - so any unresolved variables will be resolved in 
              // the child job ;)
              String pval = Template.preProcess(field.getStringValue(), getContext().getSymbols());
              childContext.set(field.getName(), pval);
              Log.debug("Runjob setting parameter '" + field.getName() + "' to '" + pval + "'");
            } else {
              childContext.set(field.getName(), field.getObjectValue());
              Log.debug("Runjob setting parameter '" + field.getName() + "' to " + field.getStringValue());
            }
          }

          engine.setContext(childContext);
        }

        try {
          engine.run();
        } catch (final Throwable t) {
          final String errMsg = "Processing exception running Job: " + t.getMessage();
          if (haltOnError) {
            throw new TaskException(errMsg);
          } else {
            Log.error(errMsg);
            return;
          }
        } finally {
          try {
            engine.close();
          } catch (final Exception ignore) {}
          getContext().set(contextKey, engine.getContext().toMap());
        }
      } catch (IOException | ConfigurationException e) {
        final String errMsg = "Could not read configuration from " + cfgUri + " - " + e.getMessage();
        if (haltOnError) {
          throw new TaskException(errMsg);
        } else {
          Log.error(errMsg);
          return;
        }
      }
    } else {
      return;
    }
  }

}
