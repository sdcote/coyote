package coyote.commons;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Parsing strings into numbers
 */
public class NumberUtil {

  /**
   * Checks whether the String a parsable number.
   *
   * <p>Valid numbers include hexadecimal marked with the {@code 0x} or
   * {@code 0X} qualifier, octal numbers, scientific notation and
   * numbers marked with a type qualifier (e.g. 123L).
   *
   * <p>Non-hexadecimal strings beginning with a leading zero are
   * treated as octal values. Thus the string {@code 09} will return
   * {@code false}, since {@code 9} is not a valid octal value.
   * However, numbers beginning with {@code 0.} are treated as decimal.
   *
   * <p>{@code null} and empty/blank {@code String} will return
   * {@code false}.
   *
   * @param str  the {@code String} to check
   * 
   * @return {@code true} if the string is a correctly formatted number
   */
  public static boolean isNumeric(final String str) {
    if (StringUtil.isEmpty(str)) {
      return false;
    }
    final char[] chars = str.toCharArray();
    int sz = chars.length;
    boolean hasExponent = false;
    boolean hasDecimalPoint = false;
    boolean allowSigns = false;
    boolean foundDigit = false;
    final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
    if (sz > start + 1 && chars[start] == '0') {
      if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
        int i = start + 2;
        if (i == sz) {
          return false;
        }
        for (; i < chars.length; i++) {
          if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
            return false;
          }
        }
        return true;
      } else if (Character.isDigit(chars[start + 1])) {
        int i = start + 1;
        for (; i < chars.length; i++) {
          if (chars[i] < '0' || chars[i] > '7') {
            return false;
          }
        }
        return true;
      }
    }
    sz--;

    int i = start;
    while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
      if (chars[i] >= '0' && chars[i] <= '9') {
        foundDigit = true;
        allowSigns = false;

      } else if (chars[i] == '.') {
        if (hasDecimalPoint || hasExponent) {
          return false;
        }
        hasDecimalPoint = true;
      } else if (chars[i] == 'e' || chars[i] == 'E') {
        if (hasExponent) {
          return false;
        }
        if (!foundDigit) {
          return false;
        }
        hasExponent = true;
        allowSigns = true;
      } else if (chars[i] == '+' || chars[i] == '-') {
        if (!allowSigns) {
          return false;
        }
        allowSigns = false;
        foundDigit = false;
      } else {
        return false;
      }
      i++;
    }
    if (i < chars.length) {
      if (chars[i] >= '0' && chars[i] <= '9') {
        return true;
      }
      if (chars[i] == 'e' || chars[i] == 'E') {
        return false;
      }
      if (chars[i] == '.') {
        if (hasDecimalPoint || hasExponent) {
          return false;
        }
        return foundDigit;
      }
      if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
        return foundDigit;
      }
      if (chars[i] == 'l' || chars[i] == 'L') {
        return foundDigit && !hasExponent && !hasDecimalPoint;
      }
      return false;
    }
    return !allowSigns && foundDigit;
  }




  /**
   * Turns a string value into a java.lang.Number.
   *
   * <p>If the string starts with {@code 0x} or {@code -0x} (lower or upper 
   * case) or {@code #} or {@code -#}, it will be interpreted as a hexadecimal 
   * Integer - or Long, if the number of digits after the prefix is more than 
   * 8 - or BigInteger if there are more than 16 digits.
   *
   * <p>Then, the value is examined for a type qualifier on the end, i.e. one 
   * of {@code 'f','F','d','D','l','L'}. If it is found, it starts trying to 
   * create successively larger types from the type specified until one is 
   * found that can represent the value.
   *
   * <p>If a type specifier is not found, it will check for a decimal point 
   * and then try successively larger types from {@code Integer} to {@code 
   * BigInteger} and from {@code Float} to {@code BigDecimal}.
  *
   * <p>Integral values with a leading {@code 0} will be interpreted as octal; 
   * the returned number will be Integer, Long or BigDecimal as appropriate.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * <p>This method does not trim the input string, i.e., strings with leading
   * or trailing spaces will generate NumberFormatExceptions.
   *
   * @param str  String containing a number, may be null
   * 
   * @return Number created from the string (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static Number parse(final String str) throws NumberFormatException {
    if (str == null) {
      return null;
    }
    if (StringUtil.isBlank(str)) {
      throw new NumberFormatException("A blank string is not a valid number");
    }
    final String[] hexPrefixes = {"0x", "0X", "-0x", "-0X", "#", "-#"};
    int prefixLength = 0;
    for (final String prefix : hexPrefixes) {
      if (str.startsWith(prefix)) {
        prefixLength += prefix.length();
        break;
      }
    }
    if (prefixLength > 0) {
      char firstSigDigit = 0;
      for (int i = prefixLength; i < str.length(); i++) {
        firstSigDigit = str.charAt(i);
        if (firstSigDigit == '0') {
          prefixLength++;
        } else {
          break;
        }
      }
      final int hexDigits = str.length() - prefixLength;
      if ((hexDigits > 16) || ((hexDigits == 16) && (firstSigDigit > '7'))) {
        return parseBigInteger(str);
      }
      if ((hexDigits > 8) || ((hexDigits == 8) && (firstSigDigit > '7'))) {
        return parseLong(str);
      }
      return parseInteger(str);
    }
    final char lastChar = str.charAt(str.length() - 1);
    String mantissa;
    String decimal;
    String exponent;
    final int decimalPosition = str.indexOf('.');
    final int exponentPosition = str.indexOf('e') + str.indexOf('E') + 1;

    if (decimalPosition > -1) {
      if (exponentPosition > -1) {
        if ((exponentPosition < decimalPosition) || (exponentPosition > str.length())) {
          throw new NumberFormatException(str + " is not a valid number.");
        }
        decimal = str.substring(decimalPosition + 1, exponentPosition);
      } else {
        decimal = str.substring(decimalPosition + 1);
      }
      mantissa = getMantissa(str, decimalPosition);
    } else {
      if (exponentPosition > -1) {
        if (exponentPosition > str.length()) {
          throw new NumberFormatException(str + " is not a valid number.");
        }
        mantissa = getMantissa(str, exponentPosition);
      } else {
        mantissa = getMantissa(str);
      }
      decimal = null;
    }
    if (!Character.isDigit(lastChar) && (lastChar != '.')) {
      if ((exponentPosition > -1) && (exponentPosition < (str.length() - 1))) {
        exponent = str.substring(exponentPosition + 1, str.length() - 1);
      } else {
        exponent = null;
      }
      final String numeric = str.substring(0, str.length() - 1);
      final boolean allZeros = isAllZeros(mantissa) && isAllZeros(exponent);
      switch (lastChar) {
        case 'l':
        case 'L':
          if ((decimal == null) && (exponent == null) && (((numeric.charAt(0) == '-') && StringUtil.isDigits(numeric.substring(1))) || StringUtil.isDigits(numeric))) {
            try {
              return parseLong(numeric);
            } catch (final NumberFormatException nfe) {
              // Too big for a long
            }
            return parseBigInteger(numeric);
          }
          throw new NumberFormatException(str + " is not a valid number.");
        case 'f':
        case 'F':
          try {
            final Float f = NumberUtil.parseFloat(str);
            if (!(f.isInfinite() || ((f.floatValue() == 0.0F) && !allZeros))) {
              return f;
            }

          } catch (final NumberFormatException nfe) {
            // ignore the bad number
          }
          //fall-through
        case 'd':
        case 'D':
          try {
            final Double d = NumberUtil.parseDouble(str);
            if (!(d.isInfinite() || ((d.floatValue() == 0.0D) && !allZeros))) {
              return d;
            }
          } catch (final NumberFormatException nfe) {
            // ignore the bad number
          }
          try {
            return parseBigDecimal(numeric);
          } catch (final NumberFormatException e) {
            // ignore the bad number
          }
          // fall-through
        default:
          throw new NumberFormatException(str + " is not a valid number.");

      }
    }

    if ((exponentPosition > -1) && (exponentPosition < (str.length() - 1))) {
      exponent = str.substring(exponentPosition + 1, str.length());
    } else {
      exponent = null;
    }
    if ((decimal == null) && (exponent == null)) {
      try {
        return parseInteger(str);
      } catch (final NumberFormatException nfe) {
        // ignore the bad number
      }
      try {
        return parseLong(str);
      } catch (final NumberFormatException nfe) {
        // ignore the bad number
      }
      return parseBigInteger(str);
    }

    final boolean allZeros = isAllZeros(mantissa) && isAllZeros(exponent);
    try {
      final Float f = parseFloat(str);
      final Double d = parseDouble(str);
      if (!f.isInfinite() && !((f.floatValue() == 0.0F) && !allZeros) && f.toString().equals(d.toString())) {
        return f;
      }
      if (!d.isInfinite() && !((d.doubleValue() == 0.0D) && !allZeros)) {
        final BigDecimal b = parseBigDecimal(str);
        if (b.compareTo(BigDecimal.valueOf(d.doubleValue())) == 0) {
          return d;
        }
        return b;
      }
    } catch (final NumberFormatException nfe) {
      // ignore the bad number
    }
    return parseBigDecimal(str);
  }




  /**
   * Convert a {@code String} to a {@code BigDecimal}.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code BigDecimal} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static BigDecimal parseBigDecimal(final String str) {
    if (str == null) {
      return null;
    }
    if (StringUtil.isBlank(str)) {
      throw new NumberFormatException("A blank string is not a valid number");
    }
    if (str.trim().startsWith("--")) {
      throw new NumberFormatException(str + " is not a valid number.");
    }
    return new BigDecimal(str);
  }




  /**
   * Convert a {@code String} to a {@code Long}.
   * 
   * <p>This handles hex (0Xhhhh) and octal (0ddd) notations. N.B. a leading 
   * zero means octal; spaces are not trimmed.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code Long} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static Long parseLong(final String str) {
    if (str == null) {
      return null;
    }
    return Long.decode(str);
  }




  /**
   * Convert a {@code String} to a {@code Float}.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code Float} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static Float parseFloat(final String str) {
    if (str == null) {
      return null;
    }
    return Float.valueOf(str);
  }




  /**
   * Convert a {@code String} to a {@code Integer}.
   * 
   * <p> This handles hex (0xhhhh) and octal (0dddd) notations. N.B. a leading 
   * zero means octal; spaces are not trimmed.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code Integer} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static Integer parseInteger(final String str) {
    if (str == null) {
      return null;
    }
    return Integer.decode(str);
  }




  /**
   * Convert a {@code String} to a {@code BigInteger}.
   * 
   * <p>This handles hex (0x or #) and octal (0) notations.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code BigInteger} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static BigInteger parseBigInteger(final String str) {
    if (str == null) {
      return null;
    }
    int pos = 0;
    int radix = 10;
    boolean negate = false;
    if (str.startsWith("-")) {
      negate = true;
      pos = 1;
    }
    if (str.startsWith("0x", pos) || str.startsWith("0X", pos)) {
      radix = 16;
      pos += 2;
    } else if (str.startsWith("#", pos)) {
      radix = 16;
      pos++;
    } else if (str.startsWith("0", pos) && (str.length() > (pos + 1))) {
      radix = 8;
      pos++;
    }

    final BigInteger value = new BigInteger(str.substring(pos), radix);
    return negate ? value.negate() : value;
  }




  /**
   * Convert a {@code String} to a {@code Double}.
   *
   * <p>Returns {@code null} if the string is {@code null}.
   *
   * @param str  a {@code String} to convert, may be null
   * 
   * @return converted {@code Double} (or null if the input is null)
   * 
   * @throws NumberFormatException if the value cannot be converted
   */
  public static Double parseDouble(final String str) {
    if (str == null) {
      return null;
    }
    return Double.valueOf(str);
  }




  /**
   * Returns {@code true} if s is {@code null}.
   *
   * @param str  the String to check
   * 
   * @return true if it is all zeros or {@code null} false otherwise
   */
  private static boolean isAllZeros(final String str) {
    if (str == null) {
      return true;
    }
    for (int i = str.length() - 1; i >= 0; i--) {
      if (str.charAt(i) != '0') {
        return false;
      }
    }
    return str.length() > 0;
  }




  /**
   * Returns mantissa of the given number.
   *
   * @param str the string representation of the number
   * 
   * @return mantissa of the given number
   */
  private static String getMantissa(final String str) {
    return getMantissa(str, str.length());
  }




  /**
   * Returns mantissa of the given number.
   *
   * @param str the string representation of the number
   * @param stopPos the position of the exponent or decimal point
   * 
   * @return mantissa of the given number
   */
  private static String getMantissa(final String str, final int stopPos) {
    final char firstChar = str.charAt(0);
    final boolean hasSign = (firstChar == '-') || (firstChar == '+');

    return hasSign ? str.substring(1, stopPos) : str.substring(0, stopPos);
  }




  /**
   * Deal with infinite and NaN conditions in doubles and floats.
   * 
   * @param number the number to check
   * 
   * @return true if the number is an infinite value or Not a Number, false otherwise.
   */
  public static boolean isSpecial(Number number) {
    boolean specialDouble = number instanceof Double && (Double.isNaN((Double)number) || Double.isInfinite((Double)number));
    boolean specialFloat = number instanceof Float && (Float.isNaN((Float)number) || Float.isInfinite((Float)number));
    return specialDouble || specialFloat;
  }




  /**
   * Convert the given number object to a BigDecimal.
   * 
   * @param number the number to convert
   * 
   * @return a BigDecimal representation of the number
   * 
   * @throws IllegalArgumentException if the string representation of the number could not be parsed by BigDecimal
   */
  public static BigDecimal toBigDecimal(Number number) {
    if (number instanceof BigDecimal)
      return (BigDecimal)number;
    if (number instanceof BigInteger)
      return new BigDecimal((BigInteger)number);
    if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long)
      return new BigDecimal(number.longValue());
    if (number instanceof Float || number instanceof Double)
      return new BigDecimal(number.doubleValue());

    try {
      return new BigDecimal(number.toString());
    } catch (final NumberFormatException e) {
      throw new IllegalArgumentException("The given number (\"" + number + "\" of class " + number.getClass().getName() + ") does not have a parsable string representation", e);
    }
  }

}
