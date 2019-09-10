package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CMC;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.mc.snow.ServiceNow;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowIncident;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the incidents in the "incident" table.
 *
 * <p>This counts the current number of open incidents</p>
 */
public class SnowIncidentMetricReader extends SnowMetricReader implements FrameReader {
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

      // It is possible that we need either JSON or JSONv2...New Yor may require the latter.
      try {
        getResource().setPath("incident_list.do?JSONv2&sysparm_query=" + filter.toEncodedString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      Log.info("Connecting to " + getResource().getFullURI());

      // the instance name is important grouping key for other metrics of this type
      instanceName = getConfiguration().getString(INSTANCE);
      if (StringUtil.isBlank(instanceName)) {
        context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + INSTANCE + "' element");
        context.setState("Configuration Error");
        return;
      }
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
      Log.debug("...read in " + incidents.size() + " incidents.");

      // replace the dataframes with our metrics
      dataframes = generateMetrics(incidents);
    }
    return super.read(context); // start returning all the replaced dataframes
  }

  private List<DataFrame> generateMetrics(List<SnowIncident> incidents) {
    List<DataFrame> metrics = new ArrayList<>();
    Map<String, Integer> counts = new HashMap<>();
    for(int i =1; i<=5; i++){
      counts.put(ServiceNow.getPriorityValue(Integer.toString(i)).toLowerCase(),0);
    }
    int total =0;
    for (SnowIncident incident : incidents) {
      if( incident.isActive()) {
        total++;
        String priority = ServiceNow.getPriorityValue(incident.getPriority()).toLowerCase();
        if (counts.get(priority) != null) {
          counts.put(priority, counts.get(priority) + 1);
        } else {
          counts.put(priority, 1);
        }
      }
    }
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      DataFrame metric = new DataFrame();
      metric.set(ConfigTag.NAME, entry.getKey() + "_incident_count");
      metric.set(ConfigTag.VALUE, entry.getValue());
      metric.set(ConfigTag.HELP, "The number of active incidents items with the priority of '" + entry.getKey() + "'");
      metric.set(ConfigTag.TYPE, CMC.GAUGE);
      metric.set(INSTANCE, instanceName);
      metrics.add(metric);
    }
    return metrics;
  }

}
  
