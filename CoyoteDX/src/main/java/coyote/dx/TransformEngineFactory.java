/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.aggregate.AbstractFrameAggregator;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.context.TransformContext;
import coyote.dx.filter.AbstractFrameFilter;
import coyote.dx.listener.AbstractListener;
import coyote.dx.mapper.AbstractFrameMapper;
import coyote.dx.mapper.DefaultFrameMapper;
import coyote.dx.reader.AbstractFrameReader;
import coyote.dx.task.AbstractTransformTask;
import coyote.dx.transform.AbstractFrameTransform;
import coyote.dx.validate.AbstractValidator;
import coyote.dx.vault.*;
import coyote.dx.writer.AbstractFrameWriter;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static coyote.dx.ConfigTag.SOURCE;
import static javax.xml.transform.OutputKeys.METHOD;


/**
 * This factory uses a JSON string to create and configure the proper instance of a Transform engine.
 *
 * <p>The returned engine must be opened and closed by the caller.</p>
 *
 * <p>Once opened, the caller can invoke {@code run()} to run the engine, transforming all data in the source.</p>
 */
public class TransformEngineFactory {

  /**
   * Constant to assist in determining the full class name of readers
   */
  private static final String READER_PKG = AbstractFrameReader.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of writers
   */
  private static final String WRITER_PKG = AbstractFrameWriter.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of listeners
   */
  private static final String LISTENER_PKG = AbstractListener.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of tasks
   */
  private static final String TASK_PKG = AbstractTransformTask.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of validators
   */
  private static final String VALIDATOR_PKG = AbstractValidator.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of frame transformers
   */
  private static final String TRANSFORM_PKG = AbstractFrameTransform.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of frame mappers
   */
  private static final String MAPPER_PKG = AbstractFrameMapper.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of frame filters
   */
  private static final String FILTER_PKG = AbstractFrameFilter.class.getPackage().getName();
  /**
   * Constant to assist in determining the full class name of frame aggregators
   */
  private static final String AGGREGATOR_PKG = AbstractFrameAggregator.class.getPackage().getName();

  private static final String LOCAL = "Local";
  private static final String FILE = "File";


