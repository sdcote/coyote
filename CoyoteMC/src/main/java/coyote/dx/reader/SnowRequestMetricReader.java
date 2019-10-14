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
import coyote.mc.MetricUtil;
import coyote.mc.snow.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static coyote.mc.snow.Predicate.LIKE;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the service requests in the "sc_req_item" table.
 *
 *
 * <p>The {@code item} element is used to query the requested item.</p>
 *
 * <p>The {@code instance} element is used to specify the instance name grouping key for the metrics.</p>
 */
public class SnowRequestMetricReader extends SnowMetricReader implements FrameReader {
  private static final long ONE_DAY = 1000 * 60 * 60 * 24; // in milliseconds
  private static final int DEFAULT_LIMIT = 1000;
  private static final String NEW_ACTIVE_REQUEST_COUNT = "request_active_new_count";
  private static final String ACTIVE_REQUEST_COUNT = "request_active_count";
  private static final String ACTIVE_REQUEST_AGE_AVG = "request_active_age_avg";
  private static final String NEW_ACTIVE_REQUEST_COUNTS_HELP = "The number of new active requests";
  private static final String ACTIVE_REQUEST_AGE_AVG_HELP = "The average age in days of all active requests";
  private static final String ACTIVE_REQUEST_COUNT_HELP = "The number of active requests";
  private static final String REQUEST_MTTR_AVG = "request_mttr_avg";
  private static final String REQUEST_MTTR_AVG_HELP = "The average MTTR in minutes for requests over the past week";
  private static final String CLOSED_AT = "closed_at";
  private SnowDateTime sprintStart = null;
  private List<SnowRequestItem> requestItems = null;
  private String instanceName = null;
  private int limit = DEFAULT_LIMIT;

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

    try {
      limit = getConfiguration().getInt(ConfigTag.LIMIT);
      if (limit < 10) {
        Log.notice("Limit set too low (<10) using default of " + DEFAULT_LIMIT);
        limit = DEFAULT_LIMIT;
      }
    } catch (NumberFormatException e) {
      limit = DEFAULT_LIMIT;
    }

    if (context.isNotInError()) {
      if (filter == null) {
        String item = getConfiguration().getString(ITEM);
        if (StringUtil.isBlank(item)) {
          context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + ITEM + "' or '" + ConfigTag.FILTER + "' element");
          context.setState("Configuration Error");
          return;
        } else {
          filter = new SnowFilter("cat_item.name", LIKE, item);
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

      List<SnowRequestItem> closedItems = getClosedItems(new SnowDateTime(new Date(getContext().getStartTime() - ONE_DAY * 7)));

      if (sprintStart == null) sprintStart = new SnowDateTime(new Date(getContext().getStartTime() - ONE_DAY));

      filter.and("sys_created_on", Predicate.ORDER_BY_DESC);

      try {
        // getResource().setPath("sc_req_item.do?JSONv2&sysparm_record_count=" + limit + "&displayvalue=all&sysparm_query=" + filter.toEncodedString()); // DisplayValue=all causes loads more processing on the server
        getResource().setPath("sc_req_item.do?JSONv2&sysparm_record_count=" + limit + "&sysparm_query=" + filter.toEncodedString());
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

      // replace the dataframes with our metrics
      dataframes = generateMetrics(requestItems);
      context.setLastFrame(false);
    }
    return super.read(context); // start returning all the replaced dataframes
  }

  private List<SnowRequestItem> getClosedItems(SnowDateTime window) {
    List<SnowRequestItem> retval = new ArrayList<>();
    // https://dev67296.service-now.com/sc_req_item_list.do?sysparm_query=closed_at%3E%3Djavascript:gs.dateGenerate(%272019-10-05%27%2C%2700:00:00%27)
    // https://dev67296.service-now.com/sc_req_item_list.do?sysparm_query=closed_at>=javascript:gs.dateGenerate('2019-10-05','00:00:00')
    SnowFilter query = new SnowFilter(CLOSED_AT, Predicate.GREATER_THAN_EQUALS, new SnowDateTime(new Date()).toQueryFormat());

    try {
      getResource().setPath("sc_req_item.do?JSONv2&sysparm_record_count=" + limit + "&sysparm_query=" + query.toEncodedString());
      List<DataFrame> closedItems = retrieveData();
      for (DataFrame frame : closedItems) {
        retval.add(new SnowRequestItem(frame));
      }
    } catch (URISyntaxException | SnowException e) {
      e.printStackTrace();
    }
    return retval;
  }


  private List<DataFrame> generateMetrics(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    metrics.addAll(generateNewRequestCounts(requests));
    metrics.addAll(generateActiveRequestCounts(requests));
    metrics.addAll(generateMttrCounts(requests));
    return metrics;
  }

  private List<DataFrame> generateMttrCounts(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    SnowDateTime lastWeek = new SnowDateTime(new Date(getContext().getStartTime() - (ONE_DAY * 7)));
    int closedCount = 0;
    int sumOfDurations = 0;
    for (SnowRequestItem request : requests) {
      if (request.getCreatedTimestamp().compareTo(lastWeek) >= 0) {
        if (request.isClosed()) {
          int age = request.getMttrInMinutes();
          if (age > 0) {
            closedCount++;
            sumOfDurations += age;
          }
        }
      } else {
        break;
      }
    }
    float avg = MetricUtil.round(sumOfDurations / closedCount, 2);
    metrics.add(buildMetric(REQUEST_MTTR_AVG, avg, REQUEST_MTTR_AVG_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

  private List<DataFrame> generateActiveRequestCounts(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    int total = 0;
    int sumOfAges = 0;
    for (SnowRequestItem request : requests) {
      if (request.isActive()) {
        total++;
        int age = request.getAgeInMinutes();
        sumOfAges += age;
      }
    }

    // get the average minutes per request then divide it by a day's worth of minutes (1440)
    float avg = MetricUtil.round((sumOfAges / total) / 1440, 2);
    metrics.add(buildMetric(ACTIVE_REQUEST_COUNT, total, ACTIVE_REQUEST_COUNT_HELP, CMC.GAUGE, instanceName));
    metrics.add(buildMetric(ACTIVE_REQUEST_AGE_AVG, avg, ACTIVE_REQUEST_AGE_AVG_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

  private List<DataFrame> generateNewRequestCounts(List<SnowRequestItem> requests) {
    List<DataFrame> metrics = new ArrayList<>();
    int activeCount = 0;
    for (SnowRequestItem request : requests) {
      if (request.isActive() && request.getCreatedTimestamp().compareTo(sprintStart) > 0) activeCount++;
    }
    metrics.add(buildMetric(NEW_ACTIVE_REQUEST_COUNT, activeCount, NEW_ACTIVE_REQUEST_COUNTS_HELP, CMC.GAUGE, instanceName));
    return metrics;
  }

}
  
