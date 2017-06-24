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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dx.CMT;
import coyote.dx.TaskException;
import coyote.dx.mail.AbstractMailProtocol;
import coyote.dx.mail.MailException;
import coyote.dx.mail.MailProtocol;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public class Mail extends AbstractMessageTask {

  private static final String MAIL_PROTOCOL_PKG = AbstractMailProtocol.class.getPackage().getName();




  /**
   * @see coyote.dx.task.AbstractTransformTask#execute()
   */
  @Override
  public void execute() throws TaskException {
    String protocol = getProtocol();
    String sender = getSender();
    String receiver = getReceiver();
    String subject = getSubject();
    String body = getBody();
    String attachment = getAttachment();

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
        Log.error( LogMsg.createMsg( CMT.MSG, "DX.instance_not_configurable", protocolClass ) );
        getContext().setError( "Ain't a mail protocol" );
        return;
      }
    } catch ( ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
      Log.error( LogMsg.createMsg( CMT.MSG, "DX.instantiation_error", protocolClass, e.getClass().getName(), e.getMessage() ) );
      getContext().setError( "Couldn't create mail protocol" );
      return;
    }

    Config config = new Config( (DataFrame)configuration.clone() );

    if ( StringUtil.isBlank( getSubject() ) ) {
      config.put( CMT.SUBJECT.toLowerCase(), "Data Exchange" );
    }

    // Create a new symbol table to prevent polluting the context with our values
    SymbolTable symbols = new SymbolTable();

    if ( getContext() != null && getContext().getSymbols() != null ) {
      symbols.merge( getContext().getSymbols() );
    }
    symbols.put( CMT.SENDER.toLowerCase(), sender );
    symbols.put( CMT.RECEIVER.toLowerCase(), receiver );

    String newSubject = Template.resolve( subject, symbols );
    config.put( CMT.SUBJECT, newSubject );

    String newBody = Template.resolve( body, symbols );
    config.put( CMT.BODY, newBody );

    String newAttach = Template.resolve( attachment, symbols );
    config.put( CMT.ATTACH, newAttach );

    mailProtocol.setConfiguration( config );

    try {
      mailProtocol.send();
    } catch ( MailException e ) {
      getContext().setError( e.getMessage() );
      Log.debug( ExceptionUtil.stackTrace( e ) );
    }
    finally {
      mailProtocol = null;
      config = null;
      symbols.clear();
      symbols = null;
    }

  }

}
