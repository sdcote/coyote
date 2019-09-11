package coyote.dx.reader;

import coyote.commons.StringParseException;
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
import coyote.mc.snow.FilterParser;
import coyote.mc.snow.SnowFilter;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the incidents in the "incident" table.
 */
public class SnowMetricReader extends WebServiceReader implements FrameReader {

  public static final String PROJECT = "project";
  public static final String INSTANCE = "instance";
  public static final String CONFIG_ITEM = "ConfigurationItem";

  SnowFilter filter = null;

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

    String query = getConfiguration().getString(ConfigTag.FILTER);
    if (StringUtil.isNotBlank(query)) {
      try {
        filter = FilterParser.parse(query);
      } catch (StringParseException e) {
        context.setError("The " + getClass().getSimpleName() + " configuration contained an invalid filter: " + e.getMessage());
        context.setState("Configuration Error");
      }
    }

    if (StringUtil.isEmpty(getString(ConfigTag.SELECTOR))) {
      getConfiguration().set(ConfigTag.SELECTOR, "records.*");
    }
  }

}
