package coyote.dx.listener;

import coyote.commons.ExceptionUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.*;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This listener will execute the named job when after all the fields of the working frame has been mapped to the target frame.
 * <p>This listener runs differently than that of the task with the same name. All jobs will share the transform
 * context of this listener. This allows each run to share data with its ancestors. This also means there are no
 * context items to define in the configuration. If a context item is required for the job to run, it must be placed
 * there by a task or other component.</p>
 *
 * <p>Additionally, all records will unconditionally run the job. If there is a reason not to run the job, the record
 * must be filtered out before the mapping phase.</p>
 */
public class RunJob extends AbstractListener implements ContextListener {

    /**
     * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
     */
    @Override
    public void setConfiguration(Config cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
    }


    /**
     * @see coyote.dx.context.ContextListener#onMap(coyote.dx.context.TransactionContext)
     */
    @Override
    public void onMap(TransactionContext txnContext) {
        final String filename = getString(ConfigTag.FILE);
        Log.debug("Reading job configuration file " + filename);
        final URI cfgUri = confirmConfigurationLocation(filename);
        Log.debug("Calculated job URI of " + cfgUri);

        if (cfgUri != null) {
            try {
                final Config jobConfig = Config.read(cfgUri);
                final Config engineConfig = jobConfig.getSection(ConfigTag.JOB);
                final TransformEngine engine = TransformEngineFactory.getInstance(engineConfig.toString());

                // If there was no name in the jobConfig or our (RunJob) config, set the name to the basename of the config file
                if (StringUtil.isBlank(engine.getName())) {
                    engine.setName(FileUtil.getBase(cfgUri.toString()));
                }

                // Set the engine's work directory to this listeners job directory parent...they are peers not children
                engine.setWorkDirectory(getJobDirectory().getParentFile());

                // copy the target frame fields to the context symbol table so templates will resolve the target fields
                DataFrame target = getContext().getTransaction().getTargetFrame();
                if(target != null){
                    for(DataField field: target.getFields()){
                        if( StringUtil.isNotBlank(field.getName())){
                            getContext().getSymbols().put(field.getName(),field.getObjectValue());
                        }
                    }
                } else {
                    Log.error("Target frame not found in the transaction context.");
                }

                // Merge this listeners transform context symbol table to the engine's transform context symbol table
                // so templates will resolve with the current set of symbols that include the target frame fields.
                engine.getSymbolTable().merge(getContext().getSymbols());

                try {
                    engine.run();
                } catch (NullPointerException npe) {
                    String errMsg = "Processing exception (NPE) running Job: " + npe.getMessage();
                    errMsg = errMsg.concat(ExceptionUtil.stackTrace(npe));
                    Log.error(errMsg);
                } catch (final Throwable t) {
                    Log.error("Processing exception running Job: " + t.getMessage());
                } finally {
                    try {
                        engine.close();
                    } catch (final Exception ignore) {
                        Log.debug("Problems closing engine", ignore);
                    }
                }
            } catch (IOException | ConfigurationException e) {
                final String errMsg = "Could not read configuration from " + cfgUri + " - " + e.getMessage();
                Log.error(errMsg);
            }
        }
    }


