/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.Database;
import coyote.dx.Symbols;
import coyote.dx.TransformEngine;
import coyote.loader.cfg.Config;


/**
 * This is an operational context for the transformation job as a whole.
 */
public class TransformContext extends OperationalContext {

  public static final String DISPOSITION = "TransformDisposition";
  public static final String ENDTIME = "EndTime";
  public static final String ERROR_MSG = "ErrorMessage";
  public static final String ERROR_STATE = "ErrorState";
  public static final String FRAME_COUNT = "FrameCount";
  public static final String STARTTIME = "StartTime";
  private static final String SOURCE = "Source.";
  private static final String TARGET = "Target.";
  private static final String CONTEXT = "Context.";
  private static final String TRANSFORM = "Transform.";

  private static final String WORKING = "Working.";
  private volatile TransactionContext transactionContext = null;
  protected Config configuration = new Config();
  protected Map<String, Database> databases = new HashMap<String, Database>();
  protected TransformEngine engine = null;
  protected volatile long openCount = 0;




  public TransformContext() {
    super();
  }




  public TransformContext(final List<ContextListener> listeners) {
    super(listeners);
  }




  public void addDataStore(final Database store) {
    if ((store != null) && StringUtil.isNotBlank(store.getName())) {
      databases.put(store.getName(), store);
    }
  }




  /**
   * Close (terminate) the context.
   */
  public void close() {
    for (final Map.Entry<String, Database> entry : databases.entrySet()) {
      //System.out.printf("Closing : %s %n", entry.getKey());
      try {
        entry.getValue().close();
      } catch (final IOException ignore) {
        //System.out.printf("Problems closing : %s - %s %n", entry.getKey(), ignore.getMessage());
      }
    }

    // Set the closing disposition of the job
    final Map<String, Object> disposition = new HashMap<String, Object>();
    disposition.put(Symbols.RUN_COUNT, openCount);
    disposition.put(STARTTIME, startTime);
    disposition.put(ENDTIME, endTime);
    disposition.put(ERROR_STATE, errorFlag);
    disposition.put(ERROR_MSG, errorMessage);
    disposition.put(FRAME_COUNT, currentFrame);
    properties.put(DISPOSITION, disposition);
  }




  public boolean containsField(final String token) {
    Boolean retval = false;
    if (token.startsWith(WORKING)) {
      final String name = token.substring(WORKING.length());
      if ((transactionContext != null) && (transactionContext.getWorkingFrame() != null)) {
        retval = transactionContext.getWorkingFrame().contains(name);
      }
    } else if (token.startsWith(SOURCE)) {
      final String name = token.substring(SOURCE.length());
      if ((transactionContext != null) && (transactionContext.getSourceFrame() != null)) {
        retval = transactionContext.getSourceFrame().contains(name);
      }
    } else if (token.startsWith(TARGET)) {
      final String name = token.substring(TARGET.length());
      if ((transactionContext != null) && (transactionContext.getTargetFrame() != null)) {
        retval = transactionContext.getTargetFrame().contains(name);
      }
    } else {
      // assume a working frame field
      if ((transactionContext != null) && (transactionContext.getWorkingFrame() != null)) {
        retval = transactionContext.getWorkingFrame().contains(token);
      }
    }
    return retval;
  }




  public Config getConfiguration() {
    return configuration;
  }




  public Database getDatabase(final String name) {
    return databases.get(name);
  }




  /**
  * @return the current transaction context
  */
  public TransactionContext getTransaction() {
    return transactionContext;
  }




  /**
   * Open (initialize) the context.
   *
   * <p>This is called when the engine begins running and is where we use the
   * currently set symbol table to resolve all the variables set in this
   * context.</p>
   */
  @SuppressWarnings("unchecked")
  public void open() {

    // Increment the run count
    openCount++;
    engine.getSymbolTable().put(Symbols.RUN_COUNT, openCount);

    // If we have a configuration...
    if (configuration != null) {
      // fill the context with configuration data
      for (final DataField field : configuration.getFields()) {
        if (!field.isFrame() && StringUtil.isNotBlank(field.getName()) && !field.isNull()) {
          final String token = field.getStringValue();
          final String value = Template.resolve(token, engine.getSymbolTable());
          engine.getSymbolTable().put(field.getName(), value);
          set(field.getName(), value);
        } //name-value check
      } // for
    }

  }




