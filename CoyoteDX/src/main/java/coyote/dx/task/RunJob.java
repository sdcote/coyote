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
package coyote.dx.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.SystemPropertyUtil;
import coyote.commons.UriUtil;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.TaskException;
import coyote.dx.TransformEngine;
import coyote.dx.TransformEngineFactory;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 *
 */
public class RunJob extends AbstractTransformTask {
  private static final String JSON_EXT = ".json";




  /**
   * Confirm the configuration location
   *
   * @throws TaskException
   */
  private URI confirmConfigurationLocation( final String cfgLoc ) throws TaskException {
    URI cfgUri = null;
    final StringBuffer errMsg = new StringBuffer( LogMsg.createMsg( CDX.MSG, "Task.runjob.confirming_cfg_location", cfgLoc ) + StringUtil.CRLF );

    if ( StringUtil.isNotBlank( cfgLoc ) ) {

      try {
        cfgUri = new URI( cfgLoc );
      } catch ( final URISyntaxException e ) {
        // This can happen when the location is a filename
      }

      if ( ( cfgUri == null ) || StringUtil.isBlank( cfgUri.getScheme() ) ) {

        final File localfile = new File( cfgLoc );
        File alternativeFile = new File( cfgLoc + JSON_EXT );

        if ( localfile != null ) {

          if ( localfile.exists() ) {
            cfgUri = FileUtil.getFileURI( localfile );
          } else {
            if ( !alternativeFile.exists() ) {
              alternativeFile = null;
            }

            errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.no_local_cfg_file", localfile.getAbsolutePath() ) + StringUtil.CRLF );

            if ( !localfile.isAbsolute() ) {

              final String path = System.getProperties().getProperty( Loader.APP_HOME );

              if ( StringUtil.isNotBlank( path ) ) {
                final String appDir = FileUtil.normalizePath( path );
                final File homeDir = new File( appDir );
                final File configDir = new File( homeDir, "cfg" );

                if ( configDir.exists() ) {
                  if ( configDir.isDirectory() ) {
                    final File cfgFile = new File( configDir, cfgLoc );
                    if ( cfgFile.exists() ) {
                      cfgUri = FileUtil.getFileURI( cfgFile );
                    } else {
                      if ( alternativeFile != null ) {
                        cfgUri = FileUtil.getFileURI( alternativeFile );
                      } else {
                        alternativeFile = new File( configDir, cfgLoc + JSON_EXT );
                        if ( alternativeFile.exists() ) {
                          cfgUri = FileUtil.getFileURI( alternativeFile );
                        } else {
                          errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.no_common_cfg_file", cfgFile.getAbsolutePath() ) + StringUtil.CRLF );
                          errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_file_not_found", cfgLoc ) + StringUtil.CRLF );
                          if ( haltOnError ) {
                            throw new TaskException( errMsg.toString() );
                          } else {
                            Log.error( errMsg.toString() );
                            return null;
                          }
                        }
                      }
                    }
                  } else {
                    errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_dir_is_not_directory", appDir ) + StringUtil.CRLF );
                    if ( haltOnError ) {
                      throw new TaskException( errMsg.toString() );
                    } else {
                      Log.error( errMsg.toString() );
                      return null;
                    }
                  }
                } else {
                  errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_dir_does_not_exist", appDir ) + StringUtil.CRLF );
                  if ( haltOnError ) {
                    throw new TaskException( errMsg.toString() );
                  } else {
                    Log.error( errMsg.toString() );
                    return null;
                  }
                }
              } else {
                if ( alternativeFile != null ) {
                  cfgUri = FileUtil.getFileURI( alternativeFile );
                } else {
                  errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_dir_not_provided", Loader.APP_HOME ) + StringUtil.CRLF );
                  if ( haltOnError ) {
                    throw new TaskException( errMsg.toString() );
                  } else {
                    Log.error( errMsg.toString() );
                    return null;
                  }
                }
              } // app.home path exists
            } // localfile is absolute
          } // localfile does not exist
        } //localfile != null
      } // cfguri is not valid

      if ( cfgUri != null ) {
        if ( UriUtil.isFile( cfgUri ) ) {
          final File test = UriUtil.getFile( cfgUri );
          if ( !test.exists() || !test.canRead() ) {
            errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_file_not_readable", test.getAbsolutePath() ) + StringUtil.CRLF );
            if ( haltOnError ) {
              throw new TaskException( errMsg.toString() );
            } else {
              Log.error( errMsg.toString() );
              return null;
            }
          }
          Log.info( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_reading_from_file", test.getAbsolutePath() ) );
        } else {
          Log.info( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_reading_from_network" ) );
        }
      } else {
        errMsg.append( LogMsg.createMsg( CDX.MSG, "Task.runjob.cfg_file_not_found", cfgLoc ) + StringUtil.CRLF );
        if ( haltOnError ) {
          throw new TaskException( errMsg.toString() );
        } else {
          Log.error( errMsg.toString() );
          return null;
        }
      }
    } else {
      System.err.println( LogMsg.createMsg( CDX.MSG, "Task.runjob.no_config_uri_defined" ) );
    }
    return cfgUri;
  }




  /**
  * Loads a configuration file and set of properties from the class path
  *
  * <p>This loads [name].properties as the system properties and [name.json as
  * the engine configuration.
  *
  * @param name the name of the files to use
  *
  * @return The transform engine configured with the requested configuration
  */
  private TransformEngine loadEngine( final String name ) {
    TransformEngine engine = null;
    SystemPropertyUtil.load( name.toLowerCase() );
    final StringBuffer b = new StringBuffer();
    try {
      final BufferedReader reader = new BufferedReader( new InputStreamReader( RunJob.class.getClassLoader().getResourceAsStream( name + ".json" ) ) );
      String line;
      while ( ( line = reader.readLine() ) != null ) {
        b.append( line );
      }
    } catch ( final FileNotFoundException e ) {
      e.printStackTrace();
    } catch ( final IOException e ) {
      e.printStackTrace();
    }
    final String cfgFile = b.toString();
    engine = TransformEngineFactory.getInstance( cfgFile );
    return engine;
  }




  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {

    final String filename = getString( ConfigTag.FILE );
    Log.debug( "Reading configuration file " + filename );
    final URI cfgUri = confirmConfigurationLocation( filename );
    Log.debug( "Calculated URI of " + cfgUri );

    if ( cfgUri != null ) {
      try {
        Config engineConfig = Config.read( cfgUri );
        if ( StringUtil.isBlank( engineConfig.getName() ) ) {
          engineConfig.setName( UriUtil.getBase( cfgUri ) );
        }

        TransformEngine engine = loadEngine( engineConfig.toString() );

        String contextKey = getString( ConfigTag.CONTEXT );
        if ( StringUtil.isBlank( contextKey ) ) {
          contextKey = engine.getName();
        }

        try {
          engine.run();
        } catch ( Throwable t ) {
          String errMsg = "Processing exception running Job: " + t.getMessage();
          if ( haltOnError ) {
            throw new TaskException( errMsg );
          } else {
            Log.error( errMsg );
            return;
          }
        }
        finally {
          getContext().set( contextKey, engine.getContext().toProperties() );
          try {
            engine.close();
          } catch ( Exception ignore ) {}
        }
      } catch ( IOException | ConfigurationException e ) {
        final String errMsg = "Could not read configuration from " + cfgUri + " - " + e.getMessage();
        if ( haltOnError ) {
          throw new TaskException( errMsg );
        } else {
          Log.error( errMsg );
          return;
        }
      }
    } else {
      return;
    }
  }

}
