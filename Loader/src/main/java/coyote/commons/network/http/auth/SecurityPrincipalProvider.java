/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.auth;

import coyote.commons.security.CredentialSet;
import coyote.commons.security.SecurityPrincipal;


/**
 * Any component which retrieves security principals by their credential set.
 */
public interface SecurityPrincipalProvider {

  public SecurityPrincipal getPrincipal(CredentialSet credentials);
}
