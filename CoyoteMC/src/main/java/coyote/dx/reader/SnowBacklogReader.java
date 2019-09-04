package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mc.snow.SnowFilter;

import java.net.URISyntaxException;
import java.util.*;

import static coyote.mc.snow.Predicate.IS;
import static coyote.mc.snow.Predicate.LIKE;

public class SnowBacklogReader extends WebServiceReader implements FrameReader {

  public static final String PROJECT = "project";
  private static final String CLASSIFICATION = "classification";
  private static final String UNKNOWN = "Unknown";
  private List<DataFrame> stories = null;

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
   * Read in all the data via the web service reader, and replace its read in data with our own, the metrics.
   *
   * @param context the context containing data related to the current transaction.
   * @return the metrics generated from the stories read in from ServiceNow
   */
  @Override
  public DataFrame read(TransactionContext context) {
    if (stories == null) {
      Log.info("Reading all the stories from the backlog...");
      stories = new ArrayList<>();
      for (DataFrame frame = super.read(context); frame != null; frame = super.read(context)) {
        stories.add(frame);
      }
      Log.info("...read in " + stories.size() + " stories...");
      Log.info("...completed reading.");
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
  private List<DataFrame> generateMetrics(List<DataFrame> stories) {
    List<DataFrame> metrics = new ArrayList<>();
    metrics.addAll( generateClassificationCounts(stories));

    return metrics;
  }

  private List<DataFrame> generateClassificationCounts(List<DataFrame> stories) {
    List<DataFrame> metrics = new ArrayList<>();
    Map<String,Integer> counts = new HashMap<>();
    for(DataFrame frame: stories){
      String token = frame.getAsString(CLASSIFICATION);
      if( StringUtil.isNotEmpty(token)){
        String classification = token.toLowerCase();
        if( counts.get(classification) != null){
          counts.put(classification, counts.get(classification)+1);
        } else{
          counts.put(classification,1);
        }
      } else {
        if( counts.get(UNKNOWN) != null){
          counts.put(UNKNOWN, counts.get(UNKNOWN)+1);
        } else{
          counts.put(UNKNOWN,1);
        }
      }
    }

    for(Map.Entry<String,Integer> entry: counts.entrySet()){
      DataFrame metric = new DataFrame();
      metric.set("name",entry.getKey()+"_count");
      metric.set("value",entry.getValue());
      metrics.add(metric);
    }
    return metrics;
  }

}
