/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

/**
 * 
 */
public class Platform {

  /** The prefix String for all Windows OS. */
  private static final String NAME_WINDOWS_PREFIX = "Windows";

  /**
   * The {@code os.name} System Property. Operating system name.
   * 
   * <p>Defaults to {@code null} if the runtime does not have security access 
   * to read this property or the property does not exist.
   * 
   * <p>This value is initialized when the class is loaded. If {@link 
   * System#setProperty(String,String)} or {@link 
   * System#setProperties(java.util.Properties)} is called after this class is 
   * loaded, the value will be out of sync with that System property.
   */
  public static final String getOpSysName(){
    return getSystemProperty("os.name");
  }




  /**
   * Is {@code true} if this is Windows.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isWindows() {
    return getOSMatchesName(NAME_WINDOWS_PREFIX);
  }




  /**
   * Is {@code true} if this is Linux.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static boolean isLinux() {
    return getOSMatchesName("Linux") || getOSMatchesName("LINUX");
  }




  /**
   * Is {@code true} if this is AIX.
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   * @since 2.0
   */
  public static final boolean isAIX() {
    return getOSMatchesName("AIX");
  }




  /**
   * Is {@code true} if this is HP-UX.
   * <p> The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isHPUX() {
    return getOSMatchesName("HP-UX");
  }




  /**
   * Is {@code true} if this is Irix.
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isIrix() {
    return getOSMatchesName("Irix");
  }




  /**
   * Is {@code true} if this is Mac.
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isMac() {
    return getOSMatchesName("Mac");
  }




  /**
   * Is {@code true} if this is Mac.
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isMacOSX() {
    return getOSMatchesName("Mac OS X");
  }




  /**
   * Is {@code true} if this is FreeBSD.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isFreeBSD() {
    return getOSMatchesName("FreeBSD");
  }




  /**
   * Is {@code true} if this is OpenBSD.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isOpenBSD() {
    return getOSMatchesName("OpenBSD");
  }




  /**
   * Is {@code true} if this is NetBSD.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isNetBSD() {
    return getOSMatchesName("NetBSD");
  }




  /**
   * Is {@code true} if this is Solaris.
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isSolaris() {
    return getOSMatchesName("Solaris");
  }




  /**
   * <p>
   * Is {@code true} if this is SunOS.
   * </p>
   * <p>
   * The field will return {@code false} if {@code NAME} is {@code null}.
   * </p>
   *
   * @since 2.0
   */
  public static final boolean isSunOS() {
    return getOSMatchesName("SunOS");
  }




  /**
   * Is {@code true} if this is a UNIX like system, as in any of AIX, HP-UX, 
   * Irix, Linux, MacOSX, Solaris or SUN OS.
   * 
   * <p>The field will return {@code false} if {@code NAME} is {@code null}.
   */
  public static final boolean isUnix() {
    return isAIX() || isHPUX() || isIrix() || isLinux() || isMacOSX() || isSolaris() || isSunOS() || isFreeBSD() || isOpenBSD() || isNetBSD();
  }




  /**
   * Gets a System property, defaulting to {@code null} if the property cannot 
   * be read.
   * 
   * <p>If a {@code SecurityException} is caught, the return value is {@code 
   * null} and a message is written to {@code System.err}.
   *
   * @param property the system property name
   * 
   * @return the system property value or {@code null} if a security problem 
   *         occurs
   */
  private static String getSystemProperty(final String property) {
    try {
      return System.getProperty(property);
    } catch (final SecurityException ex) {
      // we are not allowed to look at this property
      System.err.println("Caught a SecurityException reading the system property '" + property + "'; the SystemUtil property value will default to null.");
      return null;
    }
  }




  /**
   * Decides if the operating system matches.
   * 
   * @param osName the actual OS name
   * @param osNamePrefix the prefix for the expected OS name
   * 
   * @return true if matches, or false if not or can't determine
   */
  private static boolean isOSNameMatch(final String osName, final String osNamePrefix) {
    if (osName == null) {
      return false;
    }
    return osName.startsWith(osNamePrefix);
  }




  /**
   * Decides if the operating system matches.
   *
   * @param osNamePrefix the prefix for the os name
   * @return true if matches, or false if not or can't determine
   */
  private static boolean getOSMatchesName(final String osNamePrefix) {
    return isOSNameMatch(getOpSysName(), osNamePrefix);
  }

}
