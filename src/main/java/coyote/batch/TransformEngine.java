/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

import coyote.commons.template.SymbolTable;


/**
 *  
 */
public interface TransformEngine extends RunnableComponent {

  /**
   * @param object
   */
  public void addListener( ContextListener object );




  @Override
  public TransformContext getContext();




  public FrameMapper getMapper();




  public FrameReader getReader();




  public FrameWriter getWriter();




  public void setContext( TransformContext context );




  public void setMapper( FrameMapper mapper );




  public void setReader( FrameReader reader );




  public void setWriter( FrameWriter writer );




  /**
   * @return the name of the transform engine
   */
  public String getName();




  /**
   * @param name the transform engine name to set
   */
  public void setName( String name );




  /**
   * @return the symbol table the engine uses for template lookups
   */
  public SymbolTable getSymbolTable();




  /**
   * Add the task to an ordered list of tasks to be run before the transform is 
   * run.
   * 
   * <p>Tasks will be executed in the order they were added to the engine.</p>
   * 
   * @param task the task to add
   */
  public int addPreProcessTask( TransformTask task );




  /**
   * Add the task to an ordered list of tasks to be run after the transform is 
   * run.
   * 
   * <p>Tasks will be executed in the order they were added to the engine.</p>
   * 
   * @param task the task to add
   */
  public int addPostProcessTask( TransformTask task );




  public void addDataStore( DataStore store );

}
