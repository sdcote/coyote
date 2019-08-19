package coyote.mc;

import com.gargoylesoftware.htmlunit.javascript.host.performance.PerformanceTiming;
import coyote.loader.log.Log;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the browser (Selenium web driver) configured for this thread.
 *
 * <p>Using this class, it is possible to refer to the Selenium web driver in a clean, consistent manner without regard
 * to configuration or multi threaded access. This class binds a different instance of a web driver to each thread so
 * that multi-threaded tests do not try to share the same web driver.</p>
 *
 * <p>The Browser class acts like a wrapper around the Selenium web driver to simplify its use, provide additional
 * functions, make operations more uniform between tests and help make code cleaner with more descriptive method names.</p>
 *
 * <p>This class retrieves the BrowserType value from the Environment and creates a browser of that type. Only the
 * following browser types are supported:<ul>
 * <li>LOCAL (default) - attempts to create a browser which runs locally,</li>
 * <li>REMOTE - attempts to create a web browser on a remote system (e.g. Browserstack, Sauce Labs).</li>
 * </ul>
 *
 * <p>The browser product (Chrome, Firefox, etc.) is specified by the browser name stored in the TestEnvironment values.
 * Not all browser products are supported locally and the names of the browser products on the remote system depend on
 * what is installed there.</p>
 *
 * <p>Each browser product will look for additional configuration properties in the Environment. Refer to
 * the documentation in the creation methods of each of the products supported within the {@link WebDriverBuilder} class.</p>
 */
public class Browser {
  private static final String SCREENSHOT_DIR_DEFAULT = "/target/reports/screenshots/";
  private static final String PAGESOURCE_DIR_DEFAULT = "/target/reports/pagesource/";
  private static ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();

  /**
   * Opens a browser with the given configuration and associates it with the current thread.
   *
   * @param builder the builder which contains the browser configuration
   * @return the WebDriver associated to this current thread.
   */
  public static WebDriver open(WebDriverBuilder builder) {
    WebDriver retval = null;
    if (builder != null) {
      WebDriver existing = webDriver.get();
      if (existing != null) {
        existing.close();
        existing.quit();
      }
      retval = builder.build();
      webDriver.set(retval);
    }
    return retval;
  }


  /**
   * @return the Selenium web driver associated with this thread.
   */
  public static WebDriver getWebDriver() {
    return retrieveOrCreateWebDriver();
  }


  /**
   * Close the underlying Selenium web driver and release resources.
   *
   * <p>Close the current window, quitting the browser if it's the last window currently open. This also quits this driver, closing every associated window.</p>
   *
   * <p>This is important, particularly for remote web drivers. All web browser session should be closed after each test.</p>
   */
  public static void close() {
    WebDriver driver = webDriver.get();
    if (driver != null) {
      Log.info("Closing web driver '" + driver.getClass().getSimpleName() + "'...");
      driver.quit();
      webDriver.set(null);
      Log.info("Web driver '" + driver.getClass().getSimpleName() + "' closed.");
    }
  }


  public static void closeWindow() {
    WebDriver driver = webDriver.get();
    // check to see if there are windows to close. Closing when there are now windows has caused some drivers to hang. (e.g. Chrome 73 in headless mode)
    if (driver != null && driver.getWindowHandles().size() > 0) {
      driver.close();
    }
  }


  /**
   * Retrieve the currently set web driver or create one then set and return it.
   *
   * @return the currently set web driver for this browser, never returns null.
   */
  private static WebDriver retrieveOrCreateWebDriver() {
    WebDriver retval = webDriver.get();
    if (retval == null) {
      retval = new WebDriverBuilder().build();
      webDriver.set(retval);
    }
    return retval;
  }


  /**
   * Take a screen shot of the current page being displayed by the browser.
   *
   * @param filename name of the file
   * @return the location of the screen shot or null if there were problems writing the image to the file system.
   */
  public static String saveScreenShot(String filename) {
    String retval = null;
    if (getWebDriver() instanceof TakesScreenshot) {
      TakesScreenshot ts = (TakesScreenshot) getWebDriver();
      File source = ts.getScreenshotAs(OutputType.FILE);
      String finalName = filename.concat(".png");

      //String dir = TestEnvironment.getProperty(ConfigTags.SCREENSHOT_DIR, SCREENSHOT_DIR_DEFAULT);
      String dir = SCREENSHOT_DIR_DEFAULT;

      if (!dir.endsWith("/")) {
        dir = dir.concat("/");
      }

      File baseDir = new File(dir);
      if (baseDir.isAbsolute()) {
        finalName = dir.concat(finalName);
      } else {
        finalName = System.getProperty("user.dir").concat("/").concat(dir).concat(finalName);
      }

      File destination = new File(finalName);
      try {
        FileUtils.copyFile(source, destination);
        retval = destination.getCanonicalPath();
      } catch (IOException e) {
        Log.error("Could not save screen shot to '" + finalName + "' - " + e.getLocalizedMessage());
      }
    } else {
      Log.info("Warning: " + getWebDriver().getClass().getSimpleName() + " does not support screenshots");
    }
    return retval;
  }


