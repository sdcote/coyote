/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.reader;

import coyote.commons.DateUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.DataFrameException;
import coyote.dx.CMC;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.web.ExchangeType;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This is a reader which connects to a Jenkins instance and queries data via its REST API and generates metrics based
 * on the build data.
 *
 * <p>A sample configuration for a multi-branch pipeline looks like this:<pre>
 * "Reader" : {
 *   "class" : "JenkinsMetricReader",
 *   "source" : "http://ecdp3:8080",
 *   "job": "/job/Coyote-multibranch-pipeline/job/develop",
 *   "interval": "14d",
 *   "instance": "develop",
 *   "authenticator": {
 *     "class" : "BasicAuthentication",
 *     "username": "[#Vault.get(JenkinsUser,username)#]",
 *     "password": "[#Vault.get(JenkinsUser,password)#]",
 *     "preemptive" : true
 *   }
 * },
 * </pre>
 *
 * <p>Just pull the path specifying the job from the API link at the bottom right-hand corner of the page to determine
 * the values for {@code source} and {@code job}.</p>
 *
 * <p>The {@code interval} configuration item is optional and will override the default of 14D (14 days). Additionally,
 * if the context contains an ISO formatted date (YYYY-MM-DD HH:MM:SS) in the context mapped to the key of
 * {@code new.threshold}, then that date will be used for determining new builds. This value is most often populated by
 * the {@code ReadIntoContext} task which reads the properties written by a {@code PropertyWriter} after some reader
 * has determined the start date of the current sprint, for example. One such application of this is the use of the
 * {@code SnowSprintReader}, a {@code Filter} to spot the current sprint record, and a couple of {@code Transform}
 * components to create the "name" and "value" fields which the {@code PropertyWriter} writes to a file for this
 * component's job to read into its context.</p>
 *
 * <p>The {@code instance} configuration item is required as the grouping key for the metrics generated</p>
 */
public class JenkinsMetricReader extends WebServiceReader implements FrameReader {
  public static final String SUCCESS = "SUCCESS";
  public static final String FAILURE = "FAILURE";
  public static final String INTERVAL_TAG = "Interval";
  private static final String INSTANCE = "instance";
  private static final String NUMBER = "number";
  private static final String DURATION = "duration";
  private static final String RESULT = "result";
  private static final String TIMESTAMP = "timestamp";
  private static final String NEW_THRESHOLD = "new.threshold";

  private static final long DEFAULT_INTERVAL = 1000 * 60 * 60; //1 hour
  private long window = 0;
  private List<Build> builds = null;
  private String instanceName = null;


  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    if (getConfiguration().getSection(ConfigTag.PROTOCOL) == null) {
      Config protocolSection = new Config();
      protocolSection.set(CWS.EXCHANGE_TYPE, ExchangeType.JSON_HTTP.toString());
      getConfiguration().put(ConfigTag.PROTOCOL, protocolSection);
    }

    super.open(context);

