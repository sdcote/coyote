package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.CMC;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowFilter;
import coyote.mc.snow.SnowStory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static coyote.mc.snow.Predicate.IS;
import static coyote.mc.snow.Predicate.LIKE;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the stories in the "rm_story" table.
 *
 * <p>The goal of this reader is to generate metrics for sending backlog metrics to a writer for reporting or
 * publication to a database for later reporting.</p>
 *
 * <p>This reader extends the WebServiceReader to make use of a common configuration and HTTP client framework. It
 * differs from other readers of this type by performing a read of all records from the source system on the first read
 * request at which time it consumes all the records, generates all its metrics then replaces the read-in records
 * (dataframes) with the generated metric dataframes.</p>
 *
 * <p>The following is a sample configuration:<pre>
 * "Reader" : {
 *   "class" : "SnowBacklogMetricReader",
 *   "source" : "https://myinstance.service-now.com/",
 *   "project": "My Project Name",
 *   "authenticator": {
 *     "class" : "BasicAuthentication",
 *     "ENC:username" : "DXvYrFrtzPzKAGYBWbVRCDqmV4Qn/QXi",
 *     "ENC:password" : "UO/dcwkHYck/0zSKBSfnRv5kh0b2fqnl",
 *     "preemptive" : true
 *   },
 *   "Protocol" : { "ExchangeType" : "JSON_HTTP" }
 * },
 * </pre>
 */
public class SnowBacklogMetricReader extends WebServiceReader implements FrameReader {

  public static final String PROJECT = "project";
  private List<SnowStory> stories = null;

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    String source = getString(ConfigTag.SOURCE);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));

    // we need the project name from the configuration
    String project = getConfiguration().getString(PROJECT);

    SnowFilter filter = new SnowFilter("active", IS, "true")
            .and("product.name", LIKE, project);

    if (StringUtil.isEmpty(getString(ConfigTag.SELECTOR))) {
      getConfiguration().set(ConfigTag.SELECTOR, "records.*");
    }

    Log.info("Encoded " + filter.toEncodedString());

    // We need to set the request path to that of the rm_story table
    try {
      getResource().setPath("rm_story_list.do?JSON&displayvalue=all&sysparm_query=" + filter.toEncodedString());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    Log.info("Connecting to " + getResource().getFullURI());
  }

  /**
   * Read in all the data via the web service reader, and replace its "read-in" data with our own, the metrics.
   *
   * @param context the context containing data related to the current transaction.
   * @return the metrics generated from the stories read in from ServiceNow
   */
  @Override
  public DataFrame read(TransactionContext context) {
    if (stories == null) {
      stories = new ArrayList<>();
      for (DataFrame frame = super.read(context); frame != null; frame = super.read(context)) {
        try {
          stories.add(new SnowStory(frame));
        } catch (SnowException e) {
          Log.error("Received an invalid data at record #" + (stories.size() + 1) + ": " + e.getMessage(), e);
        }
      }
      Log.debug("...read in " + stories.size() + " stories.");

      // replace the dataframes with our metrics
      dataframes = generateMetrics(stories);
    }
    return super.read(context); // return all the replaced dataframes
  }

  /**
   * Generate metrics from the given stories
   *
   * @param stories the stories read in from the backlog
   * @return a set of metrics describing the characteristics of the backlog stories
   */
  private List<DataFrame> generateMetrics(List<SnowStory> stories) {
    List<DataFrame> metrics = new ArrayList<>();

    // TODO: we can reuse the resource to get other records, like sprints and releases for xref

    metrics.addAll(generateClassificationCounts(stories));

    return metrics;
  }

  /**
   * @param stories the SnowStories representing the backlog to analyze
   * @return a set of metrics relating to the classification of active stories
   */
  private List<DataFrame> generateClassificationCounts(List<SnowStory> stories) {
    List<DataFrame> metrics = new ArrayList<>();
    Map<String, Integer> counts = new HashMap<>();
    for (SnowStory frame : stories) {
      String classification = frame.getClassification().toLowerCase();
      if (counts.get(classification) != null) {
        counts.put(classification, counts.get(classification) + 1);
      } else {
        counts.put(classification, 1);
      }
    }
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      DataFrame metric = new DataFrame();
      metric.set(ConfigTag.NAME, entry.getKey() + "_count");
      metric.set(ConfigTag.VALUE, entry.getValue());
      metric.set(ConfigTag.DESCRIPTION, "The number of active backlog items with the classification of '" + entry.getKey() + "'");
      metric.set(ConfigTag.TYPE, CMC.GAUGE);
      metrics.add(metric);
    }
    return metrics;
  }

}
