package coyote.dx.reader;

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
import java.util.*;


/**
 * This is a reader which connects to a Jenkins instance and queries data via its REST API and generates metrics based
 * on the build data.
 * <p>A sample configuration for a multi-branch pipeline looks like this:<pre>
 * "Reader" : {
 *   "class" : "JenkinsMetricReader",
 *   "source" : "http://ecdp3:8080",
 *   "job": "/job/Coyote-multibranch-pipeline/job/develop",
 *   "interval": "14d",
 *   "instance": "develop",
 *   "authenticator": {
 *     "class" : "BasicAuthentication",
 *     "ENC:username" : "zPz4DXvYrFrKAGYBWbVRCDqmVtQn/QXi",
 *     "ENC:password" : "KcwkH5kh0UO/dzSb2Yck/0BSfnRvfqnl",
 *     "preemptive" : true
 *   }
 * },
 * </pre>
 * <p>Just pull the path specifying the job from the API link at the bottom right-hand corner of the page.</p>
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

    long interval = DEFAULT_INTERVAL;
    if (getConfiguration().containsIgnoreCase(INTERVAL_TAG)) {
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
    }
    if (interval > 0) {
      window = getContext().getStartTime() - interval;
      Log.debug("Window starts at " + new Date(window).toString() + " - " + window);
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
    Map<String, Integer> counts = new HashMap<>();
    int buildCount = 0;
    for (Build build : builds) {
      if (latest == null || build.getTimestamp() > latest.getTimestamp()) latest = build;
      if (build.getTimestamp() >= window) {
        buildCount++;
        String result = build.getResult().toLowerCase();
        if (counts.get(result) != null) {
          counts.put(result, counts.get(result) + 1);
        } else {
          counts.put(result, 1);
        }
      }
    }

    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      DataFrame metric = new DataFrame();
      metric.set(ConfigTag.NAME, "build_" + entry.getKey() + "_count_recent");
      metric.set(ConfigTag.VALUE, entry.getValue());
      metric.set(ConfigTag.HELP, "The number recent builds with a result of '" + entry.getKey() + "'");
      metric.set(ConfigTag.TYPE, CMC.GAUGE);
      metric.set(INSTANCE, instanceName);
      retval.add(metric);
    }

    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, "build_status");
    int value = 0;
    if (FAILURE.equalsIgnoreCase(latest.getResult())) value = -1;
    if (SUCCESS.equalsIgnoreCase(latest.getResult())) value = 1;
    metric.set(ConfigTag.VALUE, value);
    metric.set(ConfigTag.HELP, "The latest build status 1=success, -1=failure, 0=aborted/unknown");
    metric.set(ConfigTag.TYPE, CMC.GAUGE);
    metric.set(INSTANCE, instanceName);
    retval.add(metric);

    metric = new DataFrame()
            .set(ConfigTag.NAME, "build_duration")
            .set(ConfigTag.VALUE, latest.getDuration())
            .set(ConfigTag.HELP, "The latest build duration in milliseconds")
            .set(ConfigTag.TYPE, CMC.GAUGE)
            .set(INSTANCE, instanceName);
    retval.add(metric);

    metric = new DataFrame()
            .set(ConfigTag.NAME, "build_number")
            .set(ConfigTag.VALUE, latest.getNumber())
            .set(ConfigTag.HELP, "The latest build number")
            .set(ConfigTag.TYPE, CMC.GAUGE)
            .set(INSTANCE, instanceName);
    retval.add(metric);

    metric = new DataFrame()
            .set(ConfigTag.NAME, "build_count_recent")
            .set(ConfigTag.VALUE, buildCount)
            .set(ConfigTag.HELP, "The number of recent build attempts")
            .set(ConfigTag.TYPE, CMC.GAUGE)
            .set(INSTANCE, instanceName);
    retval.add(metric);

    return retval;
  }

  private class Build {

    long number = 0;
    int duration = 0;
    String result;
    long timestamp = 0;

    public Build(DataFrame frame) {
      try {
        number = (frame.containsIgnoreCase(NUMBER)) ? frame.getAsLong(NUMBER) : 0;
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

    public long getNumber() {
      return number;
    }
  }
}