    /**
     * Confirm the configuration URI
     *
     * @throws TaskException
     */
    private URI confirmConfigurationLocation(final String cfgLoc) {
        URI cfgUri = null;
        final StringBuffer errMsg = new StringBuffer(LogMsg.createMsg(CDX.MSG, "Listener.runjob.confirming_cfg_location", cfgLoc) + StringUtil.CRLF);

        if (StringUtil.isNotBlank(cfgLoc)) {

            // create a URI out of it
            try {
                cfgUri = new URI(cfgLoc);
            } catch (final URISyntaxException e) {
                // This can happen when the location is a filename
            }

            // No URI implies a file
            if ((cfgUri == null) || StringUtil.isBlank(cfgUri.getScheme())) {
                final File cfgFile = new File(cfgLoc);
                if (!cfgFile.isAbsolute()) {
                    cfgUri = checkCurrentDirectory(cfgLoc, errMsg);
                    if (cfgUri == null) {
                        cfgUri = checkWorkDirectory(cfgLoc, errMsg);
                    }
                    if (cfgUri == null) {
                        cfgUri = checkAppCfgDirectory(cfgLoc, errMsg);
                    }
                }

                if (cfgUri == null) {
                    errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
                    Log.error(errMsg.toString());
                } else {
                    final File test = UriUtil.getFile(cfgUri);
                    if (!test.exists() || !test.canRead()) {
                        errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_file_not_readable", test.getAbsolutePath()) + StringUtil.CRLF);
                        Log.error(errMsg.toString());
                    } else {
                        Log.debug(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_reading_from_file", test.getAbsolutePath()));
                    }
                }
            } else {
                Log.info(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_reading_from_network"));
            }
        } else {
            System.err.println(LogMsg.createMsg(CDX.MSG, "Listener.runjob.no_config_uri_defined"));
        }
        return cfgUri;
    }


    private URI checkAppCfgDirectory(String cfgLoc, StringBuffer errMsg) {
        URI retval = null;
        final String path = System.getProperties().getProperty(Loader.APP_HOME);
        if (StringUtil.isNotBlank(path)) {
            final String appDir = FileUtil.normalizePath(path);
            final File homeDir = new File(appDir);
            final File configDir = new File(homeDir, "cfg");
            if (configDir.exists()) {
                if (configDir.isDirectory()) {
                    final File cfgFile = new File(configDir, cfgLoc);
                    final File alternativeFile = new File(configDir, cfgLoc + CDX.JSON_EXT);

                    if (cfgFile.exists()) {
                        retval = FileUtil.getFileURI(cfgFile);
                    } else {
                        if (alternativeFile.exists()) {
                            retval = FileUtil.getFileURI(alternativeFile);
                        } else {
                            errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.no_common_cfg_file", cfgFile.getAbsolutePath()) + StringUtil.CRLF);
                            errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
                        }
                    }
                } else {
                    errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_dir_is_not_directory", appDir) + StringUtil.CRLF);
                }
            } else {
                errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_dir_does_not_exist", appDir) + StringUtil.CRLF);
            }
        }
        return retval;
    }


    private URI checkWorkDirectory(String cfgLoc, StringBuffer errMsg) {
        URI retval = null;
        if (getContext() != null && getContext().getEngine() != null) {
            File wrkDir = getContext().getEngine().getWorkDirectory();
            if (wrkDir != null) {
                if (wrkDir.exists()) {
                    if (wrkDir.isDirectory()) {
                        final File cfgFile = new File(wrkDir, cfgLoc);
                        final File alternativeFile = new File(wrkDir, cfgLoc + CDX.JSON_EXT);

                        if (cfgFile.exists() && !cfgFile.isDirectory()) {
                            retval = FileUtil.getFileURI(cfgFile);
                        } else {
                            if (alternativeFile.exists()) {
                                retval = FileUtil.getFileURI(alternativeFile);
                            } else {
                                errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.no_work_dir_file", cfgFile.getAbsolutePath()) + StringUtil.CRLF);
                                errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
                            }
                        }
                    } else {
                        errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.wrk_dir_is_not_directory", wrkDir) + StringUtil.CRLF);
                    }
                } else {
                    errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.wrk_dir_does_not_exist", wrkDir) + StringUtil.CRLF);
                }

            } else {
                errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.work_dir_not_set_in_engine") + StringUtil.CRLF);
            }
        } else {
            errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.no_reference_to_engine") + StringUtil.CRLF);
        }
        return retval;
    }


    private URI checkCurrentDirectory(String cfgLoc, StringBuffer errMsg) {
        URI retval = null;
        File localfile = new File(cfgLoc);
        File alternativeFile = new File(cfgLoc + CDX.JSON_EXT);

        if (localfile.exists() && !localfile.isDirectory()) {
            retval = FileUtil.getFileURI(localfile);
        } else {
            if (alternativeFile.exists()) {
                retval = FileUtil.getFileURI(alternativeFile);
            } else {
                errMsg.append(LogMsg.createMsg(CDX.MSG, "Listener.runjob.no_local_cfg_file", localfile.getAbsolutePath()) + StringUtil.CRLF);
            }
        }
        return retval;
    }


}
