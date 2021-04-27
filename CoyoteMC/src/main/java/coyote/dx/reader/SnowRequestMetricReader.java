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
import coyote.i13n.SimpleMetric;
import coyote.loader.log.Log;
import coyote.mc.MetricUtil;
import coyote.mc.snow.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static coyote.mc.snow.Predicate.IS;
import static coyote.mc.snow.Predicate.LIKE;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the service requests in the "sc_req_item" table.
 *
 * <p>Here is a sample configuration:</p>
 * <pre>
 * "Reader": {
 *   "class": "SnowRequestMetricReader",
 *   "source": "https://someserver.service-now.com/",
 *   "item": "Middleware Assistance",
 *   "instance": "middleware",
 *   "authenticator": {
 *     "class": "BasicAuthentication",
 *     "username": "[#Vault.get(SnowUser,username)#]",
 *     "password": "[#Vault.get(SnowUser,password)#]",
 *     "preemptive": true
 *   }
 * },
 * </pre>
 *
 *
 * <p>The {@code item} element is used to query the requested item.</p>
 *
 * <p>The {@code instance} element is used to specify the instance name grouping key for the metrics.</p>
 */
public class SnowRequestMetricReader extends SnowMetricReader implements FrameReader {
  private static final long ONE_WEEK = 1000 * 60 * 60 * 24 * 7; // in milliseconds
  private static final String NEW_ACTIVE_REQUEST_COUNT = "request_active_new_count";
  private static final String ACTIVE_REQUEST_COUNT = "request_active_count";
  private static final String ACTIVE_REQUEST_AGE_AVG = "request_active_age_avg";
  private static final String NEW_ACTIVE_REQUEST_COUNTS_HELP = "The number of active requests created in the past 24 hours";
  private static final String ACTIVE_REQUEST_AGE_AVG_HELP = "The average age in days of all active requests";
  private static final String ACTIVE_REQUEST_COUNT_HELP = "The current total of active requests";
  private static final String REQUEST_MTTR_AVG = "request_mttr_avg";
  private static final String REQUEST_MTTR_AVG_HELP = "The average MTTR in hours for requests closed over the past 7 days";
  private static final String CLOSED_AT = "closed_at";
  private SnowDateTime window = null;
  private List<SnowRequestItem> requestItems = null;
  private String instanceName = null;

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    instanceName = getConfiguration().getString(INSTANCE);
    if (StringUtil.isBlank(instanceName)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + INSTANCE + "' element");
      context.setState("Configuration Error");
    }

    if (context.isNotInError()) {
      if (filter == null) {
        String item = getConfiguration().getString(ITEM);
        if (StringUtil.isBlank(item)) {
          context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + ITEM + "' or '" + ConfigTag.FILTER + "' element");
          context.setState("Configuration Error");
          return;
        } else {
          filter = new SnowFilter("active", IS, "true").and(CLOSED_AT, Predicate.IS_EMPTY);

          // setup this way so we can start ANDing more clauses from the configuration
          if (StringUtil.isNotBlank(item)) {
            filter.and("cat_item.name", LIKE, item);
          }
        }
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
    if (requestItems == null) {
      if (window == null) window = new SnowDateTime(new Date(getContext().getStartTime() - ONE_WEEK));
      List<SnowRequestItem> closedItems = getClosedItems(window);

      try {
        // not strictly necessary, but aids in debugging
        filter.and("sys_created_on", Predicate.ORDER_BY_DESC);
        getResource().setPath("sc_req_item.do?JSONv2&sysparm_query=" + filter.toEncodedString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }

      requestItems = new ArrayList<>();
      for (DataFrame frame = super.read(context); frame != null; frame = super.read(context)) {
        try {
          requestItems.add(new SnowRequestItem(frame));
        } catch (SnowException e) {
          Log.error("Received invalid data at record #" + (requestItems.size() + 1) + ": " + e.getMessage(), e);
        }
      }
      Log.debug("...read in " + requestItems.size() + " requests.");

      // replace the data frames with our metrics
      dataframes = generateMetrics(requestItems);
      dataframes.addAll(generateMttrCounts(closedItems));
      context.setLastFrame(false);
    }
    return super.read(context); // start returning all the replaced dataframes
  }

  /**
   * Get all the Request items closed since the given date to the current moment.
   *
   * @param window the date starting the window of the query.
   * @return a list of Service Requests closed between the given date and now. May be empty but never null;
   */
  private List<SnowRequestItem> getClosedItems(SnowDateTime window) {
    List<SnowRequestItem> retval = new ArrayList<>();
    String item = getConfiguration().getString(ITEM);

    SnowFilter query = new SnowFilter("cat_item.name", LIKE, item).and(CLOSED_AT, Predicate.GREATER_THAN_EQUALS, new SnowDateTime(new Date()).toQueryFormat());
    try {
      getResource().setPath("sc_req_item.do?JSONv2&sysparm_query=" + query.toEncodedString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    List<DataFrame> closedItems = retrieveData();
    try {
      for (DataFrame frame : closedItems) {
        retval.add(new SnowRequestItem(frame));
      }
    } catch (SnowException e) {
      Log.error("Received invalid data retrieving closed requests at record #" + (requestItems.size() + 1) + ": " + e.getMessage(), e);
    }
    return retval;
  }


  private List<DataFrame> generateMetrics(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    metrics.addAll(generateNewRequestCounts(requests));
    metrics.addAll(generateActiveRequestCounts(requests));
    return metrics;
  }

  private List<DataFrame> generateMttrCounts(List<SnowRequestItem> closedRequests) {
    List<DataFrame> metrics = new ArrayList<>();
    float sumOfDurations = 0;
    for (SnowRequestItem request : closedRequests) {
      float age = request.getMttrInHours();
      sumOfDurations += age;
    }
    float avg;
    if (closedRequests.size() > 0) {
      avg = MetricUtil.round(sumOfDurations / closedRequests.size(), 2);
    } else {
      avg = 0.0F;
    }
    metrics.add(buildMetric(REQUEST_MTTR_AVG, avg, REQUEST_MTTR_AVG_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

  private List<DataFrame> generateActiveRequestCounts(List<SnowRequestItem> activeRequests) {
    List<DataFrame> metrics = new ArrayList<>();
    metrics.add(buildMetric(ACTIVE_REQUEST_COUNT, activeRequests.size(), ACTIVE_REQUEST_COUNT_HELP, CMC.GAUGE, instanceName));

    SimpleMetric metric = new SimpleMetric(ACTIVE_REQUEST_AGE_AVG, "minutes");

    int sumOfAges = 0;
    for (SnowRequestItem request : activeRequests) {
      long age = request.getAgeInMinutes();
      metric.sample(age);
    }

    float avg = MetricUtil.round(metric.getAvgValue() / 1440F, 2);
    metrics.add(buildMetric(ACTIVE_REQUEST_AGE_AVG, avg, ACTIVE_REQUEST_AGE_AVG_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

  private List<DataFrame> generateNewRequestCounts(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    int activeCount = 0;
    for (SnowRequestItem request : requests) {
      if (request.isActive() && request.getCreatedTimestamp().compareTo(window) > 0) activeCount++;
    }
    metrics.add(buildMetric(NEW_ACTIVE_REQUEST_COUNT, activeCount, NEW_ACTIVE_REQUEST_COUNTS_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

}
  
