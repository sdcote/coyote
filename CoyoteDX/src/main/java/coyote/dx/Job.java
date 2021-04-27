package coyote.dx;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.loader.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This class represents the DX job to execute.
 * 
 * <p>The working directory is determined based on the setting of the 
 * DX.home system property which causes all relative file operations to 
 * resolve to the DX.home/wrk/&lt;job.name&gt;/ directory. If the DX.home 
 * property is not set, then the Job attempts to locate the file used to 
 * configure it and uses that directory as the working directory.</p>
 */
public class Job extends AbstractBatchLoader implements Loader {

  TransformEngine engine = null;
  boolean repeat = false;




  /**
   * @see coyote.loader.AbstractLoader#configure(coyote.loader.cfg.Config)
   */
  @Override
  public void configure(Config cfg) throws ConfigurationException {
    super.configure(cfg);

    // Support the concept of an ever-repeating job
    try {
      repeat = configuration.getBoolean(ConfigTag.REPEAT);
    } catch (NumberFormatException ignore) {
      // probably does not exist
    }

    // calculate and normalize the appropriate value for "app.home"
    determineHomeDirectory();

    determineWorkDirectory();

    List<Config> jobs = cfg.getSections(coyote.dx.ConfigTag.JOB);

    if (jobs.size() > 0) {

      Config job = jobs.get(0);

      // If the job has no name... 
      if (StringUtil.isBlank(job.getName())) {
        if (StringUtil.isNotBlank(cfg.getName())) {
          //...set it to the name of the parent...
          job.setName(cfg.getName());
        } else {
          //...or the base of the configuration URI
          String cfguri = System.getProperty(ConfigTag.CONFIG_URI);
          if (StringUtil.isNotBlank(cfguri)) {
            try {
              job.setName(UriUtil.getBase(new URI(cfguri)));
            } catch (URISyntaxException ignore) {
              // well, we tried, it will probably get assigned a UUID later
            }
          }
        }
      }

      // have the Engine Factory create a transformation engine based on the
      // configuration 
      engine = TransformEngineFactory.getInstance(job);

      if (getLoader() != null) {
        engine.setLoader(getLoader()); 
      } else {
        engine.setLoader(this);
      }

      // store the command line arguments in the symbol table of the engine
      for (int x = 0; x < commandLineArguments.length; x++) {
        engine.getSymbolTable().put(Symbols.COMMAND_LINE_ARG_PREFIX + x, commandLineArguments[x]);
      }

      // store environment variables in the symbol table
      Map<String, String> env = System.getenv();
      for (String envName : env.keySet()) {
        engine.getSymbolTable().put(Symbols.ENVIRONMENT_VAR_PREFIX + envName, env.get(envName));
      }

      if (StringUtil.isBlank(engine.getName())) {
        Log.trace(LogMsg.createMsg(CDX.MSG, "Job.unnamed_engine_configured"));
      } else {
        Log.trace(LogMsg.createMsg(CDX.MSG, "Job.engine_configured", engine.getName()));
      }
    } else {
      Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.no_job_section"));
    }
  }




  /**
   * @see coyote.loader.AbstractLoader#start()
   */
  @Override
  public void start() {

    if (engine != null) {
      Log.trace(LogMsg.createMsg(CDX.MSG, "Job.running", engine.getName(), engine.getClass().getSimpleName()));

      // run the transformation
      // Note that depending on the configuration, this could be placed in the 
      // scheduler and run intermittently as a scheduled job or multiple 
      // transform engines could be run in the thread pool of the super-class.
      do {
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
          Log.trace(LogMsg.createMsg(CDX.MSG, "Job.completed", engine.getName(), engine.getClass().getSimpleName()));
        } // try-catch-finally
      }
      while (repeat);

    } else {
      Log.fatal(LogMsg.createMsg(CDX.MSG, "Job.no_engine"));
    }
  }




  /**
   * Shut everything down when the JRE terminates.
   * 
   * <p>There is a shutdown hook registered with the JRE when this Job is 
   * loaded. The shutdown hook will call this method when the JRE is 
   * terminating so that the Job can terminate any long-running processes.</p>
   * 
   * <p>Note: this is different from {@code close()} but {@code shutdown()} 
   * will normally result in {@code close()} being invoked at some point.</p>
   * 
   * @see coyote.loader.thread.ThreadJob#shutdown()
   */
  @Override
  public void shutdown() {
    engine.shutdown();
  }

}
