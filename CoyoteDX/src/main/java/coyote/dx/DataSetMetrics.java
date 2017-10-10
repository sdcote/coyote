/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import java.util.ArrayList;
import java.util.List;

import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * Represents a basic structure of a group of records to be used in database
 * operations.
 *
 * <p>Writers (and other components) can use this class to compile data about
 * Data Frames they have observed, compiling a detailed profile about the set
 * of frames.</p>
 */
public class MetricSchema {

  private final List<FieldMetrics> metrics = new ArrayList<FieldMetrics>();

  private long samples = 0;




  /**
   * Clear all the metrics from this schema.
   */
  public void clear() {
    metrics.clear();
    samples = 0;
  }




  /**
   * Return FieldMetrics associated with the named field.
   *
   * <p>If one is not found, one will be created and placed in the cache for
   * later reference. This method never returns null.</p>
   *
   * @param name The name of the field to be represented by the returned metric
   *
   * @return a FieldMetric associated with the named field. Never returns null.
   */
  public FieldMetrics getMetric(final String name) {
    FieldMetrics retval = null;
    if (name != null) {
      for (final FieldMetrics metric : metrics) {
        if (name.equals(metric.getName())) {
          retval = metric;
          break;
        }
      }
    }

    if (retval == null) {
      retval = new FieldMetrics(name);
      metrics.add(retval);
    }

    return retval;
  }




  /**
   * @return the list of field metrics this schema uses to track field metadata
   */
  public List<FieldMetrics> getMetrics() {
    return metrics;
  }




  /**
   * @return the samples
   */
  public long getSampleCount() {
    return samples;
  }




  /**
   * Take a sample.
   *
   * @param frame DataFrame containing the fileds to sample.
   */
  public void sample(final DataFrame frame) {
    if (frame != null) {
      samples++;
      for (final DataField field : frame.getFields()) {
        getMetric(field.getName()).sample(field);
      } // for
    } // null
  }

}
