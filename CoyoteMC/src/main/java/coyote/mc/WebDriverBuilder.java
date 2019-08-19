package coyote.mc;

import coyote.loader.log.Log;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the logic for building different types of Selenium web drivers based on the currently set
 * configuration.
 */
public class WebDriverBuilder {

  // Browser names supported
  public static final String CHROME = "chrome";
  public static final String CHROME_HEADLESS = "chromeheadless";
  public static final String CHROME73 = "chrome73";
  public static final String CHROME73_HEADLESS = "chrome73headless";
  public static final String CHROME74 = "chrome74";
  public static final String CHROME74_HEADLESS = "chrome74headless";
  public static final String CHROME75 = "chrome75";
  public static final String CHROME75_HEADLESS = "chrome75headless";
  public static final String HEADLESS = "headless";

  // Browser types supported
  private static final String LOCAL = "local";
  private static final String REMOTE = "remote";

  // Default settings
  private static final String DEFAULT_NAME = HEADLESS;
  private static final String DEFAULT_TYPE = LOCAL;
  private static final int DEFAULT_LOAD_TIMEOUT = 10;
  private static final int DEFAULT_SCRIPT_TIMEOUT = 5;
  private static final int DEFAULT_WAIT_TIMEOUT = 1;
  private static final int DEFAULT_WIDTH = 1024;
  private static final int DEFAULT_HEIGHT = 768;

  // Chrome system properties to control how the Chrome driver operates
  private static final String CHROME_DRIVER_PROPERTY = "webdriver.chrome.driver";
  private static final String CHROME_SILENT_PROPERTY = "webdriver.chrome.silentOutput";
  private static final String CHROME_VERBOSE_PROPERTY = "webdriver.chrome.verboseLogging";


  private String name;
  private String driverType;
  private String driverPath;
  private int pageLoadTimeout;
  private int width;
  private int height;
  private long scriptTimeout;
  private long waitTimeout;


  public WebDriverBuilder() {
    setName(DEFAULT_NAME);
    setPageLoadTimeout(DEFAULT_LOAD_TIMEOUT);
    setWaitTimeout(DEFAULT_WAIT_TIMEOUT);
    setScriptTimeout(DEFAULT_SCRIPT_TIMEOUT);
    setHeight(DEFAULT_HEIGHT);
    setWidth(DEFAULT_WIDTH);
  }


  private static WebDriver createRemoteDriver() {
    WebDriver retval = null;

    // TODO: This is all just a placeholder for a more robust remote driver creation.
    // These capabilities should be read-in from the TestEnvironment properties
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setCapability("browserName", "iPhone");
    capabilities.setCapability("device", "iPhone 8 Plus");
    capabilities.setCapability("realMobile", "true");
    capabilities.setCapability("os_version", "11");
    capabilities.setCapability("name", "Bstack-[Java] Sample Test");

    // The URL should be read in from the properties file
    String USERNAME = "USERNAME";
    String AUTOMATE_KEY = "ACCESS_KEY";
    String URL = "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";

    try {
      retval = new RemoteWebDriver(new URL(URL), capabilities);
    } catch (MalformedURLException e) {
      Log.error("Invalid URL for remote browser: " + URL);
    }
    return retval;
  }

  /**
   * @return the type of browser driver to create. default="local"
   */
  private static String getBrowserType() {
    return LOCAL;
  }


  public String getName() {
    return name;
  }


  public WebDriverBuilder setName(String name) {
    this.name = name;
    return this;
  }


  public int getPageLoadTimeout() {
    return pageLoadTimeout;
  }


  public WebDriverBuilder setPageLoadTimeout(int timeout) {
    this.pageLoadTimeout = timeout;
    return this;
  }


  private long getScriptTimeout() {
    return scriptTimeout;
  }


  public WebDriverBuilder setScriptTimeout(int timeout) {
    this.scriptTimeout = timeout;
    return this;
  }


