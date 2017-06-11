/*
 * Copyright (c) 2005 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons;

/**
 * Interface Named
 */
public interface Named {

  /**
   * @return the name of this component instance.
   */
  public abstract String getName();
}