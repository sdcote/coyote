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

import java.io.File;

import coyote.commons.CipherUtil;
import coyote.dx.CMT;
import coyote.dx.ConfigTag;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * 
 */
public abstract class AbstractMailProtocol implements MailProtocol {

  protected Config configuration = new Config();




  public void setConfiguration( Config config ) {
    if ( config == null ) {
      configuration = new Config();
    } else {
      configuration = config;
    }
  }




  protected String getHost() {
    if ( configuration.containsIgnoreCase( ConfigTag.HOST ) ) {
      return configuration.getString( ConfigTag.HOST );
    }
    return null;
  }




  protected int getPort() {
    if ( configuration.containsIgnoreCase( ConfigTag.PORT ) ) {
      try {
        return configuration.getInt( ConfigTag.PORT );
      } catch ( NumberFormatException e ) {
        Log.error( "'" + ConfigTag.PORT + "' configuration value of '" + configuration.getString( ConfigTag.PORT ) + "' is not a valid integer" );
      }
    }
    return -1;
  }




  protected String getProtocol() {
    if ( configuration.containsIgnoreCase( ConfigTag.PROTOCOL ) ) {
      return configuration.getString( ConfigTag.PROTOCOL );
    }
    return null;
  }




  protected String getSubject() {
    if ( configuration.containsIgnoreCase( CMT.SUBJECT ) ) {
      return configuration.getString( CMT.SUBJECT );
    }
    return null;
  }




  protected String getPassword() {
    if ( configuration.containsIgnoreCase( ConfigTag.PASSWORD ) ) {
      return configuration.getString( ConfigTag.PASSWORD );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) ) {
      return CipherUtil.decryptString( configuration.getString( Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD ) );
    } else {
      return null;
    }
  }




  protected String getUsername() {
    if ( configuration.containsIgnoreCase( ConfigTag.USERNAME ) ) {
      return configuration.getString( ConfigTag.USERNAME );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ) ) {
      return CipherUtil.decryptString( configuration.getString( Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME ) );
    } else {
      return null;
    }
  }




  protected String getSender() {
    if ( configuration.containsIgnoreCase( CMT.SENDER ) ) {
      return configuration.getString( CMT.SENDER );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + CMT.SENDER ) ) {
      return CipherUtil.decryptString( configuration.getString( Loader.ENCRYPT_PREFIX + CMT.SENDER ) );
    } else {
      return null;
    }
  }




  protected String getReceiver() {
    if ( configuration.containsIgnoreCase( CMT.RECEIVER ) ) {
      return configuration.getString( CMT.RECEIVER );
    } else if ( configuration.containsIgnoreCase( Loader.ENCRYPT_PREFIX + CMT.RECEIVER ) ) {
      return CipherUtil.decryptString( configuration.getString( Loader.ENCRYPT_PREFIX + CMT.RECEIVER ) );
    } else {
      return null;
    }
  }




  protected File getAttachment() {
    if ( configuration.containsIgnoreCase( CMT.ATTACH ) ) {
      return new File( configuration.getString( CMT.ATTACH ) );
    } else {
      return null;
    }
  }




  /**
   * Retrieve the message body.
   * 
   * <p>This is treated as a template and will be resolved to the current context 
   * before the message is sent.
   * 
   * @return the body of the message
   */
  protected String getBody() {
    if ( configuration.containsIgnoreCase( CMT.BODY ) ) {
      return configuration.getString( CMT.BODY );
    }
    return null;
  }




  public void send() throws MailException {

  }

}
