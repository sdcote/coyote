package coyote.commons;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;


/**
 * Immutable, arbitrary-precision signed decimal numbers designed for technical 
 * analysis.
 * 
 * <p>A {@code Decimal} consists of a {@code BigDecimal} with arbitrary {@link 
 * MathContext} (precision and rounding mode).
 *
 * @see BigDecimal
 * @see MathContext
 * @see RoundingMode
 */
public final class Decimal implements Comparable<Decimal>, Serializable {
  private static final long serialVersionUID = 7428455663065458570L;
  public static final MathContext MATH_CONTEXT = new MathContext(32, RoundingMode.HALF_UP);
  private static final DecimalFormat FORMATTER = new DecimalFormat();

  /** Not-a-Number instance (infinite error) */
  public static final Decimal NaN = new Decimal();

  public static final Decimal ZERO = valueOf(0);
  public static final Decimal ONE = valueOf(1);
  public static final Decimal TWO = valueOf(2);
  public static final Decimal THREE = valueOf(3);
  public static final Decimal TEN = valueOf(10);
  public static final Decimal HUNDRED = valueOf(100);
  public static final Decimal THOUSAND = valueOf(1000);

  /** The value we are wrapping */
  private final BigDecimal value;

  static {
    FORMATTER.setMaximumFractionDigits(8);
    FORMATTER.setMinimumFractionDigits(8);
    FORMATTER.setGroupingUsed(false);
    FORMATTER.setRoundingMode(RoundingMode.HALF_EVEN);
  }




  public static Decimal valueOf(final double val) {
    if (Double.isNaN(val)) {
      return NaN;
    }
    return new Decimal(val);
  }




  public static Decimal valueOf(final int val) {
    return new Decimal(val);
  }




  public static Decimal valueOf(final long val) {
    return new Decimal(val);
  }




  public static Decimal valueOf(final String val) {
    if ("NaN".equals(val)) {
      return NaN;
    }
    return new Decimal(val);
  }




  /**
   * Constructor unly used for NaN instance.
   */
  private Decimal() {
    value = null;
  }




  /**
   * Construct a decimal from the given BigDecimal value.
   *
   * @param val the BigDecimal value
   */
  private Decimal(final BigDecimal val) {
    value = val;
  }




  /**
   * Construct a decimal from the given double value.
   *
   * @param val the double value
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   * @throws NumberFormatException if val is infinite or NaN.
   */
  private Decimal(final double val) {
    value = new BigDecimal(val, MATH_CONTEXT);
  }




  /**
   * Construct a decimal from the given integer value.
   *
   * @param val the integer value
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   */
  private Decimal(final int val) {
    value = new BigDecimal(val, MATH_CONTEXT);
  }




  /**
   * Construct a decimal from the given long value.
   *
   * @param val the long value
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   */
  private Decimal(final long val) {
    value = new BigDecimal(val, MATH_CONTEXT);
  }




  /**
   * Construct a decimal from the given string value.
   *
   * @param val the string representation of the decimal value
   *
   * @throws ArithmeticException if the result is inexact but the rounding mode is UNNECESSARY.
   * @throws NumberFormatException  if val is not a valid representation of a BigDecimal.
   */
  private Decimal(final String val) {
    value = new BigDecimal(val, MATH_CONTEXT);
  }




