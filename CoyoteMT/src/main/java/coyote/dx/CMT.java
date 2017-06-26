/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dx;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import coyote.commons.StringUtil;
import coyote.commons.Version;
import coyote.dx.mail.AbstractMailProtocol;
import coyote.dx.mail.MailProtocol;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.loader.log.LogMsg.BundleBaseName;


/**
 * Coyote Mail Tools fixture.
 * 
 * Constants are placed here along with any static method useful across 
 * multiple classes in the project. 
 */
public class CMT {
  public static final Version VERSION = new Version( 0, 1, 1, Version.EXPERIMENTAL );
  public static final String NAME = "CMT";

  public static final String SENDER = "sender";
  public static final String RECEIVER = "receiver";
  public static final String ATTACH = "attach";
  public static final String SUBJECT = "subject";
  public static final String BODY = "body";

  private static final String MAIL_PROTOCOL_PKG = AbstractMailProtocol.class.getPackage().getName();

  public static final BundleBaseName MSG;

  static {
    MSG = new BundleBaseName( "CMTMsg" );
  }




  /**
   * Load the named email protocol module.
   * 
   * @param protocol case sensitive name of the email protocol to load.
   * 
   * @return an instance of the named protocol to be used in sending email.
   */
  public static MailProtocol loadProtocol( String protocol ) {
    String protocolClass = protocol;

    if ( protocolClass != null && StringUtil.countOccurrencesOf( protocolClass, "." ) < 1 ) {
      protocolClass = MAIL_PROTOCOL_PKG + "." + protocolClass;
    }

    MailProtocol mailProtocol = null;
    try {
      Class<?> clazz = Class.forName( protocolClass );
      Constructor<?> ctor = clazz.getConstructor();
      Object object = ctor.newInstance();
      if ( object instanceof MailProtocol ) {
        mailProtocol = (MailProtocol)object;
      } else {
        Log.error( LogMsg.createMsg( CMT.MSG, "MT.instance_not_protocol", protocolClass ) );
      }
    } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
      Log.error( LogMsg.createMsg( CMT.MSG, "MT.protocol_instantiation_error", protocolClass, e.getClass().getName(), e.getMessage() ) );
    }

    return mailProtocol;
  }

}
