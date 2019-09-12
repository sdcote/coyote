package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dx.CDX;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransformContext;
import coyote.dx.web.ExchangeType;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

/**
 * This is a reader which connects to a Jenkins instance and queries data via its REST API and generates metrics based
 * on the build data.
 */
public class JenkinsMetricReader extends WebServiceReader implements FrameReader {
  public static final String INTERVAL_TAG = "Interval";
  private long window = 1000 * 60 * 60;

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
    String source = getString(ConfigTag.SOURCE);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));

    String query = getConfiguration().getString(ConfigTag.JOB);
    if (StringUtil.isNotBlank(query)) {

    }

    if (StringUtil.isEmpty(getString(ConfigTag.SELECTOR))) {
      getConfiguration().set(ConfigTag.SELECTOR, "records.*");
    }


    if (getConfiguration().containsIgnoreCase(INTERVAL_TAG)) {
      String intervalTag = getConfiguration().getAsString(INTERVAL_TAG);

      if ((intervalTag != null) && (intervalTag.trim().length() > 0)) {
        intervalTag = intervalTag.trim().toUpperCase();

        long interval = 0;

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
          System.err.println("Could not parse '" + intervalTag + "' into an interval number");
        }

        // Make sure the interval is positive and not zero
        if (interval > 0) {
          window = interval;
        }

      }
    }


  }

}