  /**
   * Save the page source of the current page being displayed by the browser to a file.
   *
   * @param filename name of the file
   * @return the location of the page source or null if there were problems writing the image to the file system.
   */
  public static String savePageSource(String filename) {
    String retval = null;
    String finalName = filename.concat(".txt");

    // String dir = TestEnvironment.getProperty(ConfigTags.PAGESOURCE_DIR, PAGESOURCE_DIR_DEFAULT);
    String dir = PAGESOURCE_DIR_DEFAULT;

    if (!dir.endsWith("/")) {
      dir = dir.concat("/");
    }

    File baseDir = new File(dir);
    if (baseDir.isAbsolute()) {
      finalName = dir.concat(finalName);
    } else {
      finalName = System.getProperty("user.dir").concat("/").concat(dir).concat(finalName);
    }

    File destination = new File(finalName);
    try {
      FileUtils.writeStringToFile(destination, Browser.getPageSource(), StandardCharsets.UTF_8);
      retval = destination.getCanonicalPath();
    } catch (IOException e) {
      Log.error("Could not save page source to '" + finalName + "' - " + e.getLocalizedMessage());
    }
    return retval;
  }


  /**
   * Load a new web page in the current browser window.
   *
   * <p>This is done using an HTTP GET operation, and the method will block until the load is complete. This will follow
   * redirects issued either by the server or as a meta-redirect from within the returned HTML. Should a meta-redirect
   * "rest" for any duration of time, it is best to wait until this timeout is over, since should the underlying page
   * change whilst your test is executing the results of future calls against this interface will be against the freshly
   * loaded page. Synonym for {@link org.openqa.selenium.WebDriver.Navigation#to(String)}.
   *
   * @param url The URL to load. It is best to use a fully qualified URL
   */
  public static void get(String url) {
    getWebDriver().get(url);
  }


  /**
   * Get a string representing the current URL that the browser is looking at.
   *
   * @return The URL of the page currently loaded in the browser
   */
  public static String getCurrentUrl() {
    return getWebDriver().getCurrentUrl();
  }


  /**
   * The title of the current page.
   *
   * @return The title of the current page, with leading and trailing whitespace stripped, or null
   * if one is not already set
   */
  public static String getTitle() {
    return getWebDriver().getTitle();
  }


  /**
   * Find all elements within the current page using the given mechanism.
   *
   * <p>This method is affected by the 'implicit wait' times in force at the time of execution. When
   * implicitly waiting, this method will return as soon as there are more than 0 items in the
   * found collection, or will return an empty list if the timeout is reached.</p>
   *
   * @param by The locating mechanism to use
   * @return A list of all {@link WebElement}s, or an empty list if nothing matches
   * @see org.openqa.selenium.By
   * @see org.openqa.selenium.WebDriver.Timeouts
   */
  public static List<WebElement> findElements(By by) {
    return getWebDriver().findElements(by);
  }


  /**
   * Find the first {@link WebElement} using the given method.
   *
   * <p>This method is affected by the 'implicit wait' times in force at the time of execution.
   * The findElement(..) invocation will return a matching row, or try again repeatedly until
   * the configured timeout is reached.</p>
   *
   * <p>findElement should not be used to look for non-present elements, use {@link #findElements(By)}
   * and assert zero length response instead.</p>
   *
   * @param by The locating mechanism
   * @return The first matching element on the current page
   * @throws NoSuchElementException If no matching elements are found
   * @see org.openqa.selenium.By
   * @see org.openqa.selenium.WebDriver.Timeouts
   */
  public static WebElement findElement(By by) {
    return getWebDriver().findElement(by);
  }


  /**
   * Get the source of the last loaded page.
   *
   * <p>If the page has been modified after loading (for example, by Javascript) there is no guarantee that the returned
   * text is that of the modified page. Please consult the documentation of the particular driver being used to
   * determine whether the returned text reflects the current state of the page or the text last sent by the web server.
   * The page source returned is a representation of the underlying DOM: do not expect it to be formatted or escaped in
   * the same way as the response sent from the web server. Think of it as an artist's impression.</p>
   *
   * @return The source of the current page
   */
  public static String getPageSource() {
    return getWebDriver().getPageSource();
  }


