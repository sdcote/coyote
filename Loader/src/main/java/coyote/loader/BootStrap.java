/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.loader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.commons.UriUtil;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a boot strap loader which uses the configuration file to determine 
 * which loader to use.
 * 
 * <p>Since there are several different loaders from which to choose, this 
 * loader reads in a configuration, like other loaders, then uses the 
 * configuration to determine which loader to use. If none are specified, the
 * default loader is used which should be good for most general loading tasks.
 */
public class BootStrap extends AbstractLoader {

  private static Config configuration = null;
  private static String cfgLoc = null;
  private static URI cfgUri = null;

  protected static final String DEBUG_ARG = "-d";
  protected static final String INFO_ARG = "-v";
  private static final String VERSION_ARG = "-version";
  private static final String HELP_ARG = "-help";

  private static final String JSON_EXT = ".json";




  /**
   * Performs encryption operation from the command line arguments.
   * 
   * <p>The loader provides a way for the operator to generate encrypted values 
   * which can be placed in configuration files. This allows user names and 
   * passwords to be hidden from those with access to the files but who do not 
   * have access to the encryption keys.</p>
   * 
   * @param args the entire command line arguments to parse for encryption details
   */
  private static void encrypt(String[] args) {
    String token = null;
    String key = System.getProperty(ConfigTag.CIPHER_KEY, CipherUtil.getKey(CipherUtil.CIPHER_KEY));
    String cipherName = System.getProperty(ConfigTag.CIPHER_NAME, CipherUtil.CIPHER_NAME);
    if (args.length < 2) {
      System.err.println("Nothing to encrypt");
      return;
    } else {
      token = args[1];
      if (args.length > 2) {
        String rawkey = args[2];
        // make sure is it base64 encoded or make it so
        try {
          CipherUtil.decode(rawkey);
          key = rawkey;
        } catch (Exception e) {
          System.out.println("User-specified key did not appear to be Base64 encoded, encoding it.");
          key = CipherUtil.getKey(rawkey);
        }

        if (args.length > 3) {
          cipherName = args[3];
        }
      }
    }
    System.out.println("Encrypting '" + token + "'");
    System.out.println("with a key of '" + key + "'");
    System.out.println("using a '" + cipherName + "' cipher");

    if (CipherUtil.getCipher(cipherName) != null) {
      String ciphertext = CipherUtil.encipher(token, cipherName, key);
      System.out.println(ciphertext);
    } else {
      System.err.println("Cipher '" + cipherName + "' is not supported");
    }

  }




  /**
   * Use the first command line argument as the URI to the configuration file 
   * unless it is the encrypt keyword then the arguments are used to generate 
   * an encrypted string.
   * 
   * <p>If there are no arguments, then assume the configuration URI is stored 
   * in the {@code cfg.uri} system property.</p>
   * 
   * @param args The command line arguments passed to the main method
   */
  private static void parseArgs(String[] args) {

    // Get the URI to our configuration from either the command line or the system properties
    if (args != null && args.length > 0) {
      // if the first argument is "encrypt" perform an encryption operation 
      // using the rest of the command line arguments
      if (ENCRYPT.equalsIgnoreCase(args[0])) {
        encrypt(args);
        System.exit(0);
      } else {

        boolean abort = false;
        for (int x = 0; x < args.length; x++) {
          if (DEBUG_ARG.equalsIgnoreCase(args[x])) {
            Log.startLogging(Log.DEBUG);
          } else if (INFO_ARG.equalsIgnoreCase(args[x])) {
            Log.startLogging(Log.INFO);
          } else if (VERSION_ARG.equalsIgnoreCase(args[x])) {
            showVersion();
            abort = true;
          } else if (HELP_ARG.equalsIgnoreCase(args[x])) {
            showHelp();
            abort = true;
          } else {
            if (!args[x].startsWith("-")) {
              if (cfgLoc != null) {
                System.out.println("Warning: using '" + args[x] + "' instead of '" + cfgLoc + "'");
              }
              cfgLoc = args[x];
            }
          }
        }
        if (abort) {
          System.exit(0);
        }
      }
    }

    // Make sure we have a configuration 
    if (StringUtil.isBlank(cfgLoc)) {
      System.err.println(LogMsg.createMsg(MSG, "Loader.error_no_config"));
      System.exit(8);
    }

  }




  /**
   * 
   */
  private static void showHelp() {
    try {
      Class<?> clazz = Class.forName("coyote.loader.LoaderHelp");
      Constructor<?> ctor = clazz.getConstructor();
      Object obj = ctor.newInstance();
      System.out.println(obj.toString());
    } catch (Exception e) {
      // expected when the class is not present
      System.out.println("No help available.\n");
    }
  }




  /**
   * Prints the version of this loader and any other versions found.
   * 
   * <p>This method tries to load a "LoaderVersion" class which components can 
   * contribute to the classpath to get their versions reported as well.
   */
  private static void showVersion() {
    StringBuffer b = new StringBuffer();
    b.append("Loader   ");
    b.append(Loader.API_VERSION.toString());

    try {
      Class<?> clazz = Class.forName("coyote.loader.LoaderVersion");
      Constructor<?> ctor = clazz.getConstructor();
      Object obj = ctor.newInstance();
      b.append("\n");
      b.append(obj.toString());
    } catch (Exception e) {
      // expected when the class is not present
    }
    System.out.println(b.toString());
  }




