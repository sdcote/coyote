/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mc.snow;

/**
 * An instance of SnowKey that can be constructed without validation checks.
 * 
 * <p>This is useful for creating a SnowKey which came from the instance and 
 * should be valid.
 */
public class SnowKeyRaw extends SnowKey {

  public SnowKeyRaw(String key) {
    super.value = key.trim();
  }

}