  /**
   * Returns a {@code Decimal} whose value is the absolute value
   * of this {@code Decimal}.
   * @return {@code abs(this)}
   */
  public Decimal abs() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(value.abs());
  }




  @Override
  public int compareTo(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return 0;
    }
    return value.compareTo(other.value);
  }




  /**
   * Returns a {@code Decimal} whose value is {@code (this / divisor)}, with
   * rounding according to the context settings.
   *
   * @param divisor value by which this {@code Decimal} is to be divided.
   *
   * @return {@code this / divisor}, rounded as necessary
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   *
   * @see BigDecimal#divide(java.math.BigDecimal, java.math.MathContext)
   */
  public Decimal dividedBy(final Decimal divisor) {
    if ((this == NaN) || (divisor == NaN) || divisor.isZero()) {
      return NaN;
    }
    return new Decimal(value.divide(divisor.value, MATH_CONTEXT));
  }




  /**
   * {@inheritDoc}
   * Warning: This method returns true if `this` and `obj` are both NaN.
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Decimal)) {
      return false;
    }
    final Decimal other = (Decimal)obj;
    if (value != other.value && (value == null || (value.compareTo(other.value) != 0))) {
      return false;
    }
    return true;
  }




  @Override
  public int hashCode() {
    return Objects.hash(value);
  }




  /**
   * Checks if this value is equal to another.
   * @param other the other value, not null
   * @return true is this is greater than the specified value, false otherwise
   */
  public boolean isEqual(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return false;
    }
    return compareTo(other) == 0;
  }




  /**
   * Checks if this value is greater than another.
   * @param other the other value, not null
   * @return true is this is greater than the specified value, false otherwise
   */
  public boolean isGreaterThan(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return false;
    }
    return compareTo(other) > 0;
  }




  /**
   * Checks if this value is greater than or equal to another.
   * @param other the other value, not null
   * @return true is this is greater than or equal to the specified value, false otherwise
   */
  public boolean isGreaterThanOrEqual(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return false;
    }
    return compareTo(other) > -1;
  }




  /**
   * Checks if this value is less than another.
   * @param other the other value, not null
   * @return true is this is less than the specified value, false otherwise
   */
  public boolean isLessThan(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return false;
    }
    return compareTo(other) < 0;
  }




  /**
   * Checks if this value is less than or equal to another.
   * @param other the other value, not null
   * @return true is this is less than or equal to the specified value, false otherwise
   */
  public boolean isLessThanOrEqual(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return false;
    }
    return compareTo(other) < 1;
  }




  /**
   * Checks if the value is Not-a-Number.
   * @return true if the value is Not-a-Number (NaN), false otherwise
   */
  public boolean isNaN() {
    return this == NaN;
  }




  /**
   * Checks if the value is less than zero.
   * @return true if the value is less than zero, false otherwise
   */
  public boolean isNegative() {
    if (this == NaN) {
      return false;
    }
    return compareTo(ZERO) < 0;
  }




  /**
   * Checks if the value is zero or less.
   * @return true if the value is zero or less, false otherwise
   */
  public boolean isNegativeOrZero() {
    if (this == NaN) {
      return false;
    }
    return compareTo(ZERO) <= 0;
  }




  /**
   * Checks if the value is greater than zero.
   * @return true if the value is greater than zero, false otherwise
   */
  public boolean isPositive() {
    if (this == NaN) {
      return false;
    }
    return compareTo(ZERO) > 0;
  }




  /**
   * Checks if the value is zero or greater.
   * @return true if the value is zero or greater, false otherwise
   */
  public boolean isPositiveOrZero() {
    if (this == NaN) {
      return false;
    }
    return compareTo(ZERO) >= 0;
  }




  /**
   * Checks if the value is zero.
   * @return true if the value is zero, false otherwise
   */
  public boolean isZero() {
    if (this == NaN) {
      return false;
    }
    return compareTo(ZERO) == 0;
  }




  /**
   * Returns the correctly rounded natural logarithm (base e) of the <code>double</code> value of this {@code Decimal}.
   * <strong> Warning!</strong> Uses the {@code StrictMath#log(double)} method under the hood.
   * @return the natural logarithm (base e) of {@code this}
   * @see StrictMath#log(double)
   */
  public Decimal log() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(StrictMath.log(value.doubleValue()));
  }




  /**
   * Returns the maximum of this {@code Decimal} and {@code other}.
   * @param  other value with which the maximum is to be computed
   * @return the {@code Decimal} whose value is the greater of this
   *         {@code Decimal} and {@code other}.  If they are equal,
   *         as defined by the {@link #compareTo(Decimal) compareTo}
   *         method, {@code this} is returned.
   */
  public Decimal max(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return NaN;
    }
    return (compareTo(other) >= 0 ? this : other);
  }




  /**
   * Returns the minimum of this {@code Decimal} and {@code other}.
   * @param other value with which the minimum is to be computed
   * @return the {@code Decimal} whose value is the lesser of this
   *         {@code Decimal} and {@code other}.  If they are equal,
   *         as defined by the {@link #compareTo(Decimal) compareTo}
   *         method, {@code this} is returned.
   */
  public Decimal min(final Decimal other) {
    if ((this == NaN) || (other == NaN)) {
      return NaN;
    }
    return (compareTo(other) <= 0 ? this : other);
  }




  /**
   * Returns a {@code Decimal} whose value is {@code (this - augend)}, with
   * rounding according to the context settings.
   *
   * @param subtrahend value to be subtracted from this {@code Decimal}.
   *
   * @return {@code this - subtrahend}, rounded as necessary
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   *
   * @see BigDecimal#subtract(java.math.BigDecimal, java.math.MathContext)
   */
  public Decimal minus(final Decimal subtrahend) {
    if ((this == NaN) || (subtrahend == NaN)) {
      return NaN;
    }
    return new Decimal(value.subtract(subtrahend.value, MATH_CONTEXT));
  }




  /**
   * Returns a {@code Decimal} whose value is {@code this * multiplicand},
   * with rounding according to the context settings.
   *
   * @param multiplicand value to be multiplied by this {@code Decimal}.
   *
   * @return {@code this * multiplicand}, rounded as necessary
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   *
   * @see BigDecimal#multiply(java.math.BigDecimal, java.math.MathContext)
   */
  public Decimal multipliedBy(final Decimal multiplicand) {
    if ((this == NaN) || (multiplicand == NaN)) {
      return NaN;
    }
    return new Decimal(value.multiply(multiplicand.value, MATH_CONTEXT));
  }




  /**
   * Returns a {@code Decimal} whose value is {@code (this + augend)}, with
   * rounding according to the context settings.
   *
   * @param augend value to be added to this {@code Decimal}.
   *
   * @return {@code this + augend}, rounded as necessary
   *
   * @throws ArithmeticException if the result is inexact but the RoundingMode is UNNECESSARY.
   *
   * @see BigDecimal#add(java.math.BigDecimal, java.math.MathContext)
   */
  public Decimal plus(final Decimal augend) {
    if ((this == NaN) || (augend == NaN)) {
      return NaN;
    }
    return new Decimal(value.add(augend.value, MATH_CONTEXT));
  }




  /**
   * Returns a {@code Decimal} whose value is <tt>(this<sup>n</sup>)</tt>.
   * @param n power to raise this {@code Decimal} to.
   * @return <tt>this<sup>n</sup></tt>
   * @see BigDecimal#pow(int, java.math.MathContext)
   */
  public Decimal pow(final int n) {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(value.pow(n, MATH_CONTEXT));
  }




  /**
   * Returns a {@code Decimal} whose value is {@code (this % divisor)},
   * with rounding according to the context settings.
   * @param divisor value by which this {@code Decimal} is to be divided.
   * @return {@code this % divisor}, rounded as necessary.
   * @see BigDecimal#remainder(java.math.BigDecimal, java.math.MathContext)
   */
  public Decimal remainder(final Decimal divisor) {
    if ((this == NaN) || (divisor == NaN) || divisor.isZero()) {
      return NaN;
    }
    return new Decimal(value.remainder(divisor.value, MATH_CONTEXT));
  }




  /**
   * Returns the correctly rounded positive square root of the <code>double</code> value of this {@code Decimal}.
   * /!\ Warning! Uses the {@code StrictMath#sqrt(double)} method under the hood.
   * @return the positive square root of {@code this}
   * @see StrictMath#sqrt(double)
   */
  public Decimal sqrt() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(StrictMath.sqrt(value.doubleValue()));
  }




  
  /**
   * Converts this {@code Decimal} to a {@code double}.
   * 
   * @return this {@code Decimal} converted to a {@code double}
   * 
   * @see BigDecimal#doubleValue()
   */
  public double toDouble() {
    if (this == NaN) {
      return Double.NaN;
    }
    return value.doubleValue();
  }




  /**
   * Converts this {@code Decimal} to a {@code double} with only the given 
   * number of decimal places.
   * 
   * <p>This uses Half-Up rounding mode.
   * 
   * @param places the number of decimal places (i.e. precision) to return.
   * 
   * @return this {@code Decimal} converted to a {@code double}
   * 
   * @throws IllegalArgumentException if places contains a negative value
   * 
   * @see BigDecimal#doubleValue()
   */
  public double toDouble(int places) {
    return toDouble(places, RoundingMode.HALF_UP);
  }




  /**
   * Converts this {@code Decimal} to a {@code double} with only the given 
   * number of decimal places.
   * 
   * @param places the number of decimal places (i.e. precision) to return.
   * @param mode the rounding mode to use
   * 
   * @return this {@code Decimal} converted to a {@code double}
   * 
   * @throws IllegalArgumentException if places contains a negative value
   * 
   * @see BigDecimal#doubleValue()
   */
  public double toDouble(int places, RoundingMode mode) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    if (this == NaN) {
      return Double.NaN;
    }

    BigDecimal bd = new BigDecimal(Double.toString(value.doubleValue()));
    bd = bd.setScale(places, mode);
    return bd.doubleValue();
  }




  /**
   * This returns only the given number of decimal places WITHOUT ROUNDING.
   * 
   * @param places the number of decimal places to return
   * 
   * @return the value to only the given number of places.
   */
  public double toTruncatedDouble(int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    if (this == NaN) {
      return Double.NaN;
    }
    double scale = Math.pow(10, places);
    return Math.round(value.doubleValue() * scale) / scale;
  }




  @Override
  public String toString() {
    if (this == NaN) {
      return "NaN";
    }
    return FORMATTER.format(value);
  }




  /**
   * Retrieve only the whole value of this decimal.
   * 
   * <p>This effectively drops the fractional part of this decimal value.
   *  
   * @return only the whole portion of this decimal value.
   */
  public Decimal getWholePart() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(new BigDecimal(value.toBigInteger()));
  }




  /**
   * Retrive the fractional part of this 
   * 
   * @return only the fractional portion of this decimal value
   */
  public Decimal getFractionalPart() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(value.remainder(BigDecimal.ONE));
  }




  /**
   * Retrive the fractional part of this decimal as a whole number.
   * 
   * <p>This is useful when you want to retrieve 50 cents from 3.50 and not a 
   * fractional value (.5).
   * 
   * @return the fractional part of this decimal as a big integer. Imaginary 
   *         values (NaN) results in 0 being returned.
   */
  public Decimal getFractionalValue() {
    if (this == NaN) {
      return NaN;
    }
    return new Decimal(value.remainder(BigDecimal.ONE).movePointRight(value.scale()).abs());
  }




  /**
   * Rounds the value up to the next positive whole if it has a fractional 
   * part.
   * 
   * <p>Note that this uses a rounding mode of CEILING meaning this rounds 
   * towards positive infinity.
   * 
   * @return the next whole values value up towards positive infinity.
   */
  public Decimal roundUpToWhole() {
    BigDecimal retval = new BigDecimal(value.toString());
    return new Decimal(retval.setScale(0, RoundingMode.CEILING));
  }

}