  /**
   * Return a set of window handles which can be used to iterate over all open windows of this
   * browser by passing them to {@link #switchTo()}.{@link WebDriver.Options#window()}
   *
   * @return A set of window handles which can be used to iterate over all open windows.
   */
  public static Set<String> getWindowHandles() {
    return getWebDriver().getWindowHandles();
  }


  /**
   * Return an opaque handle to this window that uniquely identifies it within this driver instance.
   * This can be used to switch to this window at a later time
   *
   * @return the current window handle
   */
  public static String getWindowHandle() {
    return getWebDriver().getWindowHandle();
  }


  /**
   * Send future commands to a different frame or window.
   *
   * @return A TargetLocator which can be used to select a frame or window
   * @see org.openqa.selenium.WebDriver.TargetLocator
   */
  public static WebDriver.TargetLocator switchTo() {
    return getWebDriver().switchTo();
  }


  /**
   * An abstraction allowing the driver to access the browser's history and to navigate to a given
   * URL.
   *
   * @return A {@link org.openqa.selenium.WebDriver.Navigation} that allows the selection of what to
   * do next
   */
  public static WebDriver.Navigation navigate() {
    return getWebDriver().navigate();
  }


  /**
   * Gets the Option interface
   *
   * @return An option interface
   * @see org.openqa.selenium.WebDriver.Options
   */
  public static WebDriver.Options manage() {
    return getWebDriver().manage();
  }


  /**
   * Capture the screenshot and store it in the specified location.
   *
   * <p>For WebDriver extending TakesScreenshot, this makes a best effort depending on the browser to return the
   * following in order of preference:
   * <ul>
   * <li>Entire page</li>
   * <li>Current window</li>
   * <li>Visible portion of the current frame</li>
   * <li>The screenshot of the entire display containing the browser</li>
   * </ul>
   *
   * @param <X>    Return type for getScreenshotAs.
   * @param target target type, @see OutputType
   * @return Object in which is stored information about the screenshot.
   * @throws WebDriverException            on failure.
   * @throws UnsupportedOperationException if the underlying implementation does not support screenshot capturing.
   */
  public static <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
    if (getWebDriver() instanceof TakesScreenshot) {
      return ((TakesScreenshot) getWebDriver()).getScreenshotAs(target);
    } else {
      return null;
    }
  }


  /**
   * Return timing information from the current browser.
   *
   * <p>This is performed by executing some Javascript in the browser window to collect data and place it in a table
   * for later retrieval through the WebTimings class.</p>
   *
   * <p>See: https://developer.mozilla.org/en-US/docs/Web/API/PerformanceTiming</p>
   *
   * @return a WebTimings object from the current browser window.
   */
  public static WebTimings getWebTimings() {
    Map<String, Object> retval = null;
    Object obj = ((JavascriptExecutor) getWebDriver()).executeScript("var performance = window.performance || window.webkitPerformance || window.mozPerformance || window.msPerformance || {}; var timings = performance.timing || {}; return timings;");
    if (obj instanceof Map) {
      retval = (Map<String, Object>) obj;
    } else if (obj instanceof PerformanceTiming) {
      PerformanceTiming timings = (PerformanceTiming) obj;
      retval = new HashMap<>();
      retval.put(WebTimings.CONNECT_END, timings.getConnectEnd());
      retval.put(WebTimings.CONNECT_START, timings.getConnectStart());
      retval.put(WebTimings.LOAD_EVENT_END, timings.getLoadEventEnd());
      retval.put(WebTimings.LOAD_EVENT_START, timings.getLoadEventStart());
    } else {
      Log.error("Cannot handle web timing return value of " + obj);
    }
    return new WebTimings(retval, getWebDriver().getCurrentUrl());
  }


  /**
   * Simple wrapper around the WebDriverWait functionality to make code more readable.
   *
   * <p>Sample:<pre>
   *   Browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id(MANAGE_PROFILE_BUTTON_ID)), 30);
   * </pre>
   *
   * @param condition the expected condition for which to wait
   * @param timeout   The timeout in seconds when an expectation is called
   */
  public static void waitFor(ExpectedCondition condition, int timeout) {
    new WebDriverWait(Browser.getWebDriver(), timeout).until(condition);
  }


  /**
   * Wait some amount of time for an element to become visible.
   *
   * <p>This is useful in page models to ensure that the page has loaded correctly or the DOM had finished building.</p>
   *
   * <p>Sample:<pre>
   *   Browser.waitForElementVisibility(By.cssSelector(INCOMPLETE_PROFILE_WARNING_CSS), 30);
   * </pre>
   *
   * @param locator The locating strategy for the element
   * @param timeout the number of seconds to wait
   */
  public static void waitForElementVisibility(By locator, int timeout) {
    Browser.waitFor(ExpectedConditions.visibilityOfElementLocated(locator), timeout);
  }

}
