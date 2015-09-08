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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import coyote.batch.listener.AbstractListener;
import coyote.batch.reader.AbstractFrameReader;
import coyote.batch.task.AbstractTransformTask;
import coyote.batch.transform.AbstractFrameTransform;
import coyote.batch.validate.AbstractValidator;
import coyote.batch.writer.AbstractFrameWriter;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;


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
  private static final Logger LOG = LoggerFactory.getLogger( TransformEngineFactory.class );

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
   * <p>This will determine what component to load in the engine and uses
   * {@code ConfigurableComponent} to configure each component.</p>
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

      retval = new DefaultTransformEngine();

      for ( DataField field : frame.getFields() ) {

        if ( ConfigTag.READER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configReader( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid reader configuration section" );
          }
        } else if ( ConfigTag.MAPPER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configMapper( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid mapper configuration section" );
          }
        } else if ( ConfigTag.WRITER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configWriter( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid writer configuration section" );
          }
        } else if ( ConfigTag.DATABASES.equalsIgnoreCase( field.getName() ) || ConfigTag.DATABASE.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configDatabases( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.VALIDATE.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configValidation( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.TRANSFORM.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configTransformer( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.PREPROCESS.equalsIgnoreCase( field.getName() ) || ConfigTag.TASKS.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configPreProcess( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid pre-process configuration section" );
          }
        } else if ( ConfigTag.POSTPROCESS.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configPostProcess( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid post-process configuration section" );
          }
        } else if ( ConfigTag.CONTEXT.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            configContext( (DataFrame)field.getObjectValue(), retval );
          } else {
            LOG.error( "Invalid context configuration section" );
          }
        } else if ( ConfigTag.LISTENER.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            DataFrame cfgFrame = (DataFrame)field.getObjectValue();
            // there can be many listeners
            if ( cfgFrame.isArray() ) {
              for ( DataField cfgfield : cfgFrame.getFields() ) {
                if ( cfgfield.isFrame() ) {
                  configListener( (DataFrame)cfgfield.getObjectValue(), retval );
                } else {
                  LOG.error( "Invalid listener configuration section" );
                }
              }
            } else {
              configListener( cfgFrame, retval );
            }
          } else {
            LOG.error( "Invalid listener configuration section" );
          }
        } else if ( ConfigTag.NAME.equalsIgnoreCase( field.getName() ) ) {
          if ( field.isFrame() ) {
            LOG.error( "Invalid Name value - expecting scalar" );
          } else {
            retval.setName( field.getStringValue() );
          }
        } else {
          LOG.warn( "Unrecognized section: '{}' - ignored", field.getName() );
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
    for ( DataField field : cfg.getFields() ) {
      String className = field.getName();
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = VALIDATOR_PKG + "." + className;
      }

      // All tasks must have an object(frame) as its value.
      if ( field.isFrame() ) {
        DataFrame validatorConfig = (DataFrame)field.getObjectValue();
        Object object = createComponent( className, validatorConfig );
        if ( object != null ) {
          if ( object instanceof FrameValidator ) {
            engine.addValidator( (FrameValidator)object );
            LOG.debug( "Created validator task {} cfg={}", object.getClass().getName(), validatorConfig );
          } else {
            LOG.error( "Specified validator class '{}' was not a frame validator", field.getName() );
          }
        } else {
          LOG.error( "Could not create an instance of the specified validator task '{}'", className );
        }
      } else {
        LOG.error( "Validator task did not contain a configuration, only scalar {}", field.getStringValue() );
      }
    } // for each validator
  }




  private static void configTransformer( DataFrame cfg, TransformEngine engine ) {
    for ( DataField field : cfg.getFields() ) {
      String className = field.getName();
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = TRANSFORM_PKG + "." + className;
      }

      // All tasks must have an object(frame) as its value.
      if ( field.isFrame() ) {
        DataFrame transformerConfig = (DataFrame)field.getObjectValue();
        Object object = createComponent( className, transformerConfig );
        if ( object != null ) {
          if ( object instanceof FrameTransform ) {
            engine.addTransformer( (FrameTransform)object );
            LOG.debug( "Created frame transformer {} cfg={}", object.getClass().getName(), transformerConfig );
          } else {
            LOG.error( "Specified transformer class '{}' was not a frame transformer", field.getName() );
          }
        } else {
          LOG.error( "Could not create an instance of the specified frame transformer '{}'", className );
        }
      } else {
        LOG.error( "Transformer task did not contain a configuration, only scalar {}", field.getStringValue() );
      }
    } // for each transformer
  }




  /**
   * This creates data sources in the engine for components to use
   * 
   * @param cfg
   * @param retval
   */
  private static void configDatabases( DataFrame cfg, TransformEngine engine ) {
    for ( DataField field : cfg.getFields() ) {
      if ( field.isFrame() ) {
        if ( StringUtil.isNotBlank( field.getName() ) ) {
          DataFrame dataSourceCfg = (DataFrame)field.getObjectValue();

          Database store = new Database();
          try {
            store.setConfiguration( dataSourceCfg );
            store.setName( field.getName() );
          } catch ( ConfigurationException e ) {
            LOG.error( "Could not configure database - {} : {}", e.getClass().getSimpleName(), e.getMessage() );
          }

          // Add it to the engine (actually its transform context)
          engine.addDatabase( store );

        } else {
          LOG.error( "Databases must have a unique name" );
        }

      } else {
        LOG.error( "Database did not contain a configuration, only scalar {}", field.getStringValue() );
      }
    } // for each configuration section
  }




  private static void configPreProcess( DataFrame cfg, TransformEngine engine ) {
    for ( DataField field : cfg.getFields() ) {
      String className = field.getName();
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = TASK_PKG + "." + className;
      }

      // All tasks must have an object(frame) as its value.
      if ( field.isFrame() ) {
        DataFrame taskConfig = (DataFrame)field.getObjectValue();
        Object object = createComponent( className, taskConfig );
        if ( object != null ) {
          if ( object instanceof TransformTask ) {
            int seq = engine.addPreProcessTask( (TransformTask)object );
            LOG.debug( "Created preprocess task {} seq={} cfg={}", object.getClass().getName(), seq, cfg );
          } else {
            LOG.error( "Specified pre-process class was not a transform task" );
          }
        } else {
          LOG.error( "Could not create an instance of the specified pre-process task '{}'", className );
        }
      } else {
        LOG.error( "Pre-process task did not contain a configuration, only scalar {}", field.getStringValue() );
      }
    } // for each task
  }




  private static void configPostProcess( DataFrame cfg, TransformEngine engine ) {
    for ( DataField field : cfg.getFields() ) {
      String className = field.getName();
      if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
        className = TASK_PKG + "." + className;
      }

      // All tasks must have an object(frame) as its value.
      if ( field.isFrame() ) {
        DataFrame taskConfig = (DataFrame)field.getObjectValue();
        Object object = createComponent( className, taskConfig );
        if ( object != null ) {
          if ( object instanceof TransformTask ) {
            int seq = engine.addPostProcessTask( (TransformTask)object );
            LOG.debug( "Created postprocess task {} seq={} cfg={}", object.getClass().getName(), seq, cfg );
          } else {
            LOG.error( "Specified post-process class was not a transform task" );
          }
        } else {
          LOG.error( "Could not create an instance of the specified post-process task '{}'", className );
        }
      } else {
        LOG.error( "Post-process task did not contain a configuration, only scalar {}", field.getStringValue() );
      }
    }// for each task 
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
    TransformContext context = engine.getContext();

    if ( context == null ) {
      context = new TransformContext();
      engine.setContext( context );
    }

    for ( DataField field : cfg.getFields() ) {
      if ( !field.isFrame() ) {
        if ( StringUtil.isNotBlank( field.getName() ) && !field.isNull() ) {
          String value = Template.resolve( field.getStringValue(), engine.getSymbolTable() );
          engine.getSymbolTable().put( field.getName(), value );
          context.set( field.getName(), value );
        } //name-value check
      }// if frame
    } // for

  }




  private static void configWriter( DataFrame cfg, TransformEngine engine ) {

    // Make sure the class is fully qualified 
    String className = cfg.getAsString( ConfigTag.CLASS );
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = WRITER_PKG + "." + className;
      cfg.put( ConfigTag.CLASS, className );
    }
    Object object = createComponent( cfg );
    if ( object != null ) {
      if ( object instanceof FrameWriter ) {
        engine.setWriter( (FrameWriter)object );
        LOG.debug( "Created writer {}", object.getClass().getName() );
      } else {
        LOG.error( "Specified class was not a frame writer" );
      }
    } else {
      LOG.error( "Could not create an instance of the specified writer" );
    }
  }




  private static void configMapper( DataFrame cfg, TransformEngine engine ) {

    // If there is no class tag, use the default mapper class
    if ( !cfg.contains( ConfigTag.CLASS ) ) {
      cfg.put( ConfigTag.CLASS, DefaultFrameMapper.class.getCanonicalName() );
    }

    Object object = createComponent( cfg );
    if ( object != null ) {
      if ( object instanceof FrameMapper ) {
        engine.setMapper( (FrameMapper)object );
        LOG.debug( "Created mapper {}", object.getClass().getName() );
      } else {
        LOG.error( "Specified class was not a frame mapper" );
      }
    } else {
      LOG.error( "Could not create an instance of the specified mapper" );
    }
  }




  private static void configReader( DataFrame cfg, TransformEngine engine ) {
    // Make sure the class is fully qualified 
    String className = cfg.getAsString( ConfigTag.CLASS );
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = READER_PKG + "." + className;
      cfg.put( ConfigTag.CLASS, className );
    } else {
      LOG.error( "NO Reader Class: " + cfg.toString() );
    }
    Object object = createComponent( cfg );
    if ( object != null ) {
      if ( object instanceof FrameReader ) {
        engine.setReader( (FrameReader)object );
        LOG.debug( "Created reader {}", object.getClass().getName() );
      } else {
        LOG.error( "Specified class was not a frame reader" );
      }
    } else {
      LOG.error( "Could not create an instance of the specified reader" );
    }

  }




  /**
   * @param cfg
   * @param engine
   */
  private static void configListener( DataFrame cfg, TransformEngine engine ) {
    // Make sure the class is fully qualified 
    String className = cfg.getAsString( ConfigTag.CLASS );
    if ( className != null && StringUtil.countOccurrencesOf( className, "." ) < 1 ) {
      className = LISTENER_PKG + "." + className;
      cfg.put( ConfigTag.CLASS, className );
    } else {
      LOG.error( "NO Listener Class: " + cfg.toString() );
    }
    Object object = createComponent( cfg );
    if ( object != null ) {
      if ( object instanceof ContextListener ) {
        engine.addListener( (ContextListener)object );
        LOG.debug( "Created listener {}", object.getClass().getName() );
      } else {
        LOG.error( "Specified class '{}' was not a context listener", cfg.getAsString( ConfigTag.CLASS ) );
      }
    } else {
      LOG.error( "Could not create an instance of the specified listener" );
    }
  }




  /**
   * Create an instance of the given named class and configure it with the 
   * given dataframe if it is a configurable component.
   * 
   * @param className Fully qualified name of the class to instantiate
   * 
   * @param cfg the configuration to apply to the instance if it is configurable
   * 
   * @return a configured component
   */
  private static Object createComponent( String className, DataFrame cfg ) {
    Object retval = null;
    if ( StringUtil.isNotBlank( className ) ) {

      try {
        Class<?> clazz = Class.forName( className );
        Constructor<?> ctor = clazz.getConstructor();
        Object object = ctor.newInstance();

        if ( cfg != null ) {
          if ( object instanceof ConfigurableComponent ) {
            try {
              ( (ConfigurableComponent)object ).setConfiguration( cfg );
            } catch ( ConfigurationException e ) {
              LOG.error( "Could not configure {} - {} : {}", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
            }
          } else {
            LOG.warn( "Instance of {} is not configurable", className );
          }
        }

        retval = object;
      } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
        LOG.error( "Could not instantiate {} reason: {} - ", className, e.getClass().getName(), e.getMessage() );
      }
    } else {
      LOG.error( "Null or empty class name - cannot create component" );
    }

    return retval;
  }




  /**
   * Create an instance of a class as specified in the CLASS attribute of 
   * the given dataframe and configure it with the given dataframe if it is a 
   * configurable component.
   * 
   * @param cfg the configuration to apply to the instance if it is configurable
   * 
   * @return a configured component
   */
  private static Object createComponent( DataFrame cfg ) {
    Object retval = null;
    if ( cfg != null ) {
      if ( cfg.contains( ConfigTag.CLASS ) ) {
        String className = cfg.getAsString( ConfigTag.CLASS );

        try {
          Class<?> clazz = Class.forName( className );
          Constructor<?> ctor = clazz.getConstructor();
          Object object = ctor.newInstance();

          if ( object instanceof ConfigurableComponent ) {
            try {
              ( (ConfigurableComponent)object ).setConfiguration( cfg );
            } catch ( ConfigurationException e ) {
              LOG.error( "Could not configure {} - {} : {}", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage() );
            }
          } else {
            LOG.warn( "Instance of {} is not configurable", className );
          }
          retval = object;
        } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
          LOG.error( "Could not instantiate {} reason: {} - {}", className, e.getClass().getName(), e.getMessage() );
        }
      } else {
        LOG.error( "Configuration frame did not contain a class name" );
      }
    }

    return retval;
  }

}
