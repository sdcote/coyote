package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.network.http.Method;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowFilter;
import coyote.mc.snow.SnowSprint;

import java.net.URISyntaxException;

import static coyote.mc.snow.Predicate.IS;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export to retrieve sprints for a
 * project.
 *
 * <p>The primary use case to date is to create a local cache of sprint records which other components can read as part
 * of a pre-processing task to set the start and end dates for the current and previous sprints. For example, a job can
 * be created to read in all the sprints, filter out all but the current sprint and use the {@code }PropertyWriter} to
 * create a property file with other jobs can read in with the {@code ReadIntoContext} task to set a context variable
 * to the start date of the current sprint. This way the other jobs can generate metrics for the current sprint as they
 * will know when the start date is. When run on a daily basis, other metric collectors will have the latest start date
 * for for any sprint and any time.</p>
 */
public class SnowSprintReader extends SnowReader implements FrameReader {


  private static final String RELEASE = "release";

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    getResource().getDefaultParameters().setMethod(Method.GET);

    // TODO: there are two modes, get the sprints by product and get them by release


    String release = getConfiguration().getString(RELEASE);
    if (StringUtil.isBlank(release)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + RELEASE + "' element");
      context.setState("Configuration Error");
      return;
    }

    SnowFilter filter = new SnowFilter("release.number", IS, release);
    try {
      getResource().setPath("rm_sprint.do?JSONv2&sysparm_query=" + filter.toEncodedString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    Log.info("Connecting to " + getResource().getFullURI());
  }


  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = super.read(context);
    if (retval != null) {
      try {
        SnowSprint sprint = new SnowSprint(retval);
        if (sprint.isCurrent())
          Log.info("Sprint: " + sprint.getShortDescription() + " (" + sprint.getNumber() + ") is the current sprint");
      } catch (SnowException e) {
        Log.error("Could not parse retrieved data into a SnowSprint: " + e.getLocalizedMessage());
      }
    }
    return retval;
  }

}
