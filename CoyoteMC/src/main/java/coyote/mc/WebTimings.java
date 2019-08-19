package coyote.mc;

import java.util.Map;

public class WebTimings {

  public static final String CONNECT_END = "connectEnd";
  public static final String CONNECT_START = "connectStart";
  public static final String DOM_COMPLETE = "domComplete";
  public static final String DOM_CONTENT_LOADED_EVENTEND = "domContentLoadedEventEnd";
  public static final String DOM_CONTENT_LOADED_EVENTSTART = "domContentLoadedEventStart";
  public static final String DOM_INTERACTIVE = "domInteractive";
  public static final String DOM_LOADING = "domLoading";
  public static final String DOMAIN_LOOKUP_END = "domainLookupEnd";
  public static final String DOMAIN_LOOKUP_START = "domainLookupStart";
  public static final String FETCH_START = "fetchStart";
  public static final String LOAD_EVENT_END = "loadEventEnd";
  public static final String LOAD_EVENT_START = "loadEventStart";
  public static final String NAVIGATION_START = "navigationStart";
  public static final String REDIRECT_END = "redirectEnd";
  public static final String REDIRECT_START = "redirectStart";
  public static final String REQUEST_START = "requestStart";
  public static final String RESPONSE_END = "responseEnd";
  public static final String RESPONSE_START = "responseStart";
  public static final String SECURE_CONNECTION_START = "secureConnectionStart";
  public static final String TO_JSON = "toJSON";
  public static final String UNLOAD_EVENT_END = "unloadEventEnd";
  public static final String UNLOAD_EVENT_START = "unloadEventStart";

  private final Map<String, Object> timestamps;
  private final String resource;

  /**
   * Create a set of timings for a resource.
   *
   * @param timings map of timestamps (epoch milliseconds) to event name
   * @param url     the URL to the resource to which the timings apply
   */
  public WebTimings(Map<String, Object> timings, String url) {
    timestamps = timings;
    resource = url;
  }


  /**
   * @return the Resource URL for which these timings apply.
   */
  public String getResource() {
    return resource;
  }


  /**
   * Returns an unsigned long long representing the moment, in milliseconds since the UNIX epoch, right after the prompt
   * for unload terminates on the previous document in the same browsing context.
   *
   * <p>If there is no previous document, this value will be the same as getFetchStart.</p>
   *
   * @return timestamp in epoch milliseconds when the current resources was requested to load in the browser
   */
  public long getNavigationStart() {
    return timestamps.containsKey(NAVIGATION_START) ? (Long) timestamps.get(NAVIGATION_START) : 0;
  }


  /**
   * @return When the load event was sent for the current document. If this event has not yet been sent, it returns 0.
   */
  public long getLoadEventStart() {
    return timestamps.containsKey(LOAD_EVENT_START) ? (Long) timestamps.get(LOAD_EVENT_START) : 0;
  }


  /**
   * @return When the load event handler terminated, that is when the load event is completed. If this event has not yet been sent, or is not yet completed, it returns 0.
   */
  public long getLoadEventEnd() {
    return timestamps.containsKey(LOAD_EVENT_END) ? (Long) timestamps.get(LOAD_EVENT_END) : 0;
  }


  /**
   * @return The number of milliseconds between the LoadEventStart and LoadEventEnd.
   */
  public long getPageLoadElapsed() {
    return getLoadEventEnd() - getLoadEventStart();
  }


  /**
   * Return the elapsed time in milliseconds the resource tool to load in the browser.
   *
   * <p>See: https://www.w3.org/blog/2012/09/performance-timing-information/</p>
   *
   * @return the tile elapsed in milliseconds between NavigationStart and LoadEventEnd - includes redirects and all processing.
   */
  public long getResourceLoadElapsed() {
    return getLoadEventEnd() - getNavigationStart();
  }

}
