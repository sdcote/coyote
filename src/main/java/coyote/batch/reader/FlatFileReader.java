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
package coyote.batch.reader;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.ConfigTag;
import coyote.batch.ConfigurableComponent;
import coyote.batch.FieldDefinition;
import coyote.batch.FrameReader;
import coyote.batch.TransactionContext;
import coyote.batch.TransformContext;
import coyote.commons.FileUtil;
import coyote.commons.LineIterator;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;


/**
 * 
 */
public class FlatFileReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  /** The logger for this class */
  final Logger log = LoggerFactory.getLogger( getClass() );

  /** The list of fields we are to read in the order they are to be read */
  List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  LineIterator lines = null;




  @Override
  public void open( TransformContext context ) {
    super.open( context );

    String source = getString( ConfigTag.SOURCE );
    log.debug( "using a source of {}", source );

    if ( StringUtil.isNotBlank( source ) ) {
      File sourceFile = null;
      URI uri = UriUtil.parse( source );
      if ( uri != null ) {
        sourceFile = UriUtil.getFile( uri );
        if ( sourceFile != null ) {
          log.debug( "Using a source file of " + sourceFile.getAbsolutePath() );
        } else {
          log.warn( "The source '{}' does not represent a file", source );
        }
      } else {
        sourceFile = new File( source );
        log.debug( "Using a source file of " + sourceFile.getAbsolutePath() );
      }

      if ( sourceFile.exists() && sourceFile.canRead() ) {
        lines = FileUtil.lineIterator( sourceFile );
      } else {
        log.error( "Could not read from source: " + sourceFile.getAbsolutePath() );
        context.setError( getClass().getName() + " could not read from source: " + sourceFile.getAbsolutePath() );
      }
    } else {
      log.error( "No source specified" );
      context.setError( getClass().getName() + " could not determine source" );
    }

    // Now setup our field definitions
    DataFrame fieldcfg = getFrame( ConfigTag.FIELDS );
    if ( fieldcfg != null ) {
      boolean trim = false;// flag to trim values 

      for ( DataField field : fieldcfg.getFields() ) {
        try {
          DataFrame fielddef = (DataFrame)field.getObjectValue();

          // determine if values should be trimmed for this field
          try {
            trim = fielddef.getAsBoolean( ConfigTag.TRIM );
          } catch ( Exception e ) {
            trim = false;
          }

          fields.add( new FieldDefinition( field.getName(), fielddef.getAsInt( ConfigTag.START ), fielddef.getAsInt( ConfigTag.LENGTH ), fielddef.getAsString( ConfigTag.TYPE ), fielddef.getAsString( ConfigTag.FORMAT ), trim ) );
        } catch ( Exception e ) {
          context.setError( "Problems loading field definition '" + field.getName() + "' - " + e.getClass().getSimpleName() + " : " + e.getMessage() );
          return;
        }
      }

      log.debug( "There are {} field definitions.", fields.size() );
    } else {
      context.setError( "There are no fields configured in the reader" );
      return;
    }

  }




  /**
   * @see coyote.batch.FrameReader#read(coyote.batch.TransactionContext)
   */
  @Override
  public DataFrame read( TransactionContext context ) {
    DataFrame retval = null;

    try {

      // sometimes there are blank lines in data, keep reading until data is 
      // returned or EOF
      while ( !eof() ) {
        String line = lines.nextLine();
        if ( StringUtil.isNotBlank( line ) ) {
          retval = parse( line );
          break;
        }
      }
    } catch ( Exception e ) {
      context.setError( e.getMessage() );
    }
    
    return retval;
  }




  private DataFrame parse( String line ) {
    DataFrame retval = new DataFrame();
    for ( FieldDefinition def : fields ) {
      retval.add( def.getName(), def.convert( line.substring( def.getStart(), def.getEnd() ) ) );
    }
    return retval;
  }




  @Override
  public boolean eof() {
    return !lines.hasNext();
  }

}
