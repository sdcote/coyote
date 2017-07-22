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
package coyote.dx.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.Component;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Represents the base call for all the mappers.
 */
public abstract class AbstractFrameMapper extends AbstractConfigurableComponent implements Component {
  /** An insertion ordered list of target fields to be written to the target frame */
  List<SourceToTarget> fields = new ArrayList<SourceToTarget>();




  /**
   * Expects a configuration in the form of "Fields" : { "SourceField" : "TargetField", ... }
   * 
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config cfg ) throws ConfigurationException {
    super.setConfiguration( cfg );

    // Retrieve the "fields" section from the configuration frame
    DataFrame mapFrame = null;
    try {
      if ( cfg.containsIgnoreCase( ConfigTag.FIELDS ) ) {
        DataField field = cfg.getFieldIgnoreCase( ConfigTag.FIELDS );
        if ( field.isFrame() ) {
          mapFrame = (DataFrame)field.getObjectValue();
        } else {
          Log.warn( LogMsg.createMsg( CDX.MSG, "Mapper.invalid_section_in_configuration", ConfigTag.FIELDS ) );
        }
      }
    } catch ( Exception e ) {
      mapFrame = null;
    }

    // If we found the "fields" frame...
    if ( mapFrame != null ) {
      // For each name:value pair, setup a soure:target mapping
      for ( DataField field : mapFrame.getFields() ) {
        if ( StringUtil.isNotBlank( field.getName() ) && field.getValue().length > 0 ) {
          fields.add( new SourceToTarget( field.getName(), field.getObjectValue().toString() ) );
        }
      }
    } else {
      Log.warn( LogMsg.createMsg( CDX.MSG, "Mapper.no_section_in_configuration", ConfigTag.FIELDS ) );
    }

  }




  /**
   * @see coyote.dx.Component#getContext()
   */
  @Override
  public TransformContext getContext() {
    return context;
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    this.context = context;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {

  }

  //

  //

  /**
   * 
   */
  protected class SourceToTarget {
    private final String sourceName;
    private final String targetName;




    public SourceToTarget( String source, String target ) {
      sourceName = source;
      targetName = target;
    }




    /**
     * @return the target field name
     */
    public String getTargetName() {
      return targetName;
    }




    /**
     * @return the source field name
     */
    public String getSourceName() {
      return sourceName;
    }




    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "Mapping: '".concat( sourceName ).concat( "' to '" ).concat( targetName ).concat( "'" );
    }

  }

}
