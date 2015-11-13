/**
 * Components in this package are responsible for transforming a data frame. 
 * 
 * <p>Frame Transformers transform the working frame of a transaction context. 
 * This could be in the manner of adding static field values, adding calculated 
 * values or modifying the existing values of the working frame.</p>
 * 
 * <p>Some transforms are designed to convert specific fields into specific 
 * types. Others will format the data (e.g. converting strings to lower case) 
 * while others may calculate the values of new fields to be added to the frame 
 * and remove the unwanted fields.</p>
 * 
 * <p>Another use of transformers is the summarization and aggregation of 
 * records.  It is possible to define a frame transform which will summarize 
 * data characteristics of a  the frames observed, giving totals of values, 
 * frame counts and possible data profiling (e.g. nulls in fieldX). The results 
 * of such summarization can be accessed in the TransformContext by post 
 * processing tasks or even the context listener.</p> 
 * 
 * <p>This package is a tool kit for data transformation on a field level 
 * within the context of a data frame.</p>
 */
package coyote.batch.transform;