  /**
   * Generate a configuration from the file URI specified on the command line 
   * or the {@code cfg.uri} system property.
   * 
   * <p>If the URI has no scheme, then it is assumed to be a file name. If the 
   * file name is relative, the current directory will be checked for its 
   * existence. if it does not exist there, the {@code cfg.dir} system property 
   * is used to determine a common configuration directory and the existence of 
   * the file will be checked in that location. If the file does not exist 
   * there, a simple error message is displayed and the boot strap loader 
   * terminates.</p> 
   */
  private static void readConfig() {
    try {
      configuration = Config.read(cfgUri);
      if (StringUtil.isBlank(configuration.getName())) {
        configuration.setName(UriUtil.getBase(cfgUri));
      }
      System.setProperty(ConfigTag.CONFIG_URI, cfgUri.toString());
    } catch (IOException | ConfigurationException e) {
      System.err.println(LogMsg.createMsg(MSG, "Loader.error_reading_configuration", cfgUri, e.getLocalizedMessage(), ExceptionUtil.stackTrace(e)));
      System.exit(7);
    }

  }




  /**
   * Determine the loader to use from the given configuration and create an 
   * instance of it.
   * 
   * <p>Once created, the loader will be passed the configuration resulting in 
   * a configured loader</p>
   * 
   * @param args the command line arguments passed to this bootstrap loader
   *  
   * @return a configured loader or null if there was no "CLASS" attribute in 
   *         the root of the configuration indicating was not found.
   */
  private static Loader buildLoader(String[] args) {
    Loader retval = null;

    String className = configuration.getClassName();
    if (StringUtil.isBlank(className)) {
      className = DefaultLoader.class.getName();
      Log.info(LogMsg.createMsg(MSG, "Loader.using_default_loader", className));
    }

    if (className != null && StringUtil.countOccurrencesOf(className, ".") < 1) {
      className = BootStrap.class.getPackage().getName() + "." + className;
    }

    try {
      Class<?> clazz = Class.forName(className);
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();

      if (object instanceof Loader) {
        retval = (Loader)object;
        try {
          retval.setCommandLineArguments(args);
          retval.configure(configuration);
        } catch (ConfigurationException e) {
          System.err.println(LogMsg.createMsg(MSG, "Loader.could_not_config_loader", object.getClass().getName(), e.getClass().getSimpleName(), e.getMessage()));
          System.exit(6);
        }
      } else {
        System.err.println(LogMsg.createMsg(MSG, "Loader.class_is_not_loader", className));
        System.exit(5);
      }
    } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      System.err.println(LogMsg.createMsg(MSG, "Loader.instantiation_error", className, e.getClass().getName(), e.getMessage()));
      System.exit(4);
    }

