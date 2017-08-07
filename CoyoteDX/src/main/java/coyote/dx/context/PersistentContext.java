/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import java.text.SimpleDateFormat;
import java.util.Date;

import coyote.commons.DateUtil;
import coyote.dx.CDX;
import coyote.dx.Symbols;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a context which is persisted at the end of a transform and read in 
 * when it is started to persist values in the transform.
 * 
 * <p>An example use case is the sequential numbering of an output file after 
 * each run of a transform. After the transform completes successfully, its 
 * data is persisted to disk so when it initializes the next time, it can 
 * increment values to be used in naming files.</p>
 * 
 * <p>This class is created by the TransformEngineFactory and keys off the 
 * name of the context to determine if it is a regular context or a persistent 
 * context.
 * 
 * <p>Contexts are opened and closed like other components so this component 
 * has the ability to read itself from a file on opening and persist itself to 
 * disk on closing. 
 * 
 * <p>Because Persistent contexts are simple text files, they can be edited 
 * prior to their respective transforms being run
 */
public abstract class PersistentContext extends TransformContext {
  long runcount = 0;
  Date lastRunDate = null;




  @SuppressWarnings("unchecked")
  protected void setPreviousRunDate() {
    Object value = get(Symbols.PREVIOUS_RUN_DATETIME);

    if (value != null) {

      // clear it from the context to reduce confusion
      set(Symbols.PREVIOUS_RUN_DATETIME, null);

      try {
        Date prevrun = null;
        if (value instanceof Date) {
          prevrun = (Date)value;
        } else {
          prevrun = DateUtil.parse(value.toString());
        }

        if (prevrun != null) {
          // Set the previous run date
          set(Symbols.PREVIOUS_RUN_DATE, prevrun);

          // set the new value in the symbol table
          if (this.symbols != null) {
            symbols.put(Symbols.PREVIOUS_RUN_DATE, new SimpleDateFormat(CDX.DEFAULT_DATE_FORMAT).format(prevrun));
            symbols.put(Symbols.PREVIOUS_RUN_TIME, new SimpleDateFormat(CDX.DEFAULT_TIME_FORMAT).format(prevrun));
            symbols.put(Symbols.PREVIOUS_RUN_DATETIME, new SimpleDateFormat(CDX.DEFAULT_DATETIME_FORMAT).format(prevrun));
          }
        } else {
          Log.warn(LogMsg.createMsg(CDX.MSG, "Context.previous_run_date_parsing_error", value, "Unknown Format", "Ignored"));
        }
      } catch (Exception e) {
        Log.warn(LogMsg.createMsg(CDX.MSG, "Context.previous_run_date_parsing_error", value, e.getClass().getSimpleName(), e.getMessage()));
      }
    }
  }




  /**
   * Increments the run counter by 1
   */
  protected void incrementRunCount() {

    // Get the current value
    Object value = get(Symbols.RUN_COUNT);

    if (value != null) {
      // if a number...
      if (value instanceof Number) {
        // set it
        runcount = ((Number)value).longValue();
      } else {
        // try parsing it as a string
        try {
          runcount = Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
          Log.warn("Could not parse '" + Symbols.RUN_COUNT + "'  value [" + value.toString() + "] into a number ");
        } // try
      } // numeric check
    } // !null

    // increment the counter
    runcount++;

    set(Symbols.RUN_COUNT, runcount);

    // set the new value in the symbol table
    if (this.symbols != null) {
      symbols.put(Symbols.RUN_COUNT, runcount);
    }

    Log.debug("Runcount is " + runcount);
  }

}
