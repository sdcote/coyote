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

import coyote.commons.ExceptionUtil;
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
import coyote.dx.TransformTask;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This task runs a a data transfer job using the current context.
 * 
 * <p>Using this task, it is possible to run several jobs as one, each with a 
 * set of conditions determining if it should run. This give allow for more 
 * complex processing scenarios.
 * 
 * <p>This task can be configured thusly:<pre>
 * "RunJob" : { "file": "somejob.json", "name": "jobname" }</pre>
 * 
 * <p>The {@code file} parameter specifies the data transfer job configuration 
 * to run.
 * 
 * <p>The {@code name} parameter specifies the name to use for the job. This
 * allows for the publication of data in different locations than those 
 * specified in the configuration file or the default values.
 */
public class RunJob extends AbstractTransformTask implements TransformTask {
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
            Log.debug(LogMsg.createMsg(CDX.MSG, "Task.runjob.cfg_reading_from_file", test.getAbsolutePath()));
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

            if (cfgFile.exists() && !cfgFile.isDirectory()) {
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




  private URI checkCurrentDirectory(String cfgLoc, StringBuffer errMsg) throws TaskException {
    URI retval = null;
    File localfile = new File(cfgLoc);
    File alternativeFile = new File(cfgLoc + JSON_EXT);

    if (localfile.exists() && !localfile.isDirectory() ) {
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

        // if we have a name in our (RunJob) config, it overrides that in jobConfig file
        final String jobName = getString(ConfigTag.NAME);
        if (StringUtil.isNotBlank(jobName)) {
          engine.setName(jobName);
        }

        // If there was no name in the jobConfig or our (RunJob) config, set the name to the basename of the config file
        if (StringUtil.isBlank(engine.getName())) {
          engine.setName(FileUtil.getBase(cfgUri.toString()));
        }

        // place the jobs context in our context under the name of the job being run
        String contextKey = engine.getName();

        // Set the engine's work directory to this task's job directory  
        engine.setWorkDirectory(getJobDirectory());

        Config params = getConfiguration().getSection(ConfigTag.PARAMETERS);
        if (params != null) {
          Log.warn("The configuration section '" + ConfigTag.PARAMETERS + "' has been deprecated. use '" + ConfigTag.CONTEXT + "' instead");
        } else {
          params = getConfiguration().getSection(ConfigTag.CONTEXT);
        }

        if (params != null) {
          TransformContext childContext = engine.getContext();
          if (childContext == null) {
            childContext = new TransformContext();
          }

          for (DataField field : params.getFields()) {
            String parameterName = field.getName();
            if (field.getType() == DataField.STRING) {

              // Parameters need to be resolved. They may represent context values
              String parameterValue = field.getStringValue();

              // The value of the parameter may refer to context value 
              Object obj = getContext().resolveToValue(parameterValue);
              if (obj != null) {
                childContext.set(parameterName, obj);
                Log.debug("Runjob setting parameter '" + parameterName + "' to context reference " + obj.toString());
              } else {
                // perform a simple context resolve
                String resolvedValue = getContext().resolveToString(parameterValue);

                // If it did not result, it is probably a literal value
                if (resolvedValue == null) {
                  resolvedValue = parameterValue;
                }

                // preprocess - so any unresolved variables will be resolved in the child job
                String pval = Template.preProcess(resolvedValue, getContext().getSymbols());
                childContext.set(parameterName, pval);
                Log.debug("Runjob setting parameter '" + parameterName + "' to '" + pval + "'");
              }
            } else {
              childContext.set(parameterName, field.getObjectValue());
              Log.debug("Runjob setting parameter '" + parameterName + "' to " + field.getStringValue());
            }
          } // for each parameter

          engine.setContext(childContext);
        }

        try {
          engine.run();
        } catch (final Throwable t) {
          String errMsg = "Processing exception running Job: " + t.getMessage();
          if (t instanceof NullPointerException) {
            errMsg = errMsg.concat(ExceptionUtil.stackTrace(t));
          }
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
