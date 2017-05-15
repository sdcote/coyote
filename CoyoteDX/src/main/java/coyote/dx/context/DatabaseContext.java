/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.context;

/**
 * This is an operational context backed by a database which allows values to 
 * be persisted on remote systems.
 * 
 * <p>The data in the context can be managed externally without regard to 
 * where the transform is being run, allowing transforms to be run on many 
 * different hosts without having to manage locally persisted context data.
 * 
 * <p>The primary use case is the running of transform jobs in a pool of 
 * distributed instances in the cloud. A particular instance will run one on 
 * one host and run another time on a different host. Another use case is 
 * running jobs in virtual machines with efemeral file systems such as Heroku. 
 * The VM is restarted at least daily with a fresh file system and all local 
 * files are lost. This context allows persistent data to be stored  remotely 
 * so that local file access is not required.
 */
public class DatabaseContext extends TransformContext {

  /**
   * @see coyote.dx.context.TransformContext#open()
   */
  @Override
  public void open() {
    // TODO Auto-generated method stub
    super.open();
  }




  /**
   * Create the tables necessary to store named values for a named job.
   * 
   * <p>The table is simple:<ul>
   * <li>name - name of the transform job/context
   * <li>key - name of the attribute
   * <li>value - value of the attribute
   * <li>type - data type matching data frame field types (e.g. 3=String)
   * </ul>
   * It may be advantagous to create an index on name:key for large systems
   * and those context performing live updates on the table via the context.
   */
  private void createTables() {

  }




  /**
   * Make sure the tables exist.
   */
  private void verifyTables() {

  }




  /**
   * @see coyote.dx.context.TransformContext#close()
   */
  @Override
  public void close() {
    // TODO Auto-generated method stub
    super.close();
  }

}
