/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.i13n.platform;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

import coyote.i13n.Platform;
import coyote.loader.log.Log;


/**
 * The Platform class provides a variety of platform specific information in a 
 * standard manner. 
 */
public class DefaultPlatform implements Platform {

  protected static InetAddress localAddress = null;

  protected static final String OS_NAME = System.getProperty("os.name").toUpperCase(Locale.US);
  protected static final String OS_ARCH = System.getProperty("os.arch").toUpperCase(Locale.US);
  protected static final String OS_VERSION = System.getProperty("os.version").toUpperCase(Locale.US);
  protected static final String PATH_SEP = System.getProperty("path.separator");
  protected static final String FILE_SEP = System.getProperty("file.separator");
  protected static final String LINE_SEP = System.getProperty("line.separator");
  protected static final byte[] MTADR = {0, 0, 0, 0};

  protected static File tempDir;

  static File homeDir;

  protected static final String DEFAULT_HOME = System.getProperty("user.home");

  protected static int os = 0;
  protected static final int UNKNOWN = 0;
  protected static final int WINDOWS = 1;
  protected static final int SOLARIS = 2;
  protected static final int IRIX = 3;
  protected static final int MAC = 4;
  protected static final int HPUX = 5;
  protected static final int LINUX = 6;

  static {
    System.out.println(DefaultPlatform.OS_NAME);
    if (DefaultPlatform.OS_NAME.startsWith("WINDOWS") || DefaultPlatform.OS_NAME.startsWith("WIN XP")) {
      DefaultPlatform.os = DefaultPlatform.WINDOWS;

      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "Documents and Settings" + DefaultPlatform.FILE_SEP + "All Users" + DefaultPlatform.FILE_SEP + "Application Data");
      if (!DefaultPlatform.tempDir.exists()) {

        DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "TEMP");
        if (!DefaultPlatform.tempDir.exists()) {
          DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "WINNT" + DefaultPlatform.FILE_SEP + "TEMP");
          if (!DefaultPlatform.tempDir.exists()) {
            DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "TEMP");
            DefaultPlatform.tempDir.mkdir();
          }
        }
      }
    } else if (DefaultPlatform.OS_NAME.equals("SOLARIS") || DefaultPlatform.OS_NAME.equals("SUNOS")) {
      DefaultPlatform.os = DefaultPlatform.SOLARIS;
      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "var" + DefaultPlatform.FILE_SEP + "tmp");
        if (!DefaultPlatform.tempDir.exists()) {
          DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "usr" + DefaultPlatform.FILE_SEP + "tmp");
          DefaultPlatform.tempDir.mkdir();
        }
      }
    } else if (DefaultPlatform.OS_NAME.equals("IRIX")) {
      DefaultPlatform.os = DefaultPlatform.IRIX;
      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir.mkdir();
      }
    } else if (DefaultPlatform.OS_NAME.equals("MAC OS") || DefaultPlatform.OS_NAME.equals("MACOS")) {
      DefaultPlatform.os = DefaultPlatform.MAC;
      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir.mkdir();
      }
    } else if (DefaultPlatform.OS_NAME.equals("HP-UX")) {
      DefaultPlatform.os = DefaultPlatform.HPUX;
      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "var" + DefaultPlatform.FILE_SEP + "tmp");
        if (!DefaultPlatform.tempDir.exists()) {
          DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "usr" + DefaultPlatform.FILE_SEP + "tmp");
          DefaultPlatform.tempDir.mkdir();
        }
      }
    } else if (DefaultPlatform.OS_NAME.equals("LINUX")) {
      DefaultPlatform.os = DefaultPlatform.LINUX;
      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "var" + DefaultPlatform.FILE_SEP + "tmp");
        if (!DefaultPlatform.tempDir.exists()) {
          DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "usr" + DefaultPlatform.FILE_SEP + "tmp");
          DefaultPlatform.tempDir.mkdir();
        }
      }
    } else {
      DefaultPlatform.os = DefaultPlatform.UNKNOWN;

      DefaultPlatform.tempDir = new File(DefaultPlatform.FILE_SEP + "tmp");
      if (!DefaultPlatform.tempDir.exists()) {
        DefaultPlatform.tempDir.mkdir();
      }
    }
  }




  /**
   * This method will create a temporary file with a prefix of i13nX and a 
   * suffix or extension of ".tmp" where X will represent the i13n Identifier.
   * 
   * <p>Every attempt will be made to delete the file when the runtime exits 
   * normally but the file will probably remain if the VM is halted.
   * 
   * @return A file reference that will be deleted when the VM exits, or null 
   *         if the file could not be created for any reason.
   */
  static File createTempFile() {
    return DefaultPlatform.createTempFile(DefaultPlatform.tempDir);
  }




  /**
   * This method will create a temporary file with a prefix of i13nX and a 
   * suffix or extension of ".tmp" in the given directory where X will 
   * represent the i13n Identifier.
   * 
   * <p>Every attempt will be made to delete the file when the runtime exits 
   * normally but the file will probably remain if the VM is halted.
   * 
   * @return A file reference that will be deleted when the VM exits, or null 
   *         if the file could not be created for any reason.
   */
  static File createTempFile(final File dir) {
    try {
      final File retval = File.createTempFile("i13n + i13n.getId() + -", null, dir);
      retval.deleteOnExit();
      return retval;
    } catch (final IOException e) {
      Log.error("Could not create a temporary file in " + DefaultPlatform.tempDir + "' - " + e.getMessage());
    }

    return null;
  }




  /**
   * @return  A file reference that is suitible for temporary files.
   */
  static File getTempDir() {
    return DefaultPlatform.tempDir;
  }




  /**
   * Default constructor, should not be used.
   */
  public DefaultPlatform() {
    super();
  }

}