    return retval;
  }




  /**
   * Use the first argument in the command line (or the 'cfg.uri' system 
   * property) to specify a URI of a configuration file to load.
   * 
   * @param args command line arguments.
   */
  public static void main(String[] args) {

    // set the default logger
    Log.addLogger(Log.DEFAULT_LOGGER_NAME, new ConsoleAppender(Log.WARN_EVENTS | Log.ERROR_EVENTS | Log.FATAL_EVENTS));

    // Parse the command line arguments
    parseArgs(args);

    Log.info("Verbose logging is enabled");
    Log.debug("Debug logging is enabled");

    // confirm the readability of app.home if it is set
    confirmAppHome();

    // Confirm the writability of app.work if it is set
    confirmAppWork();

    // confirm the configuration location is a valid URI
    confirmConfigurationLocation();

    // Read in the configuration
    readConfig();

    // Create a loader from the configuration
    Loader loader = buildLoader(args);

    // If we have a loader
    if (loader != null) {

      // Register a shutdown method to terminate cleanly when the JVM exit
      registerShutdownHook(loader);

      // Name the loader based on its configuration URI
      if (loader.getName() == null) {
        loader.setName(getNameFromUri(cfgUri));
      }

      // Start the loader running in the current thread
      try {
        loader.start();
      } catch (Throwable t) {
        System.err.println(LogMsg.createMsg(MSG, "Loader.logic_error_from_loader", t.getLocalizedMessage(), ExceptionUtil.stackTrace(t)));
        System.exit(3);
      }
    } else {
      System.err.println(LogMsg.createMsg(MSG, "Loader.no_loader_configured"));
      System.exit(2);
    }

    // Normal termination
    System.exit(0);

  }




  /**
   * Return a name from the given URI
   * @param uri the URI to query
   * @return the base of the URI
   */
  private static String getNameFromUri(URI uri) {
    return UriUtil.getBase(uri);
  }




  /**
   * Confirm the configuration location
   */
  private static void confirmConfigurationLocation() {
    // start building error messages for user feedback
    StringBuffer errMsg = new StringBuffer(LogMsg.createMsg(MSG, "Loader.confirming_cfg_location", cfgLoc) + StringUtil.CRLF);

    // process the configuration location if it exists
    if (StringUtil.isNotBlank(cfgLoc)) {

      // all configurations locations should be a URI
      try {
        cfgUri = new URI(cfgLoc);
      } catch (URISyntaxException e) {
        // This can happen when the location is a filename
      }

      // if we could not create a URI from the location or its scheme is empty
      if (cfgUri == null || StringUtil.isBlank(cfgUri.getScheme())) {

        // try the location as a file
        File localfile = new File(cfgLoc);
        // keep track of an alternative version of specifying the file
        File alternativeFile = new File(cfgLoc + JSON_EXT);

        // make sure a file object has been created
        if (localfile != null) {

          // if the file is an absolute path or is relative and exists in the 
          // current working directory...
          if (localfile.exists()) {
            // we are done, get the file location as a URI
            cfgUri = FileUtil.getFileURI(localfile);
          } else {
            if (!alternativeFile.exists()) {
              alternativeFile = null;
            }

            // add the filename we checked unsuccessfully to the error message
            errMsg.append(LogMsg.createMsg(MSG, "Loader.no_local_cfg_file", localfile.getAbsolutePath()) + StringUtil.CRLF);

            // the file does not exist, so if it is a relative filename... 
            if (!localfile.isAbsolute()) {

              // see if there is a system property with a shared configuration directory
              String path = System.getProperties().getProperty(APP_HOME);

              // if there is a application home directory specified
              if (StringUtil.isNotBlank(path)) {

                // remove all the relations and duplicate slashes
                String appDir = FileUtil.normalizePath(path);

                // create a file reference to that shared directory 
                File homeDir = new File(appDir);

                // create a reference to the configuration directory
                File configDir = new File(homeDir, "cfg");

                // make sure it exists
                if (configDir.exists()) {
                  // make sure it is a directory
                  if (configDir.isDirectory()) {
                    // make a file reference to expected file
                    File cfgFile = new File(configDir, cfgLoc);
                    // see if it exists
                    if (cfgFile.exists()) {
                      // Success - cfg was found in the shared config directory
                      cfgUri = FileUtil.getFileURI(cfgFile);
                    } else {
                      // if an laternat was found in the local directory, use it
                      if (alternativeFile != null) {
                        cfgUri = FileUtil.getFileURI(alternativeFile);

                      } else {
                        // try adding an extension to the file in the common cfg directory
                        alternativeFile = new File(configDir, cfgLoc + JSON_EXT);
                        if (alternativeFile.exists()) {
                          // Success - cfg was found in the shared config directory with an added extension
                          cfgUri = FileUtil.getFileURI(alternativeFile);
                        } else {
                          // we tried the local and shared locations, report error
                          errMsg.append(LogMsg.createMsg(MSG, "Loader.no_common_cfg_file", cfgFile.getAbsolutePath()) + StringUtil.CRLF);
                          errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
                          System.out.println(errMsg.toString());
                          System.exit(9);
                        }
                      }
                    }
                  } else {
                    // the specified config directory was not a directory
                    errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_dir_is_not_directory", appDir) + StringUtil.CRLF);
                    System.out.println(errMsg.toString());
                    System.exit(10);
                  }
                } else {
                  // the specified config directory does not exist
                  errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_dir_does_not_exist", appDir) + StringUtil.CRLF);
                  System.out.println(errMsg.toString());
                  System.exit(11);
                }
              } else {
                if (alternativeFile != null) {
                  cfgUri = FileUtil.getFileURI(alternativeFile);
                } else {
                  // no shared config directory provided in system properties
                  errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_dir_not_provided", APP_HOME) + StringUtil.CRLF);
                  System.out.println(errMsg.toString());
                  System.exit(12);
                }
              }

            } // localfile is absolute

          } // localfile does not exist

        } //localfile != null

      } // cfguri is not valid

      if (cfgUri != null) {
        // Now check to see if the CFG is readable (if it is a file)
        if (UriUtil.isFile(cfgUri)) {
          File test = UriUtil.getFile(cfgUri);
          if (!test.exists() || !test.canRead()) {
            errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_file_not_readable", test.getAbsolutePath()) + StringUtil.CRLF);
            System.out.println(errMsg.toString());
            System.exit(13);
          }
          Log.info(LogMsg.createMsg(MSG, "Loader.cfg_reading_from_file", test.getAbsolutePath()));
        } else {
          Log.info(LogMsg.createMsg(MSG, "Loader.cfg_reading_from_network"));
        }
      } else {
        errMsg.append(LogMsg.createMsg(MSG, "Loader.cfg_file_not_found", cfgLoc) + StringUtil.CRLF);
        System.out.println(errMsg.toString());
        System.exit(9);

      }

    } else {
      System.err.println(LogMsg.createMsg(MSG, "Loader.no_config_uri_defined"));
      System.exit(1);
    }

  }

}
