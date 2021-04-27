/*
 * Copyright (c) 2019 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.FrameReader;

/**
 * This is a reader which connects to a ServiceNow instance and queries data via URL export and generates metrics based
 * on the incidents in the "incident" table.
 */
public abstract class SnowMetricReader extends SnowReader implements FrameReader {

  /**
   * Build a metric frame from the given data
   *
   * @param metricName   the name of the metric
   * @param value        the value of the meric
   * @param helpText     the help text
   * @param type         the type of metric
   * @param instanceName the name of the instance
   * @return a dataframe with the fields populated
   */
  protected DataFrame buildMetric(String metricName, long value, String helpText, String type, String instanceName) {
    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, metricName);
    metric.set(ConfigTag.VALUE, value);
    metric.set(ConfigTag.HELP, helpText);
    metric.set(ConfigTag.TYPE, type);
    metric.set(INSTANCE, instanceName);
    return metric;
  }

  protected DataFrame buildMetric(String metricName, float value, String helpText, String type, String instanceName) {
    DataFrame metric = new DataFrame();
    metric.set(ConfigTag.NAME, metricName);
    metric.set(ConfigTag.VALUE, value);
    metric.set(ConfigTag.HELP, helpText);
    metric.set(ConfigTag.TYPE, type);
    metric.set(INSTANCE, instanceName);
    return metric;
  }

}
