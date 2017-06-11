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

import coyote.dataframe.DataFrame;
import coyote.loader.Context;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;


/**
 * Managed components have their life cycle managed by the loader. 
 * 
 * <p>They are started, stopped, paused, enabled, disabled, and otherwise 
 * managed by the loader.</p>
 */
public interface ManagedComponent extends Component {

  /** Name used in various class identifying locations */
  public static final String CLASS = "LogicComponent";




  /**
   * Configure the component with the given configuration.
   * 
   * @param config The object containing the configuration attributes.
   */
  public void setConfiguration( Config config );




  /**
   * Set the Loader for this component.
   * 
   * @param loader the Loader managing this component.
   */
  public void setLoader( Loader loader );




  /**
   * Perform work related to communicating with the physical device and any 
   * other house keeping required.
   */
  public void doWork();




  /**
   * Gives the component instance a chance to prepare before doing work.
   * 
   * <p>This method is called after {@link #setConfiguration(Config)} and
   * before {@link #doWork()} is called.</p>
   * 
   * <p>A component will have its {@code initialize()} method called to 
   * perform initialization even if the component is not enabled. This is 
   * because it is possible that a component may be disabled and re-enabled 
   * during its operational lifecycle.</p>
   */
  public void initialize();




  /**
   * Inform the component to enter a quiescent state, possibly saving 
   * operational state information as the component will be restarted 
   * presently.
   */
  public void quiesce();




  /**
   * Sets the enabled status of the component.
   * 
   * @param flag True to enable the component for processing, false to disable 
   *        the component.
   */
  public void setEnabled( boolean flag );




  /**
   * Allows and identifier to be set so the component instance can be addressed 
   * in monitoring and management operations.
   * 
   * @param id The identifier to set in the component.
   */
  public void setId( String id );




  /**
   * Allows a logical name to be set so the component instance can be addressed 
   * in monitoring and management operations.
   * 
   * @param name the logical name of this component
   */
  public void setName( String name );




  /**
   * Allows the framework to set the time the component was started.
   *
   * @param millis Epoch time in milliseconds as is reported by 
   *        System.currentTimeMillis()
   */
  public void setStartTime( long millis );




  /**
   * Signal the component to stop processing;
   * 
   * <p>Shut this component down using the given DataFrame as a set of 
   * parameters.</p>
   * 
   * @param params Shutdown arguments, can be null.
   */
  public void shutdown( final DataFrame params );




  /**
   * Creates a thread, runs this job in that thread and exits leaving that
   * thread running.
   *
   * @return the thread in which this managed component is running.
   */
  public Thread daemonize();




  /**
   * Wait for the managed component to go active.
   *
   * @param timeout The number of milliseconds to wait for the main run loop to
   *        be entered.
   */
  public void waitForActive( long timeout );




  /**
   * Allows for the setting of a shared operational context.
   * 
   * <p>The purpose of the context is to allow the component to share data with 
   * other components in an abstract manner.</p>
   * 
   * @param context the shared operational context this component is to use.
   */
  public void setContext( Context context );

}
