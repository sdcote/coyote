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

import java.io.File;
import java.util.List;

import coyote.batch.filter.AbstractFrameFilter;
import coyote.batch.listener.AbstractListener;
import coyote.batch.mapper.AbstractFrameMapper;
import coyote.batch.mapper.DefaultFrameMapper;
import coyote.batch.reader.AbstractFrameReader;
import coyote.batch.task.AbstractTransformTask;
import coyote.batch.transform.AbstractFrameTransform;
import coyote.batch.validate.AbstractValidator;
import coyote.batch.writer.AbstractFrameWriter;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This factory uses a JSON string to create and configure the proper instance 
 * of a Transform engine.
 * 
 * <p>The returned engine must be opened and closed by the caller.</p>
 * 
 * <p>Once opened, the caller can invoke {@code run()} to run the engine, 
 * transforming all data in the source.</p>
 */
public class TransformEngineFactory {

  /** Constant to assist in determining the full class name of readers */
  private static final String READER_PKG = AbstractFrameReader.class.getPackage().getName();
  /** Constant to assist in determining the full class name of writers */
  private static final String WRITER_PKG = AbstractFrameWriter.class.getPackage().getName();
  /** Constant to assist in determining the full class name of listeners */
  private static final String LISTENER_PKG = AbstractListener.class.getPackage().getName();
  /** Constant to assist in determining the full class name of tasks */
  private static final String TASK_PKG = AbstractTransformTask.class.getPackage().getName();
  /** Constant to assist in determining the full class name of validators */
  private static final String VALIDATOR_PKG = AbstractValidator.class.getPackage().getName();
  /** Constant to assist in determining the full class name of frame transformers */
  private static final String TRANSFORM_PKG = AbstractFrameTransform.class.getPackage().getName();
  /** Constant to assist in determining the full class name of frame mappers */
  private static final String MAPPER_PKG = AbstractFrameMapper.class.getPackage().getName();
  /** Constant to assist in determining the full class name of frame filters */
  private static final String FILTER_PKG = AbstractFrameFilter.class.getPackage().getName();




