package coyote.commons.eval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The parameters of an evaluator.
 *
 * <p>An evaluator may have different parameters as the supported operators,
 * the supported functions, methods, constants, etc ...</p>
 */
public class Parameters {
  private String argumentSeparator;
  private final List<Constant> constants;
  private final List<BracketPair> expressionBrackets;
  private final List<BracketPair> functionBrackets;
  private final List<Function> functions;
  private final List<Method> methods;
  private final List<Operator> operators;
  private final Map<String, String> translations;




  /**
   * This constructor builds an instance with no operators, no functions, no
   * constants, no methods, no translations and no brackets.
   *
   * <p>Function and method argument separator is set to ','.
   */
  public Parameters() {
    operators = new ArrayList<Operator>();
    functions = new ArrayList<Function>();
    methods = new ArrayList<Method>();
    constants = new ArrayList<Constant>();
    translations = new HashMap<String, String>();
    expressionBrackets = new ArrayList<BracketPair>();
    functionBrackets = new ArrayList<BracketPair>();
    setFunctionArgumentSeparator(',');
  }




  /**
   * Adds a constant to the supported ones.
   *
   * @param constant The added constant
   */
  public void add(final Constant constant) {
    constants.add(constant);
  }




  /**
   * Adds a function to the supported ones.
   *
   * @param function The added function
   */
  public void add(final Function function) {
    functions.add(function);
  }




  /**
   * Adds an operator to the supported ones.
   *
   * @param operator The added operator
   */
  public void add(final Operator operator) {
    operators.add(operator);
  }




  /**
   * Adds constants to the supported ones.
   *
   * @param constants The constants to be added.
   */
  public void addConstants(final Collection<Constant> constants) {
    this.constants.addAll(constants);
  }




  /**
   * Adds a new bracket pair to the expression bracket list.
   *
   * @param pair A bracket pair
   */
  public void addExpressionBracket(final BracketPair pair) {
    expressionBrackets.add(pair);
  }




  /**
   * Adds bracket pairs to the expression bracket list.
   *
   * @param brackets The brackets to be added.
   */
  public void addExpressionBrackets(final Collection<BracketPair> brackets) {
    expressionBrackets.addAll(brackets);
  }




  /**
   * Adds a new bracket pair to the function bracket list.
   *
   * @param pair A bracket pair
   */
  public void addFunctionBracket(final BracketPair pair) {
    functionBrackets.add(pair);
  }




  /**
   * Adds bracket pairs to the function bracket list.
   *
   * @param brackets The brackets to be added.
   */
  public void addFunctionBrackets(final Collection<BracketPair> brackets) {
    functionBrackets.addAll(brackets);
  }




  /**
   * Adds functions to the supported ones.
   *
   * @param functions The functions to be added.
   */
  public void addFunctions(final Collection<Function> functions) {
    this.functions.addAll(functions);
  }




  /**
   * Adds methods to the supported ones.
   *
   * @param methods The methods to be added.
   */
  public void addMethods(final Collection<Method> methods) {
    this.methods.addAll(methods);
  }




  /**
   * Adds operators to the supported ones.
   *
   * @param operators The operators to be added.
   */
  public void addOperators(final Collection<Operator> operators) {
    this.operators.addAll(operators);
  }




  /**
   * @return the function argument separator.
   */
  public String getArgumentSeparator() {
    return argumentSeparator;
  }




  /**
   * @return the supported constants.
   */
  public Collection<Constant> getConstants() {
    return constants;
  }




  /**
   * @return the supported bracket pairs for expressions.
   */
  public Collection<BracketPair> getExpressionBrackets() {
    return expressionBrackets;
  }




  /**
   * @return the supported bracket pairs for functions.
   */
  public Collection<BracketPair> getFunctionBrackets() {
    return functionBrackets;
  }




  /**
   * @return the supported functions.
   */
  public Collection<Function> getFunctions() {
    return functions;
  }




  /**
   * @return the supported functions.
   */
  public Collection<Method> getMethods() {
    return methods;
  }




  /**
   * @return the supported operators.
   */
  public Collection<Operator> getOperators() {
    return operators;
  }




  /**
   * Sets the function argument separator. (The default value is ',')
   *
   * @param separator The new separator
   */
  public void setFunctionArgumentSeparator(final char separator) {
    argumentSeparator = new String(new char[]{separator});
  }




  /**
   * Sets the translated term for a constant.
   *
   * @param constant The constant you want to translate the name
   * @param translatedName The translated name
   *
   * @see #setTranslation(Function, String)
   */
  public void setTranslation(final Constant constant, final String translatedName) {
    setTranslation(constant.getName(), translatedName);
  }




  /**
   * Sets the translated term for a function.
   *
   * <p>Using this method, you can localize the names of some built-in
   * functions. For instance, for french people, you can use this method to use
   * "somme" instead of "sum" with the SUM built-in function of
   * DoubleEvaluator.</p>
   *
   * @param function The function you want to translate the name
   * @param translatedName The translated name
   *
   * @see DoubleEvaluator#SUM
   */
  public void setTranslation(final Function function, final String translatedName) {
    setTranslation(function.getName(), translatedName);
  }




  private void setTranslation(final String name, final String translatedName) {
    translations.put(name, translatedName);
  }




  protected String getTranslation(final String originalName) {
    final String translation = translations.get(originalName);
    return translation == null ? originalName : translation;
  }

}
