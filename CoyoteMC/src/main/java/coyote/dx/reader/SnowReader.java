/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.reader;

import coyote.commons.StringParseException;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;
import coyote.dx.context.TransformContext;
import coyote.dx.web.ExchangeType;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mc.snow.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static coyote.mc.snow.Predicate.IS;
import static coyote.mc.snow.Predicate.LIKE;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export.
 */
public abstract class SnowReader extends WebServiceReader implements FrameReader {

  public static final String PRODUCT = "product";
  public static final String INSTANCE = "instance";
  public static final String CONFIG_ITEM = "ConfigurationItem";

  SnowFilter filter = null;

  /**
   * @param context The transformation context in which this component should operate
   */
  @Override
  public void open(TransformContext context) {
    if (getConfiguration().getSection(ConfigTag.PROTOCOL) == null) {
      Config protocolSection = new Config();
      protocolSection.set(CWS.EXCHANGE_TYPE, ExchangeType.JSON_HTTP.toString());
      getConfiguration().put(ConfigTag.PROTOCOL, protocolSection);
    }

    super.open(context);

    String source = getString(ConfigTag.SOURCE);
    Log.debug(LogMsg.createMsg(CDX.MSG, "Reader.using_source_uri", source));

    String query = getConfiguration().getString(ConfigTag.FILTER);
    if (StringUtil.isNotBlank(query)) {
      try {
        filter = FilterParser.parse(query);
      } catch (StringParseException e) {
        context.setError("The " + getClass().getSimpleName() + " configuration contained an invalid filter: " + e.getMessage());
        context.setState("Configuration Error");
      }
    }

    if (StringUtil.isEmpty(getString(ConfigTag.SELECTOR))) {
      getConfiguration().set(ConfigTag.SELECTOR, "records.*");
    }
  }

  /**
   * Return all the sprints for a named product.
   *
   * @param product the name of the product to lookup
   * @return all the sprints for that product
   */
  protected List<SnowSprint> getSprints(String product) {
    List<SnowSprint> retval = new ArrayList<>();

    // get all the releases for the product
    SnowFilter query = new SnowFilter("model.name", LIKE, product);

    try {
      getResource().setPath("/m2m_product_release.do?JSONv2&sysparm_query=" + query.toEncodedString());
      List<DataFrame> xref = retrieveData();
      if (xref.size() > 0) {
        query = new SnowFilter();
        for (DataFrame frame : xref) {
          query.or(ServiceNowFields.SYS_ID, IS, frame.getAsString(ServiceNowFields.RELEASE));
        }
        query.and(ServiceNowFields.STATE, IS, "2"); // current
        getResource().setPath("/rm_release_scrum.do?JSONv2&sysparm_query=" + query.toEncodedString());
        List<DataFrame> releases = retrieveData();
        for (DataFrame frame : releases) {
          SnowFilter sprintQuery = new SnowFilter(ServiceNowFields.RELEASE + "." + ServiceNowFields.SYS_ID, IS, frame.getAsString(ServiceNowFields.SYS_ID));
          getResource().setPath("/rm_sprint.do?JSONv2&sysparm_query=" + sprintQuery.toEncodedString());
          List<DataFrame> sprints = retrieveData();
          if (sprints.size() > 0) {
            for (DataFrame sprintFrame : sprints) {
              try {
                retval.add(new SnowSprint(sprintFrame));
              } catch (SnowException e) {
                e.printStackTrace();
              }
            }
          } else {
            Log.warn("Could not find any sprints for release '" + frame.getAsString(ServiceNowFields.NUMBER) + "'");
          }
        } // for each release
      } // xref>0
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return retval;
  }
}
