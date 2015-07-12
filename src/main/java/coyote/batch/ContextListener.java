/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch;

/**
 * Listens to the operational context an performs actions on different events.
 */
public interface ContextListener extends ConfigurableComponent {

  void onEnd( OperationalContext context );




  void onStart( OperationalContext context );




  void onWrite( TransactionContext context );




  void onRead( TransactionContext context );




  void onError( OperationalContext context );

}
