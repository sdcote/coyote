/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.db;

import java.sql.Connection;

import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.dx.reader.AbstractFrameReader;
import coyote.loader.log.Log;


/**
 *  
 */
public class DatabaseFixtureCheck extends AbstractFrameReader {
  private int counter = 0;
  private int limit = 1;




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    counter++;
    if (counter >= limit) {
      context.setLastFrame(true);
    }
    return runCheck();
  }




  private DataFrame runCheck() {
    DataFrame retval = new DataFrame();
    boolean tryConnection = false;
    try {
      tryConnection = getConfiguration().getBoolean("Connect");
    } catch (Exception ignore) {
      // no worries
    }

    String name = getConfiguration().getString(ConfigTag.SOURCE);
    if (StringUtil.isNotBlank(name)) {
      Object obj = getContext().get(name);
      if (obj != null) {
        if (obj instanceof coyote.dx.db.DatabaseFixture) {
          coyote.dx.db.DatabaseFixture fixture = (coyote.dx.db.DatabaseFixture)obj;
          fixture.isPooled();
          if (tryConnection) {
            Connection conn = fixture.getConnection();
            if (conn == null) {
              getContext().setError("Connection failed");
            }
          }
        } else {
          getContext().setError("Found different object bound to the context as '" + name + "' - " + obj.getClass().getName());
        }
      } else {
        getContext().setError("Could not find the fixture bound to the context as '" + name + "'");
      }

    } else {
      Log.error("Was not configured with a name to search");
      getContext().setError("The unit test was not configured properly");
    }
    // do our checks here
    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return counter >= limit;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    counter = 0;
    try {
      limit = configuration.getInt(ConfigTag.LIMIT);
    } catch (NumberFormatException e) {
      limit = 1;
    }

  }

}