  private long getWaitTimeout() {
    return waitTimeout;
  }


  public WebDriverBuilder setWaitTimeout(int timeout) {
    this.waitTimeout = timeout;
    return this;
  }


  public int getWidth() {
    return width;
  }


  public WebDriverBuilder setWidth(int width) {
    this.width = width;
    return this;
  }


  public int getHeight() {
    return height;
  }


  public WebDriverBuilder setHeight(int height) {
    this.height = height;
    return this;
  }

  public String getDriverPath() {
    return driverPath;
  }

  public WebDriverBuilder setDriverPath(String path) {
    driverPath = path;
    return this;
  }

  public String getDriverType() {
    return driverType;
  }

  public WebDriverBuilder setDriverType(String driverType) {
    this.driverType = driverType;
    return this;
  }


  public WebDriver build() {
    WebDriver retval = null;
    String browserType = getBrowserType();
    switch (browserType) {
      case LOCAL:
        retval = createLocalDriver();
        break;
      case REMOTE:
        retval = createRemoteDriver();
        break;
    }
    return retval;
  }


  private WebDriver createLocalDriver() {
    WebDriver retval = null;
    String driverType = getName().toLowerCase();
    switch (driverType) {
      case HEADLESS:
        retval = createHtmlUnit();
        break;
      case CHROME75:
        retval = createChrome("chromedriver75.exe");
        break;
      case CHROME75_HEADLESS:
        retval = createChromeHeadless("chromedriver75.exe");
        break;
      case CHROME73:
        retval = createChrome("chromedriver73.exe");
        break;
      case CHROME73_HEADLESS:
        retval = createChromeHeadless("chromedriver73.exe");
        break;
      case CHROME:
      case CHROME74:
        retval = createChrome("chromedriver74.exe");
        break;
      case CHROME_HEADLESS:
      case CHROME74_HEADLESS:
        retval = createChromeHeadless("chromedriver74.exe");
        break;
      default:
        throw new IllegalArgumentException("Unsupported Browser Name: '" + getName() + "'");
    }

    retval.manage().window().setSize(new Dimension(getWidth(), getHeight()));
    retval.manage().timeouts().implicitlyWait(getWaitTimeout(), TimeUnit.SECONDS);
    retval.manage().timeouts().pageLoadTimeout(getPageLoadTimeout(), TimeUnit.SECONDS);
    retval.manage().timeouts().setScriptTimeout(getScriptTimeout(), TimeUnit.SECONDS);
    retval.manage().deleteAllCookies();

    return retval;
  }


  private WebDriver createHtmlUnit() {
    WebDriver retval = new HtmlUnitDriver(true);
    // turn off htmlunit warnings - HtmlUnit has problems with common CSS
    // They are caused by HtmlUnit's JavaScript engine Mozilla’s Rhino being unable to properly interpret some JavaScript code.
    java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
    return retval;
  }


  private WebDriver createChrome(String driver) {
    WebDriver retval = null;
    ChromeOptions options = new ChromeOptions();
    String path = driverPath + "/" + driver;
    System.setProperty(CHROME_DRIVER_PROPERTY, path);
    if (System.getProperty(CHROME_VERBOSE_PROPERTY) == null) {
      System.setProperty(CHROME_SILENT_PROPERTY, "true");
    }
    retval = new ChromeDriver(options);
    return retval;
  }


  private WebDriver createChromeHeadless(String driver) {
    WebDriver retval = null;
    ChromeOptions options = new ChromeOptions();
    options.addArguments("headless");
    options.addArguments("disable-gpu"); // Needed for windows?
    String path = driverPath + "/" + driver;
    System.setProperty(CHROME_DRIVER_PROPERTY, path);
    if (System.getProperty(CHROME_VERBOSE_PROPERTY) == null) {
      System.setProperty(CHROME_SILENT_PROPERTY, "true");
    }
    retval = new ChromeDriver(options);
    return retval;
  }

}
