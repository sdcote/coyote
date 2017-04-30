/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx;

/**
 * 
 */
public interface TransformTask extends ConfigurableComponent {

  public void execute() throws TaskException;




  /**
   * @return true if this task is enabled to run, false if the tasks is not to be executed
   */
  public boolean isEnabled();




  /**
   * @param flag true to enable this task, false to prevent it from being executed.
   */
  public void setEnabled( boolean flag );

}