  /**
   * reset the context so it can be used again in subsequent (scheduled) runs
   */
  public void reset() {
    super.currentFrame = 0;
    super.startTime = 0;
    super.endTime = 0;
    super.errorFlag = false;
    super.errorMessage = null;
  }




  /**
   * Resolve the argument.
   *
   * <p>This will try to retrieve the value from the transform context using
   * the given value as it may be a reference to a context property.</p>
   *
   * <p>If no value was found in the look-up, then the value is treated as a
   * literal and will be returned as the argument.</p>
   *
   * <p>Regardless of whether or not the value was retrieved from the
   * transform context as a reference value, the value is resolved as a
   * template using the symbol table in the transform context. This allows for
   * more dynamic values during the operation of the entire transformation
   * process.</p>
   *
   * @param value the value to resolve (or use as a literal)
   *
   * @return the resolved value of the argument.
   */
  public String resolveArgument(final String value) {
    String retval = null;

    // lookup the value in the transform context
    String cval = getAsString(value);

    // If the lookup failed, just use the value
    if (StringUtil.isBlank(cval)) {
      cval = value;
    }

    // in case it is a template, resolve it to the context's symbol table
    if (StringUtil.isNotBlank(cval)) {
      retval = Template.resolve(cval, getSymbols());
    }
    return retval;
  }




  /**
   * Return the value of a data frame field currently set in the transaction
   * context of this context.
   *
   * <p>Matching is case sensitive.
   *
   * <p>Naming Conventions:<ul>
   * <li>Working.[field name] field in the working frame of the transaction
   * context.
   * <li>Source.[field name] field in the source frame of the transaction
   * context.
   * <li>Target.[field name] field in the target frame of the transaction
   * context.
   * <li>[field name] field in the working frame of the transaction
   * <li>Context.[field name] value in the transaction context.
   * <li>Transform.[field name] value in the transform context.</ul>
   *
   * @param token the name of the field, context or symbol value
   *
   * @return the string value of the named value in this context or null if not found.
   */
  public String resolveField(final String token) {
    String retval = null;
    if (token.startsWith(WORKING)) {
      final String name = token.substring(WORKING.length());
      if ((transactionContext != null) && (transactionContext.getWorkingFrame() != null)) {
        retval = transactionContext.getWorkingFrame().getAsString(name);
      }
    } else if (token.startsWith(SOURCE)) {
      final String name = token.substring(SOURCE.length());
      if ((transactionContext != null) && (transactionContext.getSourceFrame() != null)) {
        retval = transactionContext.getSourceFrame().getAsString(name);
      }
    } else if (token.startsWith(TARGET)) {
      final String name = token.substring(TARGET.length());
      if ((transactionContext != null) && (transactionContext.getTargetFrame() != null)) {
        retval = transactionContext.getTargetFrame().getAsString(name);
      }
    } else if (token.startsWith(CONTEXT)) {
      final String name = token.substring(CONTEXT.length());
      if (transactionContext != null) {
        retval = transactionContext.getAsString(name);
      }
    } else if (token.startsWith(TRANSFORM)) {
      final String name = token.substring(TRANSFORM.length());
      retval = getAsString(name);
    }
    return retval;
  }




