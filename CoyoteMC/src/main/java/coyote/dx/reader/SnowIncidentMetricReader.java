/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.network.http.Method;
import coyote.dataframe.DataFrame;
import coyote.dx.CMC;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.mc.snow.*;

import java.net.URISyntaxException;
import java.util.*;

import static coyote.mc.snow.Predicate.IS;
import static coyote.mc.snow.Predicate.LIKE;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the incidents in the "incident" table.
 *
 * <p>This counts the current number of open incidents</p>
 *
 * <p>An example configuration would look like this:<pre>
 * "Reader" : {
 *   "class" : "SnowIncidentMetricReader",
 *   "source" : "https://myinstance.service-now.com/",
 *   "product": "Enterprise Widget Framework",
 *   "ConfigurationItem": "thingy",
 *   "instance": "widgets",
 *   "authenticator": {
 *     "class" : "BasicAuthentication",
 *     "username": "[#Vault.get(SnowUser,username)#]",
 *     "password": "[#Vault.get(SnowUser,password)#]",
 *     "preemptive" : true
 *   }
 * },
 * </pre>
 * <p>In the above the optional {@code product} configuration element is used to query the releases and sprint tables
 * for that product to determine the current sprint. The start date-time is used in determining what is considered a
 * "new" incident. If no product is specified, then the default sliding window of two weeks is used. If the product,
 * its releases, or any otherwise active sprint is found, all incidents will appear new and the new count will equal
 * the total count.</p>
 *
 * <p>The {@code configurationitem} element is used to query the symptom_ci for the incidents.</p>
 *
 * <p>The {@code instance} element is used to specify the instance name grouping key for the metrics.</p>
 */
public class SnowIncidentMetricReader extends SnowMetricReader implements FrameReader {
  private static final long TWO_WEEKS = 1000 * 60 * 60 * 24 * 14; // in milliseconds
  private static final String NEW_ACTIVE_INCIDENT_COUNT = "incident_active_new_count";
  private static final String ACTIVE_INCIDENT_COUNT = "incident_active_count";
  SnowDateTime sprintStart = null;
  private List<SnowIncident> incidents = null;
  private String instanceName = null;

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    // the instance name is important grouping key for other metrics of this type
    instanceName = getConfiguration().getString(INSTANCE);
    if (StringUtil.isBlank(instanceName)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + INSTANCE + "' element");
      context.setState("Configuration Error");
      return;
    }

    if (context.isNotInError()) {
      if (filter == null) {
        String configurationItem = getConfiguration().getString(CONFIG_ITEM);
        if (StringUtil.isBlank(configurationItem)) {
          context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + CONFIG_ITEM + "' or '" + ConfigTag.FILTER + "' element");
          context.setState("Configuration Error");
          return;
        }
        filter = new SnowFilter("cmdb_ci", LIKE, configurationItem).and("active", IS, "true");
      }

      getResource().getDefaultParameters().setMethod(Method.GET);
    } // context not in error
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

      if (StringUtil.isNotBlank(getConfiguration().getString(PRODUCT))) {
        List<SnowSprint> sprints = getSprints(getConfiguration().getString(PRODUCT));
        for (SnowSprint sprint : sprints) {
          if (sprint.isCurrent()) {
            Log.notice("Current sprint " + sprint.getShortDescription() + " (" + sprint.getScheduledStartDate() + " - " + sprint.getScheduledEndDate() + ")");
            sprintStart = sprint.getScheduledStartDate();
            break;
          }
        }
      } else {
        Log.notice("No product was specified, using the default interval for 'new' incidents.");
      }

      if (sprintStart == null) sprintStart = new SnowDateTime(new Date(getContext().getStartTime() - TWO_WEEKS));

      try {
        getResource().setPath("incident_list.do?JSONv2&sysparm_query=" + filter.toEncodedString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }


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
    metrics.addAll(generateNewIncidentCounts(incidents));
    metrics.addAll(generateIncidentTypeCounts(incidents));
    return metrics;
  }

  private List<DataFrame> generateIncidentTypeCounts(List<SnowIncident> incidents) {
    List<DataFrame> metrics = new ArrayList<>();
    Map<String, Integer> counts = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      counts.put(ServiceNow.getPriorityValue(Integer.toString(i)).toLowerCase(), 0);
    }
    int total = 0;
    for (SnowIncident incident : incidents) {
      if (incident.isActive()) {
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
      metric.set(ConfigTag.NAME, "incident_" + entry.getKey() + "_count");
      metric.set(ConfigTag.VALUE, entry.getValue());
      metric.set(ConfigTag.HELP, "The number of active incidents items with the priority of '" + entry.getKey() + "'");
      metric.set(ConfigTag.TYPE, CMC.GAUGE);
      metric.set(INSTANCE, instanceName);
      metrics.add(metric);
    }
    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, ACTIVE_INCIDENT_COUNT);
    metric.set(ConfigTag.VALUE, total);
    metric.set(ConfigTag.HELP, "The number of active incidents");
    metric.set(ConfigTag.TYPE, CMC.GAUGE);
    metric.set(INSTANCE, instanceName);
    metrics.add(metric);

    return metrics;
  }

  private List<DataFrame> generateNewIncidentCounts(List<SnowIncident> incidents) {
    List<DataFrame> metrics = new ArrayList<>();
    int activeCount = 0;
    for (SnowIncident incident : incidents) {
      if (incident.isActive() && incident.getCreatedTimestamp().compareTo(sprintStart) > 0) activeCount++;
    }
    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, NEW_ACTIVE_INCIDENT_COUNT);
    metric.set(ConfigTag.VALUE, activeCount);
    metric.set(ConfigTag.HELP, "The number of new active incidents");
    metric.set(ConfigTag.TYPE, CMC.GAUGE);
    metric.set(INSTANCE, instanceName);
    metrics.add(metric);
    return metrics;
  }


}
  
