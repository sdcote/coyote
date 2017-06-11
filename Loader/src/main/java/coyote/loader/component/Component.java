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
import coyote.loader.cfg.Config;


/**
 * The Component class models a type which can be queried for a variety of 
 * information to aid in the management of a runtime.
 * 
 * <p>Components provide data to the environment in which they run, but do not 
 * expose any methods which allow them to be managed.</p>
 */
public interface Component {

  /**
   * @return the identifier of the application to which this component belongs.
   */
  public String getApplicationId();




  /**
   * Determine the classification of this component.
   * 
   * <p>This method will return a string that represents this components 
   * category for the purposes of reporting. It is expected that the category
   * will follow some standard naming convention useful to the framework.</p>
   * 
   * <p>One such convention may include the identification of applications and
   * infrastructure components. This may include the designation of agents and
   * other specialized components.</p>
   * 
   * @return The category of this component.
   */
  public String getCategory();




  /**
   * Get the reference to the actual configuration object.
   * 
   * <p>This is useful when a managing component wants to access configuration
   * attributes within the component or wants to make a comparison with a newly
   * received configuration and the current configuration to determine if a 
   * restart is necessary.</p> 
   * 
   * @return The configuration object currently set in the component.
   */
  public Config getConfiguration();




  /**
   * 
   * @return a description of this component
   */
  public String getDescription();




  /**
   * @return the unique identifier of this component
   */
  public String getId();




  /**
   * @return the name of this component within this loader.
   */
  public String getName();




  /**
   * Access a brief informational profile for this component instance.
   * 
   * <p>This method is called as a part of the component "query" operation to 
   * ensure the component instance is operational.</p>
   * 
   * @return Small set of attributes describing this component.
   */
  public DataFrame getProfile();




  /**
   * Access to when the component was started.
   * 
   * @return The time when the component was started, 0 if the component is not
   *         yet started.
   */
  public long getStartTime();




  /**
   * Access a detailed status of this component.
   * 
   * <p>This method is called as a part of a Loaders status reporting, the 
   * result of which will be included in the Loaders response.</p>
   * 
   * <p>Care must be taken when calling this method as it may cause the 
   * component to spend significant resources in collecting data to represent 
   * its current operation status.</p>
   * 
   * @return Detailed set of attributes describing the operational state of 
   *         this component.
   */
  public DataFrame getStatus();




  /**
   * 
   * @return The identifier of the system to which this component belongs.
   */
  public String getSystemId();




  /**
   * Return a Config object that can be used as a template for configuring new 
   * or existing instances of this component.
   * 
   * <p>A template is a default configuration for a component. Using this 
   * template, the system can create an instance of a component from the 
   * default constructor and set the configuration with this template and have 
   * a fully operational component set to the defaults.</p>
   *
   * @return a object that can be used as a configuration template.
   */
  public Config getTemplate();




  /**
   * Determine if the Component is active.
   * 
   * <p>This is a way for a component to report to the loader that it should be 
   * terminated and removed from the system. If the component becomes unstable 
   * or unusable, it should set it active flag to false and cease processing. 
   * The loader can then remove the component from the system when it is 
   * convenient.</p>
   * 
   * @return True if the component is active and ready for processing, False if 
   *         it has failed or is otherwise inoperable.
   */
  public boolean isActive();




  /**
   * Indicate if the component is currently enabled.
   * 
   * @return True if the component is eligible for processing, False if 
   *         disabled.
   */
  public boolean isEnabled();




  /**
   * Determines if the component requires a license to operate.
   * 
   * @return True if the component requires a license to operate, false if the
   *         component is unrestricted.
   */
  public boolean isLicensed();




  /**
   * Access this components shared operational context.
   *  
   * @return the shared operational context this component uses to share data, 
   *         may be null.
   */
  public Context getContext();
  
}
