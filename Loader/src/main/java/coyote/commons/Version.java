/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.commons;

/**
 * Simple version reporting mechanism.
 *
 * see also semver.org
 */
public class Version {

  private int major;
  private int minor;
  private int patch;
  private short release;

  /** General availability. */
  public static final short GENERAL = 4;
  /** Beta testing release. */
  public static final short BETA = 3;
  /** Alpha testing release. */
  public static final short ALPHA = 2;
  /** Active development; pre-testing, not released to anyone outside of the development team. */
  public static final short DEVELOPMENT = 1;

  /** Proof of Concept phase; pre-development, not released to anyone outside of the lab. */
  public static final short EXPERIMENTAL = 0;
  private static final String[] releaseNames = {"exp", "dev", "alpha", "beta", "ga"};




  /**
   * Create a version by parsing the major, minor, patch and release out of the
   * given string.
   *
   * This will never return null. If the string contains invalid data, the levels will be set to 0.
   *
   * @param text the text containing the version string
   *
   * @return a new Version object with its levels populated from the parsed string
   */
  public static Version createVersion(final String text) {
    final Version retval = new Version();

    if (text != null) {
      int mark = 0;
      int mode = 0;

      for (int i = 0; i < text.length(); i++) {
        if ((text.charAt(i) == '.') || (text.charAt(i) == ' ') || (text.charAt(i) == '-')) {
          try {
            switch (mode) {
              case 0:
                retval.setMajor(Integer.parseInt(text.substring(mark, i)));
                break;
              case 1:
                retval.setMinor(Integer.parseInt(text.substring(mark, i)));
                break;
              case 2:
                retval.setPatch(Integer.parseInt(text.substring(mark, i)));
                break;
              default:
                break;
            }
          } catch (final NumberFormatException nfe) {
            return retval;
          }
          mode++;
          mark = i + 1;
        }
      }

      try {
        switch (mode) {
          case 0:
            retval.setMajor(Integer.parseInt(text.substring(mark)));
            break;
          case 1:
            retval.setMinor(Integer.parseInt(text.substring(mark)));
            break;
          case 2:
            retval.setPatch(Integer.parseInt(text.substring(mark)));
            break;
          default:
            break;
        }
      } catch (final Exception ex) {
        // ignore
      }
    }
    return retval;
  }




  /**
   * Return the standard string representation of the given release level.
   *
   * @param level the release level to represent.
   *
   * @return human readable string representation of the release level
   */
  public static String getReleaseString(final short level) {
    if (level < GENERAL) {
      return releaseNames[level];
    }
    return releaseNames[GENERAL];
  }




  /**
   * Constructor Version
   */
  public Version() {
    major = 0;
    minor = 0;
    patch = 0;
    release = GENERAL;
  }




  /**
   * Constructor Version
   *
   * @param maj major level version
   * @param min minor level version
   * @param pch patch level version
   */
  public Version(final int maj, final int min, final int pch) {
    major = maj;
    minor = min;
    patch = pch;
    release = GENERAL;
  }




  /**
   * Constructor Version
   *
   * @param maj major level version
   * @param min minor level version
   * @param pch patch level version
   * @param rls release level version
   */
  public Version(final int maj, final int min, final int pch, final short rls) {
    major = maj;
    minor = min;
    patch = pch;
    if (rls > -1 && rls <= GENERAL) {
      release = rls;
    }
  }




  /**
   * Tests to see if this version is logically equal to the given version.
   *
   * @param std The version against which we test this object.
   *
   * @return True if this version is logically equal to the given version, false
   *         if this version is greater or less than the argument.
   */
  public boolean equals(final Version std) {
    return ((major == std.major) && (minor == std.minor) && (patch == std.patch) && (release == std.release));
  }




  /**
   * @return the major level of this release.
   */
  public int getMajor() {
    return major;
  }




  /**
   * @return The minor level of the release.
   */
  public int getMinor() {
    return minor;
  }




  /**
   * @return the patch level of this release.
   */
  public int getPatch() {
    return patch;
  }




  /**
   * @return Returns the release.
   */
  public short getRelease() {
    return release;
  }




  /**
   * Tests to see if this version is logically greater than or equal to the
   * given version.
   *
   * @param std The version against which we test this object.
   * @return True if this version is logically greater than or equal to the
   *         given version, false if this version is less than the argument.
   */
  public boolean isAtLeast(final Version std) {
    if (major > std.major) {
      return true;
    } else {
      if (major == std.major) {
        if (minor > std.minor) {
          return true;
        } else {
          if (minor == std.minor && patch > std.patch) {
            return true;
          }
        }
      }
    }
    return false;
  }




  /**
   * Tests to see if this version is logically less than or equal to the given
   * version.
   *
   * @param std The version against which we test this object.
   *
   * @return True if this version is logically less than or equal to the given
   *         version, false if this version is greater than the argument.
   */
  public boolean isAtMost(final Version std) {
    if (major < std.major) {
      return true;
    } else {
      if (major == std.major) {
        if (minor < std.minor) {
          return true;
        } else {
          if (minor == std.minor && patch < std.patch) {
            return true;
          }
        }
      }
    }
    return false;
  }




  /**
   * Set the major level of this release
   *
   * @param i the level to set
   */
  public void setMajor(final int i) {
    major = i;
  }




  /**
   * Set the minor level of this release
   *
   * @param i the level to set
   */
  public void setMinor(final int i) {
    minor = i;
  }




  /**
   * Set the patch level of this release
   *
   * @param i the level to set
   */
  public void setPatch(final int i) {
    patch = i;
  }




  /**
   * @param release The release to set.
   */
  public void setRelease(final short release) {
    if (release > -1 && release >= GENERAL) {
      this.release = release;
    }
  }




  /**
   * @return a string representation of the version suitable for display, logging and later parsing.
   */
  @Override
  public String toString() {
    final StringBuffer retval = new StringBuffer(Integer.toString(major));
    retval.append(".");
    retval.append(Integer.toString(minor));
    retval.append(".");
    retval.append(Integer.toString(patch));

    if (release < GENERAL) {
      retval.append("-" + getReleaseString(release));
    }

    return retval.toString();
  }

}