  /**
   * Return the value of a data frame field currently set in the transaction
   * context of this context.
   *
   * <p>Matching is case sensitive.
   *
   * <p>Naming Conventions:<ul>
   * <li>Working.[field name] field in the working frame of the transaction
   * context.
   * <li>Source.[field name] field in the source frame of the transaction
   * context.
   * <li>Target.[field name] field in the target frame of the transaction
   * context.
   * <li>Context.[field name] value in the transaction context.
   * <li>Transform.[field name] value in the transform context.</ul>
   *
   * @param token the name of the field, context or symbol value
   *
   * @return the object value of the named value in this context or null if not found.
   */
  public Object resolveFieldValue(final String token) {
    Object retval = null;
    if (token.startsWith(WORKING)) {
      final String name = token.substring(WORKING.length());
      if ((transactionContext != null) && (transactionContext.getWorkingFrame() != null)) {
        retval = transactionContext.getWorkingFrame().getObject(name);
      }
    } else if (token.startsWith(SOURCE)) {
      final String name = token.substring(SOURCE.length());
      if ((transactionContext != null) && (transactionContext.getSourceFrame() != null)) {
        retval = transactionContext.getSourceFrame().getObject(name);
      }
    } else if (token.startsWith(TARGET)) {
      final String name = token.substring(TARGET.length());
      if ((transactionContext != null) && (transactionContext.getTargetFrame() != null)) {
        retval = transactionContext.getTargetFrame().getObject(name);
      }
    } else if (token.startsWith(CONTEXT)) {
      final String name = token.substring(CONTEXT.length());
      if (transactionContext != null) {
        retval = transactionContext.get(name);
      }
    } else if (token.startsWith(TRANSFORM)) {
      final String name = token.substring(TRANSFORM.length());
      retval = get(name);
    }
    return retval;
  }




  /**
   * Return the value of something in this transform context to a string value.
   *
   * <p>The token is scanned for a prefix to determine if it is a field name
   * in one of the data frames in the transaction context. If not, the token
   * is checked against object values in the context and the string value of
   * any returned object in the context with that name is returned. Finally,
   * if the context did not contain an object with a name matching the token,
   * the symbol table is checked and any matching symbol is returned.
   *
   * <p>Matching is case sensitive.
   *
   * <p>Naming Conventions:<ul>
   * <li>Working.[field name] field in the working frame of the transaction
   * context.
   * <li>Source.[field name] field in the source frame of the transaction
   * context.
   * <li>Target.[field name] field in the target frame of the transaction
   * context.
   * <li>[field name] field in the working frame of the transaction
   * <li>[field name] context value
   * <li>[field name] symbol value</ul>
   *
   * @param token the name of the field, context or symbol value
   *
   * @return the string value of the named value in this context or null if not found.
   */
  public String resolveToString(final String token) {
    String retval = null;
    Object obj = resolveToValue(token);

    if (obj != null) {
      retval = obj.toString();
    }
    return retval;
  }




  /**
   * Return the value of something in this transform context.
   *
   * <p>The token is scanned for a prefix to determine if it is a field name
   * in one of the data frames in the transaction context. If not, the token
   * is checked against object values in the context and the value of any 
   * returned object in the context with that name is returned. Finally,
   * if the context did not contain an object with a name matching the token,
   * the symbol table is checked and any matching symbol is returned.
   *
   * <p>Matching is case sensitive.
   *
   * <p>Naming Conventions:<ul>
   * <li>Working.[field name] field in the working frame of the transaction
   * context.
   * <li>Source.[field name] field in the source frame of the transaction
   * context.
   * <li>Target.[field name] field in the target frame of the transaction
   * context.
   * <li>[field name] field in the working frame of the transaction
   * <li>[field name] context value
   * <li>[field name] symbol value</ul>
   *
   * @param token the name of the field, context or symbol value
   *
   * @return the object value of the named value in this context or null if not found.
   */
  public Object resolveToValue(final String token) {
    Object retval = null;
    if (StringUtil.isNotEmpty(token)) {
      final Object value = resolveFieldValue(token);
      if (value != null) {
        retval = value;
      } else {
        final Object obj = searchForValue(token);
        if (obj != null) {
          retval = obj.toString();
        } else {
          if (symbols != null && symbols.containsKey(token)) {
            retval = symbols.get(token);
          }
        }
      }
    }
    return retval;
  }




