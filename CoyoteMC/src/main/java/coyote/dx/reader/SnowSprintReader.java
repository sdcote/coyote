/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.commons.network.http.Method;
import coyote.dataframe.DataFrame;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.log.Log;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowFilter;
import coyote.mc.snow.SnowSprint;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static coyote.mc.snow.Predicate.IS;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export to retrieve sprints for a
 * project.
 *
 * <p>The primary use case to date is to create a local cache of sprint records which other components can read as part
 * of a pre-processing task to set the start and end dates for the current and previous sprints. For example, a job can
 * be created to read in all the sprints, filter out all but the current sprint and use the {@code }PropertyWriter} to
 * create a property file with other jobs can read in with the {@code ReadIntoContext} task to set a context variable
 * to the start date of the current sprint. This way the other jobs can generate metrics for the current sprint as they
 * will know when the start date is. When run on a daily basis, other metric collectors will have the latest start date
 * for for any sprint and any time.</p>
 *
 * <p>An example configuration is as follows:<pre>
 * "Reader" : {
 *    "class" : "SnowSprintReader",
 *    "source" : "https://servicenow.aepsc.com/",
 *    "release": "RLSE0010497",
 *    "instance": "EAF",
 *    "authenticator": {
 *      "class" : "BasicAuthentication",
 *      "ENC:username" : "qmV4DbVRCDasdcAGYBWXvYrFrtQn/QXi",
 *      "ENC:password" : "/0zSb2K123nkHYckRv5kh0UO/dcwfqnl",
 *      "preemptive" : true
 *    }
 *  },
 * </pre>
 *
 * <p>The {@code release} configuration element is used to more efficiently find the sprints. A more complete way is to
 * replace the {@code release} element with the {@code product} element. This will scan for all releases related to the
 * product and then scan for all the sprints for all those releases. Note you cannot have both and at lease one of them
 * must be specified.</p>
 */
public class SnowSprintReader extends SnowReader implements FrameReader {


  private static final String RELEASE = "release";

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    getResource().getDefaultParameters().setMethod(Method.GET);

    String product = getConfiguration().getString(PRODUCT);
    String release = getConfiguration().getString(RELEASE);

    if (StringUtil.isBlank(release) && StringUtil.isBlank(product)) {
      context.setError("The " + getClass().getSimpleName() + " configuration did not contain the '" + PRODUCT + "' nor '" + RELEASE + "' element.");
      context.setState("Configuration Error");
      return;
    }
    if (StringUtil.isNotBlank(release) && StringUtil.isNotBlank(product)) {
      context.setError("The " + getClass().getSimpleName() + " configuration cannot contain both '" + PRODUCT + "' and '" + RELEASE + "' elements.");
      context.setState("Configuration Error");
      return;
    }


    if (StringUtil.isNotBlank(release)) {
      SnowFilter filter = new SnowFilter("release.number", IS, release);
      try {
        getResource().setPath("rm_sprint.do?JSONv2&sysparm_query=" + filter.toEncodedString());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

  }


  @Override
  public DataFrame read(TransactionContext context) {
// if this is the first time through and we are supposed to search by product...
    if (dataframes == null && StringUtil.isNotBlank(getConfiguration().getString(PRODUCT))) {
      dataframes = getData(); // fill the dataframes with sprints
    }


    DataFrame retval = super.read(context);
    if (retval != null) {
      try {
        SnowSprint sprint = new SnowSprint(retval);
        if (sprint.isCurrent())
          Log.info("Sprint: " + sprint.getShortDescription() + " (" + sprint.getNumber() + ") is the current sprint");
      } catch (SnowException e) {
        Log.error("Could not parse retrieved data into a SnowSprint: " + e.getLocalizedMessage());
      }
    }
    return retval;
  }

  private List<DataFrame> getData() {
    List<DataFrame> retval = new ArrayList<>();
    List<SnowSprint> sprints = getSprints(getConfiguration().getString(PRODUCT));
    for (SnowSprint sprint : sprints) {
      retval.add(sprint);
    }
    return retval;
  }

}
