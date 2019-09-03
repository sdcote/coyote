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

public class WebPageMetricReader extends AbstractFrameReader implements FrameReader {

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

    WebDriverBuilder builder = new WebDriverBuilder();
    /*
      # Specifies the type of browser, either local or remote. (default = local)
      BrowserType = Local

      # Specifies the name of the browser product. See the README.md for possible values. (default = ChromeHeadless)
      BrowserName = ChromeHeadless

      # Specifies into which directory screen shots will be placed. Relative paths are resolved from the current working directory.
      ScreenShotDirectory = target/reports/dev-screenshots/

      # The name of the directory where all page source dumps will be placed (on errors)
      PageSourceDirectory = target/reports/dev-pagesource/

      # The name of the directory containing the Selenium web drivers
      WebDriverDirectory = target/reports/dev-pagesource/

      # The browser window height in pixels. (default=768)
      BrowserHeight = 768

      # The browser window width in pixels. (default=1024)
      BrowserWidth = 1024

      # Sets the number of seconds to wait for a page load to complete before throwing an error. (a.k.a. Page Load Timeout)
      BrowserLoadTimeout = 10

      # Sets the number of seconds to wait for an asynchronous script to finish execution before throwing an error.
      BrowserScriptTimeout = 10

      # Specifies the number of seconds the driver should wait when searching for an element if it is not immediately present. (a.k.a. Implicit Wait)
      BrowserWaitTimeout = 2
     */
    Browser.open(builder);


  }

  @Override
  public DataFrame read(TransactionContext context) {
    Log.info(getClass().getSimpleName() + " reading...");
    Browser.get(getSource());
    //WebTimings timings = Browser.getWebTimings();
    //Log.info(timings.toString());
    endOfFile = true;
    return null;
  }

  @Override
  public boolean eof() {
    return endOfFile;
  }

}
