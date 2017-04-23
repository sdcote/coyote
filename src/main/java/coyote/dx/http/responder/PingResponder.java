/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx.http.responder;

import coyote.commons.network.http.responder.Responder;


/**
 * Ping gives metrics for the Service
 * 
 * <p>Ping/:id returns the metrics for the identified component (e.g. 
 * job) running in the service.
 */
public class PingResponder extends AbstractBatchResponder implements Responder {

}