  /**
   * Performs a search of this context.
   * 
   * <p>This search supports hierarchical searches using a dotted notation. 
   * Each fieldname is divided into separate tokens and used to search deeper 
   * into the hierarchy.
   * 
   * <p>If a token does not result in finding a named value, the token is 
   * rebuilt adding the remaining tokens in order and performing a search each 
   * time a token is added. This allows for finding fields with dotted names.
   * 
   * @param fieldname
   * @return
   */
  protected Object searchForValue(String fieldname) {
    Object retval = null;
    String[] tokens = fieldname.split("\\.");
    if (tokens.length == 1) {
      retval = get(fieldname);
    } else {
      int generation = 0;
      String nameToCheck = tokens[generation];
      while (generation < tokens.length) {
        Object ancestor = get(nameToCheck);
        if (ancestor != null) {
          if (generation + 1 < tokens.length) {
            if (ancestor instanceof DataFrame) {
              return checkFrame((DataFrame)ancestor, tokens, generation);
            } else if (ancestor instanceof Map) {
              return checkMap((Map)ancestor, tokens, generation);
            }
          } else {
            retval = ancestor;
          }
        } else {
          if (generation + 1 < tokens.length) {
            nameToCheck = nameToCheck.concat(".").concat(tokens[generation + 1]);
          }
        }
        generation++;
      }
    }
    return retval;
  }




  /**
   * @param map the map to check
   * @param tokens the list of tokens from the request
   * @param level the pointer to how far into the token list we are
   * 
   * @return the value found or null if there is no matching value with that name.   
   */
  private Object checkMap(Map map, String[] tokens, int level) {
    Object retval = null;
    int generation = level + 1;
    String nameToCheck = tokens[generation];
    while (generation < tokens.length) {
      Object value = map.get(nameToCheck);
      if (value != null) {
        if (generation + 1 < tokens.length) {
          if (value instanceof DataFrame) {
            return checkFrame((DataFrame)value, tokens, generation);
          } else if (value instanceof Map) {
            return checkMap((Map)value, tokens, generation);
          }
        } else {
          retval = value;
        }
      } else {
        if (generation + 1 < tokens.length) {
          nameToCheck = nameToCheck.concat(".").concat(tokens[generation + 1]);
        }
      }
      generation++;
    }
    return retval;
  }




  /**
   * @param frame the dataframe to check
   * @param tokens the list of tokens from the request
   * @param level the pointer to how far into the token list we are
   * 
   * @return the value found or null if there is no matching value with that name.
   */
  private Object checkFrame(DataFrame frame, String[] tokens, int level) {
    Object retval = null;
    int generation = level + 1;
    String nameToCheck = tokens[generation];
    while (generation < tokens.length) {
      Object value = frame.getObject(nameToCheck);
      if (value != null) {
        if (generation + 1 < tokens.length) {
          if (value instanceof DataFrame) {
            return checkFrame((DataFrame)value, tokens, generation);
          } else if (value instanceof Map) {
            return checkMap((Map)value, tokens, generation);
          }
        } else {
          retval = value;
        }
      } else {
        if (generation + 1 < tokens.length) {
          nameToCheck = nameToCheck.concat(".").concat(tokens[generation + 1]);
        }
      }
      generation++;
    }
    return retval;
  }




  public void setConfiguration(final Config config) {
    configuration = config;
  }




  /**
   * @param engine the engine to which this context is associated.
   */
  public void setEngine(final TransformEngine engine) {
    this.engine = engine;
  }




  /**
   * Sets the current transaction in the transformation context.
   *
   * <p>This allows all components in the transformation engine to access the
   * current transaction frames.</p>
   *
   * @param context the current transaction context being processed
   */
  public void setTransaction(final TransactionContext context) {
    transactionContext = context;
  }




  /**
   * @return the engine to which this context is associated.
   */
  public TransformEngine getEngine() {
    return engine;
  }

}
