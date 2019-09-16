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
public abstract class SnowMetricReader extends SnowReader implements FrameReader {


}
