package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowIncident;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the incidents in the "incident" table.
 *
 * <p>This counts the current number of open incidents</p>
 */
public class SnowIncidentMetricReader extends SnowReader implements FrameReader {

  private List<SnowIncident> incidents = null;
  private String instanceName = null;

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    if (context.isNotInError()) {
      Log.info("Encoded " + filter.toEncodedString());

      // We need to set the request path to that of the rm_story table
      try {
        getResource().setPath("incident_list.do?JSON&displayvalue=all&sysparm_query=" + filter.toEncodedString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      Log.info("Connecting to " + getResource().getFullURI());
    }
  }

  /**
   * Read in all the data via the web service reader, and replace its "read-in" data with our own, the metrics.
   *
   * @param context the context containing data related to the current transaction.
   * @return the metrics generated from the stories read in from ServiceNow
   */
  @Override
  public DataFrame read(TransactionContext context) {
    if (incidents == null) {
      incidents = new ArrayList<>();
      for (DataFrame frame = super.read(context); frame != null; frame = super.read(context)) {
        try {
          incidents.add(new SnowIncident(frame));
        } catch (SnowException e) {
          Log.error("Received invalid data at record #" + (incidents.size() + 1) + ": " + e.getMessage(), e);
        }
      }
      Log.debug("...read in " + incidents.size() + " stories.");

      // replace the dataframes with our metrics
      //dataframes = generateMetrics(stories);
    }
    return super.read(context); // start returning all the replaced dataframes
  }

}
  
