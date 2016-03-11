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

/**
 * A list of constants shared by all components.
 */
public class ConfigTag {

  // System Properties

  public static final String CIPHER_NAME = "cipher.name";
  public static final String CIPHER_KEY = "cipher.key";

  public static final String AUTO_CREATE = "autocreate";
  /** Batch size ({@value}) configuration attribute. */
  public static final String BATCH = "batch";
  public static final String CLASS = "class";
  public static final String DRIVER = "driver";
  public static final String FIELDS = "fields";
  public static final String FORMAT = "format";
  public static final String HEADER = "header";
  public static final String FOOTER = "footer";
  public static final String LENGTH = "length";
  public static final String LIBRARY = "library";
  public static final String LIMIT = "limit";
  public static final String NAME = "name";
  public static final String VALUE = "value";
  public static final String CONDITION = "condition";
  public static final String DEFAULT = "default";
  public static final String PASSWORD = "password";
  public static final String ENCRYPTED_PASSWORD = "encrypted_password";
  public static final String PRELOAD = "preload";
  public static final String QUERY = "query";
  public static final String SOURCE = "source";
  public static final String START = "start";
  public static final String TABLE = "table";
  public static final String TARGET = "target";
  public static final String TRIM = "trim";
  public static final String TYPE = "type";
  /** User name ({@value}) configuration attribute. */
  public static final String USERNAME = "username";
  public static final String ENCRYPTED_USERNAME = "encrypted_username";
  public static final String DATEFORMAT = "dateformat";
  public static final String ALIGN = "align";
  public static final String RECURSE = "recurse";
  public static final String DOMAIN = "domain";
  public static final String READ = "read";
  public static final String WRITE = "write";
  public static final String LINEMAP = "linemap";
  public static final String PATTERN = "pattern";
  public static final String PRESERVE = "preserve";
  public static final String DELETE = "delete";
  public static final String OVERWRITE = "overwrite";
  public static final String KEEPDATE = "keepdate";
  public static final String RENAME = "rename";
  public static final String CHARACTER = "character";

  public static final String ROOT_ELEMENT = "rootElement";
  public static final String ROOT_ATTRIBUTE = "rootAttributes";
  public static final String ROW_ELEMENT = "rowElement";
  public static final String ROW_ATTRIBUTE = "rowAttributes";
  public static final String FIELD_FORMAT = "fieldFormat";

  // Transform engine

  public static final String CONTEXT = "context";
  public static final String TASKS = "tasks";
  public static final String PREPROCESS = "preprocess";
  public static final String POSTPROCESS = "postprocess";
  public static final String LISTENER = "listeners";
  public static final String DATABASE = "database";
  public static final String WRITER = "writer";
  public static final String MAPPER = "mapper";
  public static final String FILTERS = "filters";
  public static final String READER = "reader";
  public static final String DATABASES = "databases";
  public static final String VALIDATE = "validate";
  public static final String TRANSFORM = "transform";
  public static final String PERSISTENT_CONTEXT = "persistentcontext";
  public static final String LOGGERS = "loggers";
  public static final String CATEGORIES = "categories";
  public static final String ENABLED = "enabled";

  // Tasks

  public static final String TODIR = "todir";
  public static final String FROMDIR = "fromdir";
  public static final String HALT_ON_ERROR = "haltonerror";
  public static final String HOST = "hostname";
  public static final String PORT = "port";
  public static final String PROTOCOL = "protocol";
  public static final String FILE = "filename";
  public static final String DIRECTORY = "directory";
  public static final String APPEND = "append";

  // Validations

  public static final String FIELD = "field";
  public static final String MATCH = "match";
  public static final String AVOID = "avoid";
  public static final String HALT_ON_FAIL = "halt";
  public static final String DESCRIPTION = "desc";
  
  // Scheduler
  
  public static final String SCHEDULE = "Schedule";
  public static final String MINUTES = "minutes";
  public static final String HOURS = "houre";
  public static final String DAYS = "days";
  public static final String DAYS_OF_WEEK = "weekdays";
  public static final String MONTHS = "months";
  public static final String MILLIS = "millis";

}