  /**
   * Read a JSON string in from a file and create a transformation engine to 
   * the specifications in the file.
   * 
   * @param cfgFile File containing the JSON configuration
   * 
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance( File cfgFile ) {
    String configuration = FileUtil.fileToString( cfgFile );
    return getInstance( configuration );
  }




  /**
   * Create a Transformation engine configured to the specification provided in 
   * the given configuration string.
   * 
   * @param cfg The JSON string specifying the configuration
   * 
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance( String cfg ) {
    TransformEngine retval = null;

    List<DataFrame> config = JSONMarshaler.marshal( cfg );

    if ( config != null && config.size() > 0 ) {
      DataFrame frame = config.get( 0 );

      retval = getInstance( frame );

    }

    return retval;
  }




  /**
   * Create a Transformation engine configured to the specification provided in 
   * the given configuration frame.
   * 
   * <p>This will determine what component to load in the engine and uses
   * {@code ConfigurableComponent} to configure each component.</p>
   * 
   * @param frame The DataFrame containing the configuration
   * 
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance( DataFrame frame ) {
    TransformEngine retval = null;

    if ( frame != null ) {

      retval = new DefaultTransformEngine();

      for ( DataField field : frame.getFields() ) {

        if ( ConfigTag.READER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configReader( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid reader configuration section" );
          }
        } else if ( ConfigTag.FILTERS.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configFilters( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid filters configuration section" );
          }
        } else if ( ConfigTag.MAPPER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configMapper( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid mapper configuration section" );
          }
        } else if ( ConfigTag.WRITER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configWriter( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid writer configuration section" );
          }
        } else if ( ConfigTag.DATABASES.equalsIgnoreCase( field.getName() ) || ConfigTag.DATABASE.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configDatabases( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid database configuration section" );
          }
        } else if ( ConfigTag.VALIDATE.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configValidation( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.TRANSFORM.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configTransformer( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.PREPROCESS.equalsIgnoreCase( field.getName() ) || ConfigTag.TASKS.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configPreProcess( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.POSTPROCESS.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configPostProcess( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid post-process configuration section" );
          }
        } else if ( ConfigTag.CONTEXT.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configContext( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid context configuration section" );
          }
        } else if ( ConfigTag.PERSISTENT_CONTEXT.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configPersistentContext( (DataFrame)field.getObjectValue(), retval );
          } else {
            Log.error( "Invalid context configuration section" );
          }
        } else if ( ConfigTag.LOGGERS.equalsIgnoreCase( field.getName() ) ) {

          // logging should be a section, not a scalar value
          if ( field.isFrame() ) {

            // create a new log manager
            LogManager logmgr = new LogManager();

            // Configure it with the logging section
            try {
              logmgr.setConfiguration( (DataFrame)field.getObjectValue() );
            } catch ( ConfigurationException e ) {
              Log.error( "Invalid logging configuration", e );
            }

            // Set the log manager in the engine
            retval.setLogManager( logmgr );
          } else {
            Log.error( "Invalid logging configuration section" );
          }
        } else if ( ConfigTag.LISTENER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            DataFrame cfgFrame = (DataFrame)field.getObjectValue();
            if ( cfgFrame != null ) {
              // there can be many listeners
              if ( cfgFrame.isArray() ) {
                for ( DataField cfgfield : cfgFrame.getFields() ) {
                  if ( cfgfield.isFrame() ) {
                    configListener( (DataFrame)cfgfield.getObjectValue(), retval );
                  } else {
                    Log.error( "Invalid listener configuration section" );
                  }
                }
              } else {
                configListener( cfgFrame, retval );
              }
            } // null / empty check
          } else {
            Log.error( "Invalid listener configuration section" );
          }
        } else if ( ConfigTag.NAME.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            Log.error( "Invalid Name value - expecting scalar" );
          } else {
            retval.setName( field.getStringValue() );
          }
        } else {
          Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.unrecognized_configuration_section", field.getName() ) );
        }

      }
    }

    return retval;
  }




  /**
   * Get a 
   * @param cfg
   * @param engine
   */
  private static void configValidation( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = VALIDATOR_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame validatorConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, validatorConfig );
          if ( object != null ) {
            if ( object instanceof FrameValidator ) {
              engine.addValidator( (FrameValidator)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.Created validator task {} cfg={}", object.getClass().getName(), validatorConfig ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Specified validator class '{}' was not a frame validator", field.getName() ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not create an instance of the specified validator task '{}'", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Validator task did not contain a configuration, only scalar {}", field.getStringValue() ) );
        }
      } // for each validator
    } // cfg !null
  }




  private static void configTransformer( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = TRANSFORM_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame transformerConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, transformerConfig );
          if ( object != null ) {
            if ( object instanceof FrameTransform ) {
              engine.addTransformer( (FrameTransform)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_frame_transformer", object.getClass().getName(), transformerConfig ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.specified_class_was_not_a_transformer", field.getName() ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_an_instance_of_specified_transformer", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.transformer_task_did_not_contain_valid_configuration", field.getStringValue() ) );
        }
      } // for each transformer
    } // cfg !null
  }




  /**
   * This creates data sources in the engine for components to use
   * 
   * @param cfg
   * @param retval
   */
  private static void configDatabases( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        if ( field.isFrame() ) {
          if ( StringUtil.isNotBlank( field.getName() ) ) {
            DataFrame dataSourceCfg = (DataFrame)field.getObjectValue();

            Database store = new Database();
            try {
              store.setConfiguration( dataSourceCfg );
              store.setName( field.getName() );
            } catch ( ConfigurationException e ) {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not configure database - {} : {}", e.getClass().getSimpleName(), e.getMessage() ) );
            }

            // Add it to the engine (actually its transform context)
            engine.addDatabase( store );

          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Databases must have a unique name" ) );
          }

        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Database did not contain a configuration, only scalar {}", field.getStringValue() ) );
        }
      } // for each configuration section
    } // cfg !null
  }




  private static void configFilters( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = FILTER_PKG + "." + className;
        }

        // All filters must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame taskConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, taskConfig );
          if ( object != null ) {
            if ( object instanceof FrameFilter ) {
              int seq = engine.addFilter( (FrameFilter)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.Created filter task {} seq={} cfg={}", object.getClass().getName(), seq, cfg ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Specified filter class was not a frame filter" ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not create an instance of the specified filter '{}'", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.filter did not contain a configuration, only scalar {}", field.getStringValue() ) );
        }
      } // for each task
    } // cfg !null
  }




  private static void configPreProcess( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = TASK_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame taskConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, taskConfig );
          if ( object != null ) {
            if ( object instanceof TransformTask ) {
              int seq = engine.addPreProcessTask( (TransformTask)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_preprocess_task", object.getClass().getName(), seq, cfg ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.preprocess_class_not_transform_task", object.getClass().getName() ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_preprocess_task", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.preprocess_task_config_not_section", field.getStringValue() ) );
        }
      } // for each task
    } // cfg !null
  }




  private static void configPostProcess( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = TASK_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame taskConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, taskConfig );
          if ( object != null ) {
            if ( object instanceof TransformTask ) {
              int seq = engine.addPostProcessTask( (TransformTask)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.Created postprocess task {} seq={} cfg={}", object.getClass().getName(), seq, cfg ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Specified post-process class was not a transform task" ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Could not create an instance of the specified post-process task '{}'", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.Post-process task did not contain a configuration, only scalar {}", field.getStringValue() ) );
        }
      }// for each task 
    } // cfg !null
  }




  /**
   * This section of the configuration should be simply name-value pairs.
   * 
   * <p>The values are treated as templates and parsed into their final values
   * based on the contents of the symbol table and the systems properties 
   * contained therein.</p>
   * 
   * <p>The name value pairs are placed in the symbol table as well as the 
   * context so that results of processing can be used in subsequent 
   * templates.</p>
   * 
   * @param cfg the configuration frame
   * @param engine the transform engine
   */
  private static void configContext( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      TransformContext context = engine.getContext();

      if ( context == null ) {
        context = new TransformContext();

        // Set the configuration so it can be used when the context is opened
        context.setConfiguration( cfg );

        // set the context in the engine
        engine.setContext( context );

        // set the engine in the context
        context.setEngine( engine );
        Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_context", context.getClass().getName() ) );
      }

      Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.loaded_context", context.getClass().getName() ) );
    } // cfg !null
  }




  /**
   * This section of the configuration should be simply name-value pairs.
   * 
   * <p>This section describes a context which is persisted between runs. When 
   * the context is opened, its data is read in from a file. When the context 
   * is closed, it is written to a file.</p>
   * 
   * <p>The values are treated as templates and parsed into their final values
   * based on the contents of the symbol table and the systems properties 
   * contained therein.</p>
   * 
   * <p>The name value pairs are placed in the symbol table as well as the 
   * context so that results of processing can be used in subsequent 
   * templates.</p>
   * 
   * @param cfg the configuration frame
   * @param engine the transform engine
   */
  private static void configPersistentContext( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      TransformContext context = engine.getContext();

      if ( context == null ) {
        if ( engine.getName() != null ) {
          context = new PersistentContext();
          context.setConfiguration( cfg );

          // set the context in the engine
          engine.setContext( context );

          // set the engine in the context
          context.setEngine( engine );

          Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_persistent_context", context.getClass().getName() ) );
        } else {
          Log.warn( LogMsg.createMsg( Batch.MSG, "EngineFactory.unnamed_engine_configuration" ) );
          configContext( cfg, engine );
          return;
        }
      } else {
        // TODO: support converting a regular context into a persistent one
        Log.warn( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_replace_existing_context" ) );
      }
    } // cfg !null
  }




  private static void configWriter( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      // Make sure the class is fully qualified 
      String className = cfg.getAsString( ConfigTag.CLASS );
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = WRITER_PKG + "." + className;
        cfg.put( ConfigTag.CLASS, className );
      }
      Object object = Batch.createComponent( cfg );
      if ( object != null ) {
        if ( object instanceof FrameWriter ) {
          engine.addWriter( (FrameWriter)object );
          Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_writer", object.getClass().getName() ) );
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.specified_class_is_not_a_writer", object.getClass().getName() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_instance_of_specified_writer", className ) );
      }
    } // cfg !null
  }




  private static void configMapper( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      String className = cfg.getAsString( ConfigTag.CLASS );

      // If there is no class tag, use the default mapper class
      if ( className == null ) {
        cfg.put( ConfigTag.CLASS, DefaultFrameMapper.class.getCanonicalName() );
      } else {
        // make sure the class name is fully qualified
        if ( StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = MAPPER_PKG + "." + className;
          cfg.put( ConfigTag.CLASS, className );
        }
      }

      Object object = Batch.createComponent( cfg );
      if ( object != null ) {
        if ( object instanceof FrameMapper ) {
          engine.setMapper( (FrameMapper)object );
          Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_mapper", object.getClass().getName() ) );
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.specified_class_not_framemapper",object.getClass().getName() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_specified_mapper",className ) );
      }
    } // cfg !null
  }




  private static void configReader( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      // Make sure the class is fully qualified 
      String className = cfg.getAsString( ConfigTag.CLASS );
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = READER_PKG + "." + className;
        cfg.put( ConfigTag.CLASS, className );
      } else {
        Log.error( "NO Reader Class: " + cfg.toString() );
      }
      Object object = Batch.createComponent( cfg );
      if ( object != null ) {
        if ( object instanceof FrameReader ) {
          engine.setReader( (FrameReader)object );
          Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_reader", object.getClass().getName() ) );
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.specified_class_is_not_a_reader", object.getClass().getName() ) );
        }
      } else {
        Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_instance_of_specified_reader", className ) );
      }
    } // cfg !null
  }




  /**
   * Configure the all the listeners.
   * 
   * <p>This method expects listeners to be formatted in a manner similar to 
   * the following:<pre>"Listeners": {
   *   "DataProfiler": { "target": "dataprofile.txt" }
   * }</pre>
   * 
   * @param cfg The entire data frame containing the listener configurations
   * @param engine The engine to which the configured listeners will be added.
   */
  private static void configListener( DataFrame cfg, TransformEngine engine ) {
    if ( cfg != null ) {
      for ( DataField field : cfg.getFields() ) {
        String className = field.getName();
        if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
          className = LISTENER_PKG + "." + className;
        }

        // All listeners must have an object(frame) as its value.
        if ( field.isFrame() ) {
          DataFrame listenerConfig = (DataFrame)field.getObjectValue();
          Object object = Batch.createComponent( className, listenerConfig );
          if ( object != null ) {
            if ( object instanceof ContextListener ) {
              engine.addListener( (ContextListener)object );
              Log.debug( LogMsg.createMsg( Batch.MSG, "EngineFactory.created_listener", object.getClass().getName() ) );
            } else {
              Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.specified_class_is_not_a_listener", object.getClass().getName() ) );
            }
          } else {
            Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.could_not_create_instance_of_specified_listener", className ) );
          }
        } else {
          Log.error( LogMsg.createMsg( Batch.MSG, "EngineFactory.listener_configuration_invalid", field.getStringValue() ) );
        }
      }// for each listener 
    } // cfg !null
  }

}
