/*
 * Copyright (c) 2007 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.loader.component;

import coyote.commons.GUID;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.loader.Context;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.thread.ThreadJob;


/**
 * The AbstractComponent class models a base class of logic components which 
 * are created and managed by the Loader.
 */
public abstract class AbstractManagedComponent extends ThreadJob implements ManagedComponent {

  private static final String UNKNOWN = "Unknown";

  protected Config configuration = null;
  protected volatile boolean logging = false;
  protected volatile boolean enabled = true;
  protected volatile boolean licensed = false;
  protected long startTime;
  protected String identifier = new GUID().toString();
  protected String componentName = AbstractManagedComponent.CLASS;
  protected Loader loader = null;
  protected Context context = null;




  /**
   * 
   */
  public AbstractManagedComponent() {
    super();
  }




  /**
   * Configure the component with the given configuration object.
   * 
   * @param config The object containing the configuration attributes.
   */
  @Override
  public void setConfiguration( final Config config ) {
    configuration = config;
  }




  /**
   * @see coyote.loader.component.Component#getApplicationId()
   */
  @Override
  public String getApplicationId() {
    return UNKNOWN;
  }




  /**
   * @see coyote.loader.component.Component#getCategory()
   */
  @Override
  public String getCategory() {
    return UNKNOWN;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getConfiguration()
   */
  @Override
  public Config getConfiguration() {
    return configuration;
  }




  /**
   * @see coyote.loader.component.Component#getDescription()
   */
  @Override
  public String getDescription() {
    return null;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getId()
   */
  @Override
  public String getId() {
    return identifier;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getName()
   */
  @Override
  public String getName() {
    return componentName;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setName(java.lang.String)
   */
  @Override
  public void setName( String name ) {
    componentName = name;
  }




  /**
   * @see coyote.loader.component.Component#getProfile()
   */
  @Override
  public DataFrame getProfile() {
    final DataFrame retval = new DataFrame();
    retval.put( CLASS, CLASS );
    retval.put( "ID", identifier );

    return retval;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getStartTime()
   */
  @Override
  public long getStartTime() {
    return startTime;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#getStatus()
   */
  @Override
  public DataFrame getStatus() {
    return getProfile();
  }




  /**
   * @see coyote.loader.component.Component#getSystemId()
   */
  @Override
  public String getSystemId() {
    return ManagedComponent.CLASS;
  }




  /**
   * Return a Config that can be used as a template for defining instances
   * of this component.
   *
   * @return a configuration that can be used as a template
   * 
   * @see coyote.loader.component.ManagedComponent#getTemplate()
   */
  @Override
  public Config getTemplate() {
    final Config template = new Config();

    try {
      template.setName( ManagedComponent.CLASS );

      // define the slots
      // template.addConfigSlot( new ConfigSlot( LogicComponent.ENABLED_TAG, "Flag indicating the component is enabled to run.", new Boolean( true ) ).toString() );
    } catch ( final Exception ex ) {
      // should always work
    }

    return template;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#isEnabled()
   */
  @Override
  public boolean isEnabled() {
    return enabled;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled( final boolean flag ) {
    enabled = flag;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setId(java.lang.String)
   */
  @Override
  public void setId( final String id ) {
    if ( StringUtil.isNotBlank( id ) ) {
      identifier = id;
    }
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setStartTime(long)
   */
  @Override
  public void setStartTime( final long millis ) {
    startTime = millis;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#isLicensed()
   */
  @Override
  public boolean isLicensed() {
    return false;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#quiesce()
   */
  @Override
  public void quiesce() {}




  /**
   * @see coyote.loader.component.ManagedComponent#shutdown(coyote.dataframe.DataFrame)
   */
  @Override
  public void shutdown( DataFrame params ) {}




  /**
   * @see coyote.loader.component.ManagedComponent#setLoader(coyote.loader.Loader)
   */
  @Override
  public void setLoader( Loader loader ) {
    this.loader = loader;
  }




  /**
   * @see coyote.loader.component.ManagedComponent#setContext(coyote.loader.Context)
   */
  @Override
  public void setContext( Context context ) {
    this.context = context;
  }




  /**
   * @see coyote.loader.component.Component#getContext()
   */
  @Override
  public Context getContext() {
    return context;
  }

}
