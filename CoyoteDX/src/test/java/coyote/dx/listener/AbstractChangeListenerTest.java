/*
 * Copyright (c) 2018 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.listener;

import java.util.ArrayList;
import java.util.List;

import coyote.dataframe.DataFrame;
import coyote.dx.AbstractTest;
import coyote.dx.context.ContextListener;
import coyote.dx.context.TransactionContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * 
 */
public class AbstractChangeListenerTest extends AbstractTest {

  protected void loadListener(ContextListener listener, List<DataFrame> frames) {
    for (int x = 0; x < frames.size(); x++) {
      TransactionContext txnCtx = new TransactionContext(getTransformContext());
      txnCtx.setRow(x);
      txnCtx.setTargetFrame(frames.get(x));
      getTransformContext().fireMap(txnCtx);
    }
    getTransformContext().end();
  }




  protected void initListener(ContextListener listener, Config cfg) throws ConfigurationException {
    listener.setConfiguration(cfg);
    getTransformContext().addListener(listener);
    listener.open(getTransformContext());
  }




  protected static List<DataFrame> getSystemMetrics() {
    List<DataFrame> retval = new ArrayList<>();

    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "Billing").set("Memory", 0.3));
    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "Billing").set("Disk", 0.71));
    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "Billing").set("CPU", 0.09));
    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "OrderEntry").set("Memory", 0.65));
    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "OrderEntry").set("Disk", 0.42));
    retval.add(new DataFrame().set("Timestamp", "1518560000").set("System", "OrderEntry").set("CPU", 0.09));

    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "Billing").set("Memory", 0.6));
    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "Billing").set("Disk", 0.71));
    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "Billing").set("CPU", 0.09));
    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "OrderEntry").set("Memory", 0.21));
    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "OrderEntry").set("Disk", 0.42));
    retval.add(new DataFrame().set("Timestamp", "1518560300").set("System", "OrderEntry").set("CPU", 0.09));

    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "Billing").set("Memory", 0.5));
    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "Billing").set("Disk", 0.71));
    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "Billing").set("CPU", 0.85));
    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "OrderEntry").set("Memory", 0.23));
    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "OrderEntry").set("Disk", 0.42));
    retval.add(new DataFrame().set("Timestamp", "1518560600").set("System", "OrderEntry").set("CPU", 0.09));

    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "Billing").set("Memory", 0.8));
    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "Billing").set("Disk", 0.71));
    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "Billing").set("CPU", 0.09));
    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "OrderEntry").set("Memory", 0.22));
    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "OrderEntry").set("Disk", 0.42));
    retval.add(new DataFrame().set("Timestamp", "1518560900").set("System", "OrderEntry").set("CPU", 0.09));

    return retval;
  }

}
