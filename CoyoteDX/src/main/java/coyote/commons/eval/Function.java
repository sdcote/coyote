package coyote.commons.eval;

/**
 * A <a href="http://en.wikipedia.org/wiki/Function_(mathematics)">function</a>.
 */
public class Function {
  private final int maxArgumentCount;
  private final int minArgumentCount;
  private final String name;




  /**
   * This constructor builds a function with a fixed argument count.
   *
   * @param name The functions name
   * @param argumentCount The functions argument count.
   *
   * @throws IllegalArgumentException if argumentCount is lower than 0 or if the function name is null or empty.
   */
  public Function(final String name, final int argumentCount) {
    this(name, argumentCount, argumentCount);
  }




  /**
   * This constructor builds a function with a variable argument count.
   *
   * <p>For instance, a minimum function may have at least one argument.</p>
   *
   * @param name The functions name
   * @param minArgumentCount The functions minimum argument count.
   * @param maxArgumentCount The functions maximum argument count (Integer.MAX_VALUE to specify no upper limit).
   *
   * @throws IllegalArgumentException if minArgumentCount is less than 0 or greater than maxArgumentCount or if the function name is null or empty.
   */
  public Function(final String name, final int minArgumentCount, final int maxArgumentCount) {
    if ((minArgumentCount < 0) || (minArgumentCount > maxArgumentCount)) {
      throw new IllegalArgumentException("Invalid argument count");
    }
    if ((name == null) || (name.length() == 0)) {
      throw new IllegalArgumentException("Invalid function name");
    }
    this.name = name;
    this.minArgumentCount = minArgumentCount;
    this.maxArgumentCount = maxArgumentCount;
  }




  /**
   * @return the functions maximum argument count.
   */
  public int getMaximumArgumentCount() {
    return maxArgumentCount;
  }




  /**
   * @return the functions minimum argument count.
   */
  public int getMinimumArgumentCount() {
    return minArgumentCount;
  }




  /**
   * @return the name of the function
   */
  public String getName() {
    return name;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }

}