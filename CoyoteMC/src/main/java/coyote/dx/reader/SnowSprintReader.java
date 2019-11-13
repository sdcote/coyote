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
import coyote.mc.snow.ServiceNowFields;
import coyote.mc.snow.SnowException;
import coyote.mc.snow.SnowFilter;
import coyote.mc.snow.SnowSprint;

import java.net.URISyntaxException;

import static coyote.mc.snow.Predicate.LIKE;

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
 *    "group": "AgileTeam6",
 *    "instance": "Otters",
 *    "authenticator": {
 *      "class" : "BasicAuthentication",
 *      "username": "[#Vault.get(SnowUser,username)#]",
 *      "password": "[#Vault.get(SnowUser,password)#]",
 *      "preemptive" : true
 *    }
 *  },
 * </pre>
 */
public class SnowSprintReader extends SnowReader implements FrameReader {


  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    getResource().getDefaultParameters().setMethod(Method.GET);

    String team = getConfiguration().getString(GROUP);

    if (StringUtil.isBlank(team)) {
      context.setError("The " + getClass().getSimpleName() + " configuration must contain the '" + GROUP + "' element.");
      context.setState("Configuration Error");
      return;
    }

    if (StringUtil.isNotBlank(team)) {
      SnowFilter filter = new SnowFilter(ServiceNowFields.ASSIGNMENT_GROUP, LIKE, team);
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
      super.read(context); // fill the data frames with sprints
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


}
