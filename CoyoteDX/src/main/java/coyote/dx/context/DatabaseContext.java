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

import java.sql.Connection;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dx.ConfigTag;
import coyote.dx.Database;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;


/**
 * This is an operational context backed by a database which allows values to 
 * be persisted on remote systems.
 * 
 * <p>The data in the context can be managed externally without regard to 
 * where the transform is being run, allowing transforms to be run on many 
 * different hosts without having to manage locally persisted context data.
 * 
 * <p>key value pairs specified in the fields, are used to reset the field 
 * values in the context at the start of the job. Their values will be 
 * persisted when the context is closed. Other jobs using the context will 
 * then have access to these values unless the reset them in a similar fashion. 
 * 
 * <p>The primary use case is the running of transform jobs in a pool of 
 * distributed instances in the cloud. A particular instance will run one on 
 * one host and run another time on a different host. Another use case is 
 * running jobs in virtual machines with ephemeral file systems such as Heroku. 
 * The VM is restarted at least daily with a fresh file system and all local 
 * files are lost. This context allows persistent data to be stored remotely 
 * so that local file access is not required.
 * 
 * <p>Unlike a writer, this component deals with fields of a dataframe not the 
 * dataframe itself. Reach field is a record in the table differentiated by 
 * the field name and the name of the job to which it belongs.
 */
public class DatabaseContext extends PersistentContext {

  /** The JDBC connection used by this context to interact with the database */
  protected Connection connection;

  /** Component we use to handle connection creation */
  private Database database = null;
  Connection conn = null;




  /**
   * @see coyote.dx.context.TransformContext#open()
   */
  @Override
  public void open() {

    if ( configuration != null ) {
      
      database = new Database();
      try {
        database.setConfiguration( configuration );
        
        conn = database.getConnection();
        
        
      } catch ( ConfigurationException e ) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Config section = configuration.getSection( ConfigTag.FIELDS );
      for ( DataField field : section.getFields() ) {
        if ( !field.isFrame() ) {
          if ( StringUtil.isNotBlank( field.getName() ) && !field.isNull() ) {
            String token = field.getStringValue();
            String value = Template.resolve( token, engine.getSymbolTable() );
            engine.getSymbolTable().put( field.getName(), value );
            set( field.getName(), value );
          } //name-value check
        } // if frame
      } // for
    }
  }




  /**
   * Create the table necessary to store named values for a named job.
   * 
   * <p>The table is simple:<ul>
   * <li>name - name of the transform job/context
   * <li>key - name of the attribute
   * <li>value - value of the attribute
   * <li>type - data type matching data frame field types (e.g. 3=String)
   * </ul>
   * It may be advantageous to create an index on name:key for large systems
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

    super.close();
  }

}
