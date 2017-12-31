/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameAggregator;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;


/**
 * One of the more generic aggregators, the grouping aggregator groups frames 
 * together based on the value in the key field.
 * 
 * <p>Grouping the fields by key makes certain types of processing easier and 
 * more efficient by grouping related frames together and not having to wait 
 * for all data to be read in before processing begins. For example, if 
 * processing the prices of a product over the last week, it would be easier 
 * if each products price would appear together as a group so when a new 
 * product is observed, the processing of the pervious product can be 
 * concluded and resources returned for subsequent processing and not left 
 * open for the duration of the transfer job.
 * 
 * <p>This Aggregator, like many, will load all data into memory and release 
 * the frames once the transformation context end event is raised. At that 
 * time, the aggregator will cycle through all the unique keys, and emit the 
 * frames with all the frames with the same key value grouped together in the 
 * order they were received.
 * 
 * <p>The aggregator has the ability to limit the number of frames it returns 
 * for each of the groups. This enables the ability to self-maintain the size 
 * of the data sets. For example, if we retrieved the prices of securities 
 * from an exchange for a variety of products over the past 24 hour, this 
 * aggregator can keep all the prices grouped together by the security and 
 * limit the number of price records retained for each security to only 1 hour
 * to ensure we only keep the latest hours price records for each security.
 * 
 * <p>Another key use case for which this aggregator was designed it where
 * readings are taken from devices at regular intervals and appended to a file
 * for intermetiary storage for processing by one or more other jobs. To
 * prevent unbounded growth of the intermediary file, a limit is placed on the
 * number of frames retained for each key. This keeps the sixe of the file
 * deterministic: file size &lt;= number of devices * {@code limit} * the
 * number of samples. Subsequent processing jobs will always get the last
 * {@code limit} number of samples for each device all grouped by device key
 * for simple break processing.
 * 
 * <p>A sample configurate is as follows:<pre>
 * "Aggregator": { "class": "Grouping", "key":"Symbol", "limit": 288, "sort":"ascend" }</pre>
 * The above configuration groups all 
 */
public class Grouping extends AbstractFrameAggregator implements FrameAggregator {

  private enum Sort {
    ASCEND, DESCEND, NONE, ASCEND_CI, DESCEND_CI
  }

  private static final String KEY = "Key";
  private static final String LIMIT = "Limit";
  private static final String SORT = "Sort";
  private static final String ASCEND = "Ascend";
  private static final String DESCEND = "Descent";
  private static final String NONE = "None";
  private static final String ASCEND_CI = "AscendNoCase";
  private static final String DESCEND_CI = "DescendNoCase";

  private final List<String> keys = new ArrayList<>();
  private Sort sort = Sort.NONE;
  private Map<String, List<DataFrame>> dataMap = new HashMap<>();




  /**
   * @see coyote.dx.aggregate.AbstractFrameAggregator#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String sortMode = getString(SORT);
    if (StringUtil.isNotBlank(sortMode)) {
      if (ASCEND.equalsIgnoreCase(sortMode)) {
        sort = Sort.ASCEND;
      } else if (DESCEND.equalsIgnoreCase(sortMode)) {
        sort = Sort.DESCEND;
      } else if (ASCEND_CI.equalsIgnoreCase(sortMode)) {
        sort = Sort.ASCEND_CI;
      } else if (DESCEND_CI.equalsIgnoreCase(sortMode)) {
        sort = Sort.DESCEND_CI;
      } else if (NONE.equalsIgnoreCase(sortMode)) {
        sort = Sort.NONE;
      } else {
        Log.warn("Unrecognized sourt parameter '" + sortMode + "' - no sorting will occur");
        sort = Sort.NONE;
      }
    } else {
      sort = Sort.NONE;
    }

  }




  /**
   * @see coyote.dx.aggregate.AbstractFrameAggregator#aggregate(java.util.List, coyote.dx.context.TransactionContext)
   */
  @Override
  protected List<DataFrame> aggregate(List<DataFrame> frames, TransactionContext txnContext) {
    List<DataFrame> retval = new ArrayList<>();

    for (int x = 0; x < frames.size(); x++) {
      addFrame(frames.get(x));
    }

    if (txnContext.isLastFrame()) {
      retval = compileFrames();
    }

    return retval;
  }




  /**
   * @param dataFrame
   */
  private void addFrame(DataFrame dataFrame) {
    String key = getKey(dataFrame);
    addKey(key);
    addData(key, dataFrame);
  }




  /**
   * Return the value of the key field as a string.
   * 
   * <p>The key field is the field with the name of the correlation key to be 
   * used.
   * 
   * @param frame the frame from which the field is to be retrieved.
   * 
   * @return the string value of the key field or null if the field does not 
   *         exist or there is no key field specified
   */
  private String getKey(DataFrame frame) {
    String retval = null;
    String keyname = getKeyFieldName();
    if (StringUtil.isNotBlank(keyname)) {
      Object obj = frame.get(keyname);
      if (obj != null) {
        retval = obj.toString();
      } else {
        Log.warn("Frame did not contain a field named '" + keyname + "'");
      }
    }
    return retval;
  }




  /**
   * Add the key to the list if it does not exist in the data map.
   * 
   * @param key value to be used as a key
   */
  private void addKey(String key) {
    if (!dataMap.containsKey(key)) {
      keys.add(key);
    }
  }




  /**
   * Add the data to the data map.
   *   
   * @param key the key value to which the dataframe should be mapped
   * @param frame the dataframe to add to the list of other frames.
   */
  private void addData(String key, DataFrame frame) {
    List<DataFrame> list = dataMap.get(key);
    if (list == null) {
      list = new ArrayList<DataFrame>();
      dataMap.put(key, list);
    }
    list.add(frame);

    int limit = getLimit();
    if (limit > 0) {
      int extraCount = list.size() - limit;
      for (int x = 0; x < extraCount; x++) {
        list.remove(0);
      }
    }
  }




  /**
   * @return
   */
  private int getLimit() {
    int retval = 0;
    String value = getString(LIMIT);
    if (StringUtil.isNotBlank(value)) {
      try {
        retval = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        Log.error("Could not parse limit config (" + LIMIT + ") into an integer - value: '" + value + "'");
      }
    }
    return retval;
  }




  /**
   * @return
   */
  private String getKeyFieldName() {
    String retval = null;
    retval = getString(KEY);
    return retval;
  }




  /**
   * @return all the frames collected so far grouped by their key value.
   */
  private List<DataFrame> compileFrames() {
    List<DataFrame> retval = new ArrayList<>();
    sortKeys(sort);

    for (int x = 0; x < keys.size(); x++) {
      String key = keys.get(x);
      List<DataFrame> list = dataMap.get(key);
      retval.addAll(list);
    }

    return retval;
  }




  /**
   * Sort the keys in the order indicated by the mode parameter.
   * 
   * @param mode How to sort the keys
   */
  private void sortKeys(Sort mode) {
    if (mode != Sort.NONE) {
      if (mode == Sort.ASCEND || mode == Sort.DESCEND) {
        Collections.sort(keys);
      } else {
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
      }
      if (mode == Sort.DESCEND || mode == Sort.DESCEND_CI) {
        Collections.reverse(keys);
      }
    }
  }

}