    instanceName = getConfiguration().getString(INSTANCE);
    if (StringUtil.isBlank(instanceName)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + INSTANCE + "' element");
      context.setState("Configuration Error");
      return;
    }

    String job = getConfiguration().getString(ConfigTag.JOB);
    if (StringUtil.isBlank(job)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + ConfigTag.JOB + "' element");
      context.setState("Configuration Error");
      return;
    } else {
      if (!job.startsWith("/")) job = "/" + job;
      job = job.concat("/api/json?tree=builds[number,timestamp,result,duration]");
      try {
        getResource().setPath(job);
      } catch (URISyntaxException e) {
        context.setError("The " + getClass().getSimpleName() + " configuration contained invalid characters in the '" + ConfigTag.JOB + "' element: " + e.getMessage());
        context.setState("Configuration Error");
        return;
      }
    }

    if (StringUtil.isEmpty(getString(ConfigTag.SELECTOR))) {
      getConfiguration().set(ConfigTag.SELECTOR, "builds.*");
    }

    String threshold = getContext().getAsString(NEW_THRESHOLD);
    if (StringUtil.isNotBlank(threshold)) {
      Date thresholdDate = DateUtil.parse(threshold);
      if (thresholdDate != null) {
        Log.notice("Found threshold for new builds in context '" + threshold + "' - setting date to " + thresholdDate + " - " + window);
        window = thresholdDate.getTime();
      } else {
        Log.warn("Could not parse threshold for new builds as a date '" + threshold + "' - ignoring");
      }
    }

    // configuration overrides context settings
    if (getConfiguration().containsIgnoreCase(INTERVAL_TAG)) {
      long interval = 0;
      String intervalTag = getConfiguration().getString(INTERVAL_TAG);
      if ((intervalTag != null) && (intervalTag.trim().length() > 0)) {
        intervalTag = intervalTag.trim().toUpperCase();
        try {
          if (intervalTag.endsWith("D") || intervalTag.endsWith("d")) {
            interval = Long.parseLong(intervalTag.substring(0, (intervalTag.length() - 1))) * (24 * 60 * 60 * 1000);
          } else if (intervalTag.endsWith("H") || intervalTag.endsWith("h")) {
            interval = Long.parseLong(intervalTag.substring(0, (intervalTag.length() - 1))) * (60 * 60 * 1000);
          } else if (intervalTag.endsWith("M") || intervalTag.endsWith("m")) {
            interval = Long.parseLong(intervalTag.substring(0, (intervalTag.length() - 1))) * (60 * 1000);
          } else if (intervalTag.endsWith("S") || intervalTag.endsWith("s")) {
            interval = Long.parseLong(intervalTag.substring(0, (intervalTag.length() - 1))) * (1000);
          } else {
            interval = Long.parseLong(intervalTag) * (1000);
          }
        } catch (final NumberFormatException e) {
          System.err.println("Could not parse '" + intervalTag + "' into an interval number - format = X[D|H|M|S]");
        }
      }
      if (interval > 0) {
        window = getContext().getStartTime() - interval;
        Log.debug("Threshold for new builds starts at " + new Date(window).toString() + " - " + window);
      }
    }

    // if noting in the context and nothing in the configuration, use the default
    if (window == 0) {
      window = getContext().getStartTime() - DEFAULT_INTERVAL;
      Log.debug("Using default interval for for new build threshold: " + new Date(window).toString() + " - " + window);
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
    if (builds == null) {
      builds = new ArrayList<>();
      for (DataFrame frame = super.read(context); frame != null; frame = super.read(context)) {
        builds.add(new Build(frame));
      }
      Log.debug("...read in " + builds.size() + " builds.");

      // replace the dataframes with our metrics
      dataframes = generateMetrics(builds);
    }
    return super.read(context); // start returning all the replaced dataframes
  }

  private List<DataFrame> generateMetrics(List<Build> builds) {
    List<DataFrame> retval = new ArrayList<>();
    Build latest = null;

    int successCount = 0;
    int failedCount = 0;
    int unknownCount = 0;
    int recentSuccessCount = 0;
    int recentFailedCount = 0;
    int recentUnknownCount = 0;

    for (Build build : builds) {
      if (latest == null || build.getTimestamp() > latest.getTimestamp()) latest = build;
      if (SUCCESS.equalsIgnoreCase(build.getResult())) {
        successCount++;
        if (build.getTimestamp() >= window) recentSuccessCount++;

      } else if (FAILURE.equalsIgnoreCase(build.getResult())) {
        failedCount++;
        if (build.getTimestamp() >= window) recentFailedCount++;
      } else {
        unknownCount++;
        if (build.getTimestamp() >= window) recentUnknownCount++;
      }
    }

    retval.add(createMetric("build_success_count", successCount, "The number of successful builds on record", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_failure_count", failedCount, "The number of failed builds on record", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_unknown_count", unknownCount, "The number of builds on record with an unknown result", CMC.GAUGE, instanceName));

    retval.add(createMetric("build_success_recent_count", recentSuccessCount, "The number of recent successful builds", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_failure_recent_count", recentFailedCount, "The number of recent failed builds", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_unknown_recent_count", recentUnknownCount, "The number of recent builds with an unknown result", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_recent_count", recentSuccessCount + recentFailedCount + recentUnknownCount, "The latest build number", CMC.GAUGE, instanceName));

    int value = 0;
    if (FAILURE.equalsIgnoreCase(latest.getResult())) value = -1;
    if (SUCCESS.equalsIgnoreCase(latest.getResult())) value = 1;
    retval.add(createMetric("build_status", value, "The latest build status 1=success, -1=failure, 0=aborted/unknown", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_duration", latest.getDuration(), "The latest build duration in milliseconds", CMC.GAUGE, instanceName));
    retval.add(createMetric("build_number", latest.getNumber(), "The latest build number", CMC.GAUGE, instanceName));
    return retval;
  }

  private DataFrame createMetric(String name, int value, String help, String type, String instance) {
    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, name);
    metric.set(ConfigTag.VALUE, value);
    metric.set(ConfigTag.HELP, help);
    metric.set(ConfigTag.TYPE, type);
    metric.set(INSTANCE, instance);
    return metric;
  }

  private class Build {
    int number = 0;
    int duration = 0;
    String result;
    long timestamp = 0;

    public Build(DataFrame frame) {
      try {
        number = (frame.containsIgnoreCase(NUMBER)) ? frame.getAsInt(NUMBER) : 0;
        duration = (frame.containsIgnoreCase(DURATION)) ? frame.getAsInt(DURATION) : 0;
        timestamp = (frame.containsIgnoreCase(TIMESTAMP)) ? frame.getAsLong(TIMESTAMP) : 0;
      } catch (DataFrameException e) {
        Log.error("Could not parse build data record: " + e.getMessage());
        Log.debug("Unknown format: " + frame.toString());
      }
      result = (frame.containsIgnoreCase(RESULT)) ? frame.getAsString(RESULT) : "";
    }

    public long getTimestamp() {
      return timestamp;
    }

    public String getResult() {
      return result;
    }

    public int getDuration() {
      return duration;
    }

    public int getNumber() {
      return number;
    }
  }

}
