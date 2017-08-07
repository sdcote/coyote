/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.task;

import coyote.dx.TaskException;


/**
 * 
 */
public class MockTask extends AbstractTransformTask {
  private volatile boolean executed = false;




  public boolean executed() {
    return executed;
  }




  @Override
  protected void performTask() throws TaskException {
    executed = true;
  }

}
