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

import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dx.CMT;
import coyote.dx.TaskException;
import coyote.dx.mail.MailException;
import coyote.dx.mail.MailProtocol;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * 
 */
public class Mail extends AbstractMessageTask {

  /**
   * @see coyote.dx.task.AbstractTransformTask#performTask()
   */
  @Override
  protected void performTask() throws TaskException {
    String protocol = getProtocol();
    String sender = getSender();
    String receiver = getReceiver();
    String subject = getSubject();
    String body = getBody();
    String attachment = getAttachment();

    MailProtocol mailProtocol = CMT.loadProtocol( protocol );
    if ( mailProtocol == null ) {
      getContext().setError( "Could not load email protocol of '" + protocol + "'" );
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

    if ( StringUtil.isNotBlank( attachment ) ) {
      String newAttach = Template.resolve( attachment, symbols );
      config.put( CMT.ATTACH, newAttach );
    }

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
