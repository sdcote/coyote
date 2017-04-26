/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus.message;

/**
 * The ClosureMessage class models a special type of packet that represents the
 * channel is closed.
 * 
 * <p>Channels should terminate operation upon receiving this packet and not 
 * allow any other packets to be processed from that point on.</p>
 * 
 * @author Stephan D. Cote' - Enterprise Architecture
 * @version $Revision:$
 */
public class ClosureMessage extends Message {

}