  /**
   * Read a JSON string in from a file and create a transformation engine to the specifications in the file.
   *
   * @param cfgFile File containing the JSON configuration
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance(File cfgFile) {
    String configuration = FileUtil.fileToString(cfgFile);
    return getInstance(configuration);
  }


  /**
   * Create a Transformation engine configured to the specification provided in the given configuration string.
   *
   * @param cfg The JSON string specifying the configuration
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance(String cfg) {
    TransformEngine retval = null;

    List<DataFrame> config = JSONMarshaler.marshal(cfg);

    if (config != null && config.size() > 0) {
      DataFrame frame = config.get(0);

      retval = getInstance(frame);

    }

    return retval;
  }


  /**
   * Create a Transformation engine configured to the specification provided in the given configuration frame.
   *
   * <p>This will determine what component to load in the engine and uses {@code ConfigurableComponent} to configure
   * each component.</p>
   *
   * @param frame The DataFrame containing the configuration
   * @return an engine ready to run the transformation
   */
  public static TransformEngine getInstance(DataFrame frame) {
    if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Engine Factory " + CDX.VERSION + " creating engine instance.");
    TransformEngine retval = null;

    if (frame != null) {

      retval = new DefaultTransformEngine();

      for (DataField field : frame.getFields()) {

        if (StringUtil.equalsIgnoreCase(ConfigTag.READER, field.getName())) {
          if (field.isFrame()) {
            configReader((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid reader configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.VAULT, field.getName())) {
          if (field.isFrame()) {
            configVault((DataFrame) field.getObjectValue());
          } else {
            Log.error("Invalid vault configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.PRELOAD, field.getName())) {
          if (field.isFrame()) {
            configPreloader((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid filters configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.FILTER, field.getName())) {
          if (field.isFrame()) {
            configFilters((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid filters configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.MAPPER, field.getName())) {
          if (field.isFrame()) {
            configMapper((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid mapper configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.AGGREGATOR, field.getName())) {
          if (field.isFrame()) {
            configAggregator((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid aggregator configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.WRITER, field.getName())) {
          if (field.isFrame()) {
            configWriter((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid writer configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.VALIDATE, field.getName())) {
          if (field.isFrame()) {
            configValidation((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid pre-process configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.TRANSFORM, field.getName())) {
          if (field.isFrame()) {
            configTransformer((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid pre-process configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.PREPROCESS, field.getName()) || StringUtil.equalsIgnoreCase(ConfigTag.TASK, field.getName())) {
          if (field.isFrame()) {
            configPreProcess((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid pre-process configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.POSTPROCESS, field.getName())) {
          if (field.isFrame()) {
            configPostProcess((DataFrame) field.getObjectValue(), retval);
          } else {
            Log.error("Invalid post-process configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.CONTEXT, field.getName())) {
          if (field.isFrame()) {
            configContext(new Config((DataFrame) field.getObjectValue()), retval);
          } else {
            Log.error("Invalid context configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.LOGGING, field.getName())) {
          if (field.isFrame()) {
            configJobLogging(new Config((DataFrame) field.getObjectValue()), retval);
          } else {
            Log.error("Invalid logging configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.CLASS, field.getName())) {
          // ignore the CLASS field...it is used by the Loader, but not by us.
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.LISTENER, field.getName())) {
          if (field.isFrame()) {
            DataFrame cfgFrame = (DataFrame) field.getObjectValue();
            if (cfgFrame != null) {
              // there can be many listeners
              if (cfgFrame.isArray()) {
                for (DataField cfgfield : cfgFrame.getFields()) {
                  if (cfgfield.isFrame()) {
                    configListener((DataFrame) cfgfield.getObjectValue(), retval);
                  } else {
                    Log.error("Invalid listener configuration section");
                  }
                }
              } else {
                configListener(cfgFrame, retval);
              }
            } // null / empty check
          } else {
            Log.error("Invalid listener configuration section");
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.NAME, field.getName())) {
          if (field.isFrame()) {
            Log.error("Invalid Name value - expecting simple type (string)");
          } else {
            retval.setName(field.getStringValue());
          }
        } else if (StringUtil.equalsIgnoreCase(ConfigTag.SCHEDULE, field.getName())) {
          if (!field.isFrame()) {
            Log.error("Invalid Schedule section - expecting complex type");
          }
        } else {
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.unrecognized_configuration_section", field.getName()));
        }
      }
    }
    return retval;
  }


  /**
   * Create a vault and place it in the Template fixture.
   *
   * @param cfg the configuration for the vault
   */
  private static void configVault(DataFrame cfg) {
    VaultBuilder builder = new VaultBuilder();
    for (DataField field : cfg.getFields()) {
      if (ConfigTag.PROVIDER.equalsIgnoreCase(field.getName())) {
        builder.setProvider(field.getStringValue());
      } else if (ConfigTag.METHOD.equalsIgnoreCase(field.getName())) {
        builder.setMethod(field.getStringValue());
      } else if (ConfigTag.SOURCE.equalsIgnoreCase(field.getName())) {
        builder.setSource(field.getStringValue());
      } else {
        builder.setProperty(field.getName(), field.getStringValue());
      }
    }

    // if no provider, assume local
    if (StringUtil.isBlank(builder.getProvider())) builder.setProvider(LOCAL);

    // if no method && local provider, assume file
    if (StringUtil.isBlank(builder.getMethod()) && LOCAL.equalsIgnoreCase(builder.getProvider()))
      builder.setMethod(FILE);

    try {
      Vault vault = builder.build();
      Template.putStatic(Vault.LOOKUP_TAG, new VaultProxy(vault));
    } catch (ConfigurationException e) {
      e.printStackTrace();
    } catch (VaultException e) {
      e.printStackTrace();
    }
  }


  /**
   * Configure the preloader
   *
   * @param cfg    the configuration section to use in configuring the components
   * @param engine the engine to configure
   */
  private static void configPreloader(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      // Make sure the class is fully qualified 
      String className = findString(ConfigTag.CLASS, cfg);
      if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
        className = READER_PKG + "." + className;
        cfg.put(ConfigTag.CLASS, className);
      } else {
        Log.error("NO Reader Class in configuration: " + cfg.toString());
      }
      Object object = CDX.createComponent(cfg);
      if (object != null) {
        if (object instanceof FrameReader) {
          engine.setPreloader((FrameReader) object);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_preloader", object.getClass().getName()));
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_a_preloader", object.getClass().getName()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_preloader", className));
      }
    } // cfg !null    
  }


  /**
   * Configure the aggregator
   *
   * @param cfg    the configuration section to use in configuring the components
   * @param engine the engine to configure
   */
  private static void configAggregator(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      // Make sure the class is fully qualified 
      String className = findString(ConfigTag.CLASS, cfg);
      if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
        className = AGGREGATOR_PKG + "." + className;
        cfg.put(ConfigTag.CLASS, className);
      }
      Object object = CDX.createComponent(cfg);
      if (object != null) {
        if (object instanceof FrameAggregator) {
          engine.addAggregator((FrameAggregator) object);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_aggregator", object.getClass().getName()));
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_an_aggregatorr", object.getClass().getName()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_aggregator", className));
      }
    } // cfg !null    
  }


  /**
   * Use the given configuration frame to add validators to the engine.
   *
   * @param cfg    the dataframe containing the validators
   * @param engine the engine to configure with the validators
   */
  private static void configValidation(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = VALIDATOR_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame validatorConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, validatorConfig);
          if (object != null) {
            if (object instanceof FrameValidator) {
              engine.addValidator((FrameValidator) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_validator", object.getClass().getName(), validatorConfig));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_a_validator", field.getName()));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_validator", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.frame_validator_did_not_contain_valid_configuration", field.getStringValue()));
        }
      } // for each validator
    } // cfg !null
  }


  private static void configTransformer(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = TRANSFORM_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame transformerConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, transformerConfig);
          if (object != null) {
            if (object instanceof FrameTransform) {
              engine.addTransformer((FrameTransform) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_frame_transformer", object.getClass().getName(), transformerConfig));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_was_not_a_transformer", field.getName()));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_an_instance_of_specified_transformer", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.transformer_task_did_not_contain_valid_configuration", field.getStringValue()));
        }
      } // for each transformer
    } // cfg !null
  }


  private static void configFilters(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = FILTER_PKG + "." + className;
        }

        // All filters must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame taskConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, taskConfig);
          if (object != null) {
            if (object instanceof FrameFilter) {
              int seq = engine.addFilter((FrameFilter) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_filter", object.getClass().getName(), seq, cfg));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.class_not_filter", object.getClass().getName()));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.coult_not_create_filter", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.filter_config_not_section", field.getStringValue()));
        }
      } // for each task
    } // cfg !null
  }


  private static void configPreProcess(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = TASK_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame taskConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, taskConfig);
          if (object != null) {
            if (object instanceof TransformTask) {
              int seq = engine.addPreProcessTask((TransformTask) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_preprocess_task", object.getClass().getName(), seq, taskConfig));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.preprocess_class_not_transform_task", object.getClass().getName()));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_preprocess_task", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.preprocess_task_config_not_section", field.getStringValue()));
        }
      } // for each task
    } // cfg !null
  }


  private static void configPostProcess(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = TASK_PKG + "." + className;
        }

        // All tasks must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame taskConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, taskConfig);
          if (object != null) {
            if (object instanceof TransformTask) {
              int seq = engine.addPostProcessTask((TransformTask) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.Created postprocess task {} seq={} cfg={}", object.getClass().getName(), seq, cfg));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.Specified post-process class was not a transform task"));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.Could not create an instance of the specified post-process task '{}'", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.Post-process task did not contain a configuration, only scalar {}", field.getStringValue()));
        }
      } // for each task 
    } // cfg !null
  }


  /**
   * This section of the configuration should be simply name-value pairs.
   *
   * <p>The values are treated as templates and parsed into their final values based on the contents of the symbol
   * table and the systems properties contained therein.</p>
   *
   * <p>The name value pairs are placed in the symbol table as well as the context so that results of processing can be
   * used in subsequent templates.</p>
   *
   * <p>If the configuration contains a "class" attribute, then a custom context will be loaded from that class name
   * and passed the configuration section to configure it further. In such cases, custom context classes use the
   * "fields" attribute to populate the context, by convention. If no class attribute is given, each of the attributes
   * are loaded into the default context as described above.</p>
   *
   * @param cfg    the configuration frame
   * @param engine the transform engine
   */
  private static void configContext(Config cfg, TransformEngine engine) {

    if (cfg != null) {
      TransformContext context = engine.getContext();

      if (context == null) {
        if (cfg.contains(ConfigTag.CLASS)) {
          String className = cfg.getAsString(ConfigTag.CLASS);
          if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
            className = OperationalContext.class.getPackage().getName() + "." + className;
            cfg.put(ConfigTag.CLASS, className);
          }

          try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> ctor = clazz.getConstructor();
            Object object = ctor.newInstance();

            if (object instanceof TransformContext) {
              try {
                context = (TransformContext) object;
                context.setConfiguration(cfg);
                engine.setContext(context);
                context.setEngine(engine);
                Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_custom_context", context.getClass().getName()));
              } catch (Exception e) {
                Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_configure_specified_context", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
              }
            } else {
              Log.warn(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_context_not_transformcontext", className));
            }
          } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_context", className, e.getClass().getName(), e.getMessage()));
          }
        } else {
          // this is a regular, in-memory context with these settings
          context = new TransformContext();
          context.setConfiguration(cfg);
          engine.setContext(context);
          context.setEngine(engine);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_context", context.getClass().getName()));
        }
        Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.loaded_context", context.getClass().getName()));
      } else {
        Log.warn(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_replace_existing_context"));
      }
    } // cfg !null
  }


  /**
   * This allows a Job to have a set of loggers active while its engine runs.
   *
   * <p>These loggers are different from those in the Loader as they are active only when the engine is active. They
   * are created and deleted at the beginning and end of every Job run and have access to the job instance symbols.</p>
   * @param cfg    the configuration frame
   * @param engine the transform engine
   */
  private static void configJobLogging(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      LogManager logManager = new LogManager();
      try {
        logManager.setConfiguration(new Config(cfg));
      } catch (Throwable t) {
        Log.error("Could not configure job logger - " + t.getMessage());
      }
      engine.setLogManager(logManager);
    } // cfg !null
  }


  private static void configWriter(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      // Make sure the class is fully qualified 
      String className = findString(ConfigTag.CLASS, cfg);
      if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
        className = WRITER_PKG + "." + className;
        cfg.put(ConfigTag.CLASS, className);
      }
      Object object = CDX.createComponent(cfg);
      if (object != null) {
        if (object instanceof FrameWriter) {
          engine.addWriter((FrameWriter) object);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_writer", object.getClass().getName()));
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_a_writer", object.getClass().getName()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_writer", className));
      }
    } // cfg !null
  }


  private static void configMapper(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      String className = findString(ConfigTag.CLASS, cfg);

      // If there is no class tag, use the default mapper class
      if (className == null) {
        cfg.put(ConfigTag.CLASS, DefaultFrameMapper.class.getCanonicalName());
      } else {
        // make sure the class name is fully qualified
        if (StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = MAPPER_PKG + "." + className;
          cfg.put(ConfigTag.CLASS, className);
        }
      }

      Object object = CDX.createComponent(cfg);
      if (object != null) {
        if (object instanceof FrameMapper) {
          engine.setMapper((FrameMapper) object);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_mapper", object.getClass().getName()));
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_not_framemapper", object.getClass().getName()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_specified_mapper", className));
      }
    } // cfg !null
  }


  private static void configReader(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      // Make sure the class is fully qualified 
      String className = findString(ConfigTag.CLASS, cfg);
      if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
        className = READER_PKG + "." + className;
        cfg.put(ConfigTag.CLASS, className);
      } else {
        Log.error("NO Reader Class in configuration: " + cfg.toString());
      }
      Object object = CDX.createComponent(cfg);
      if (object != null) {
        if (object instanceof FrameReader) {
          engine.setReader((FrameReader) object);
          Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_reader", object.getClass().getName()));
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_a_reader", object.getClass().getName()));
        }
      } else {
        Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_reader", className));
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
   * @param cfg    The entire data frame containing the listener configurations
   * @param engine The engine to which the configured listeners will be added.
   */
  private static void configListener(DataFrame cfg, TransformEngine engine) {
    if (cfg != null) {
      for (DataField field : cfg.getFields()) {
        String className = field.getName();
        if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
          className = LISTENER_PKG + "." + className;
        }

        // All listeners must have an object(frame) as its value.
        if (field.isFrame()) {
          DataFrame listenerConfig = (DataFrame) field.getObjectValue();
          Object object = CDX.createComponent(className, listenerConfig);
          if (object != null) {
            if (object instanceof ContextListener) {
              engine.addListener((ContextListener) object);
              Log.debug(LogMsg.createMsg(CDX.MSG, "EngineFactory.created_listener", object.getClass().getName()));
            } else {
              Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.specified_class_is_not_a_listener", object.getClass().getName()));
            }
          } else {
            Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.could_not_create_instance_of_specified_listener", className));
          }
        } else {
          Log.error(LogMsg.createMsg(CDX.MSG, "EngineFactory.listener_configuration_invalid", field.getStringValue()));
        }
      } // for each listener 
    } // cfg !null
  }


  /**
   * Convenience method to perform a case insensitive search for a named field
   * in a data frame and return its value as a string.
   *
   * @param name  the name of the field to search
   * @param frame the data frame in which to search
   * @return the string value of the first found field with that name or null if the field is null, the name is null or
   * the field with that name was not found.
   */
  private static String findString(String name, DataFrame frame) {
    if (name != null) {
      for (DataField field : frame.getFields()) {
        if (StringUtil.equalsIgnoreCase(name, field.getName())) {
          return field.getStringValue();
        }
      }
    }
    return null;
  }

}
