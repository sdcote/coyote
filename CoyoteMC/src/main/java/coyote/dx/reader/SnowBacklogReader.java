package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.Component;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mc.Browser;
import coyote.mc.WebDriverBuilder;

public class SnowBacklogReader extends WebServiceReader implements FrameReader {

  private volatile boolean endOfFile = false;

  /**
   * @param context
   * @see Component#open(TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    String source = getString(ConfigTag.SOURCE);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));

    // we need the project name from the configuration

    // We need to set the request path to that of the rm_story table

  }


}
