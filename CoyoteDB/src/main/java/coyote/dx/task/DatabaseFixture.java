/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import coyote.commons.StringUtil;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransformContext;
import coyote.dx.db.DefaultDatabaseFixture;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;


/**
 * Create a database fixture in the Transform context.
 * 
 * <p>This fixture is accessible by all components in the transform context so
 * it is possible for all components to share a datasource which makes it 
 * simpler to specify a database in one location in contrast with copying a
 * configuration across multiple sections.
 * 
 * <p>Additionally, this task enables the pooling of connections in a fixture
 * by allowing different fixture classes to be created and placed in the 
 * transform context.
 */
public class DatabaseFixture extends AbstractDatabaseFixtureTask {
  private static final String DEFAULT_NAME = "Default";
  private coyote.dx.db.DatabaseFixture fixture = null;
  private String fixtureName = null;




  /**
   * @see coyote.dx.task.AbstractTransformTask#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration(Config cfg) throws ConfigurationException {
    super.setConfiguration(cfg);

    //    if (StringUtil.isBlank(cfg.getString(ConfigTag.CLASS))) {
    //      throw new ConfigurationException("no class defined");
    //    }

    Log.info("Fixture configuration is valid");
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);
    Log.info("Opening database fixture");

    Config cfg = getConfiguration();

    // Determine which fixture to load
    String className = cfg.getString(ConfigTag.CLASS);
    if (StringUtil.isBlank(className)) {
      className = DefaultDatabaseFixture.class.getName();
    } else if (StringUtil.countOccurrencesOf(className, ".") < 1) {
      className = DefaultDatabaseFixture.class.getPackage().getName() + "." + className;
    }
    cfg.put(ConfigTag.CLASS, className);

    // Make sure there is a name
    fixtureName = cfg.getString(ConfigTag.NAME);
    if (StringUtil.isBlank(fixtureName)) {
      fixtureName = DEFAULT_NAME;
      Log.notice("Unnamed database fixture will be bound to the default name of '" + fixtureName + "'");
    }

    // Now create an instance of the fixture and configure it
    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if (object instanceof coyote.dx.db.DatabaseFixture) {
        try {
          fixture = (coyote.dx.db.DatabaseFixture)object;
          fixture.setConfiguration(cfg);
          Log.debug("Created database fixture");
        } catch (Exception e) {
          Log.error("Could not configure database fixture");
        }
      } else {
        Log.warn("Class did not specify a database fixture");
      }
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      Log.error("Could not create an instance of database fixture: " + e.getClass().getName() + " - " + e.getMessage());
    }

  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    // create an instance of the fixture and place it in the context
    Log.info("Setting fixture in context");
    if (fixture != null) {
      getContext().set(fixtureName, fixture);
    } else {
      throw new TaskException("Could not create fixture");
    }
  }




  /**
   * @see coyote.dx.task.AbstractDatabaseFixtureTask#onEnd(coyote.dx.context.OperationalContext)
   */
  @Override
  public void onEnd(OperationalContext context) {
    if (context instanceof TransformContext) {
      Log.info("Closing database fixture");
    }
  }

}
