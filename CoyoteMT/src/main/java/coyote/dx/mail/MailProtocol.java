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
package coyote.dx.mail;

import coyote.loader.cfg.Config;


/**
 * Defines a contract between mail components and modules providing specific 
 * mail exchange protocols.
 * 
 * <p>The concept is a component will want to send a mail message in an 
 * abstract manner and that the exact protocol of message exchange is 
 * different across organizations. Therefore a library of common exchange 
 * patterns (i.e. protocols) is needed to handle the different cases. This 
 * interface allow components to abstract away the detail of the protocols 
 * and allo custom protocols to be developed.
 * 
 * <p>Configuration details will be different for each of the protocols, but 
 * this is abstracted from the components with the Config classes and the use 
 * of abstract data types. The component can pass along the configuration to 
 * the protocol classes with minor adjustments to meet the needs of the 
 * installation.
 */
public interface MailProtocol {

  /**
   * Set the configuration of the protocol.
   * 
   * <p>Although the configuration is expected to be passed directly from the 
   * job, the component may alter the job configuration to meet the needs of 
   * the installation. For example, any templated values (e.g. message body) 
   * may be resolved before being placed in the configuration.
   * 
   * @param config The configuration the mail protocol is to use for sending 
   *        the message.
   */
  void setConfiguration( Config config );




  /**
   * Sens an email message based on the currently set configuration.
   * 
   * @throws MailException if the message could not be sent.
   */
  void send() throws MailException;

}
