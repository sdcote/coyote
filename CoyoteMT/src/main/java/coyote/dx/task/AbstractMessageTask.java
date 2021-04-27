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

import coyote.commons.CipherUtil;
import coyote.commons.StringUtil;
import coyote.dx.CMT;
import coyote.dx.ConfigTag;
import coyote.loader.Loader;


/**
 * 
 */
public abstract class AbstractMessageTask extends AbstractTransformTask {

  protected String getProtocol() {
    return getString( ConfigTag.PROTOCOL );
  }




  protected String getSender() {
    String retval = getString( CMT.SENDER );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getString( Loader.ENCRYPT_PREFIX + CMT.SENDER );
      if ( StringUtil.isNotBlank( retval ) ) {
        return CipherUtil.decryptString( retval );
      }
    }
    return retval;
  }




  protected String getReceiver() {
    String retval = getString( CMT.RECEIVER );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getString( Loader.ENCRYPT_PREFIX + CMT.RECEIVER );
      if ( StringUtil.isNotBlank( retval ) ) {
        return CipherUtil.decryptString( retval );
      }
    }
    return retval;
  }

  protected String getUsername() {
    String retval = getString( ConfigTag.USERNAME );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getString( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME );
      if ( StringUtil.isNotBlank( retval ) ) {
        return CipherUtil.decryptString( retval );
      }
    }
    return retval;
  }

  protected String getPassword() {
    String retval = getString( ConfigTag.PASSWORD );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getString( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD );
      if ( StringUtil.isNotBlank( retval ) ) {
        return CipherUtil.decryptString( retval );
      }
    }
    return retval;
  }


  protected String getSubject() {
    return getString( CMT.SUBJECT );
  }




  /**
   * Retrieve the name of the file to use as an attachment.
   * 
   * @return fully-qualified path to the file, or null if no attachment is to 
   *         be sent.
   */
  protected String getAttachment() {
    return getString( CMT.ATTACH );
  }




  /**
   * Retrieve the message body.
   * 
   * @return the body of the message
   */
  protected String getBody() {
    return getString( CMT.BODY );
  }

}
