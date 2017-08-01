/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.auth;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * A combination of authentication (AuthN) and role-based authorization (AuthZ)
 * must be performed before accessing this method.
 */
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Auth{

  /**
   * The group(s) to which the authenticated user must belong to gain access to
   * this method.
   *
   * @return the names of the groups delimited with a comma to which the
   *     identity must belong before access is granted to this method.
   */
  public String groups() default "";




  /**
   * Is authentication required for this method.
   *
   * <p>The identity of the caller must be established with the system before
   * access is granted. In other words, any logged-in user can access this
   * method. Public, anonymous access is not allowed.
   *
   * @return true (default) if access requires authentication, false if
   *     anonymous access is allowed.
   */
  public boolean required() default true;




  /**
   * Access to this method requires SSL.
   *
   * @return true (default) if the connection is required to be established
   *     via SSL, false if an unencrypted connection is allowed.
   */
  public boolean requireSSL() default false;

}
