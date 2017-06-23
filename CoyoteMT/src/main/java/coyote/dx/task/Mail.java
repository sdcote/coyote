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
package coyote.dx.task;

import java.io.File;

import coyote.dx.TaskException;


/**
 * 
 */
public class Mail extends AbstractMessageTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    String host = getHost();
    int port = getPort();
    String protocol = getProtocol();
    String username = getUsername();
    String password = getPassword();
    String sender = getSender();
    String receiver = getReceiver();
    File attachment = getAttachment();
    String body = getBody();

    System.out.println( host );
    System.out.println( port );
    System.out.println( protocol );
    System.out.println( username );
    System.out.println( password );
    System.out.println( sender );
    System.out.println( receiver );
    System.out.println( attachment == null ? "No attachment" : attachment.getAbsolutePath() );
    System.out.println( body );

  }

}
