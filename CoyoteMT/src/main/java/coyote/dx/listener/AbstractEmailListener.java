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
package coyote.dx.listener;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataFrame;
import coyote.dx.CMT;
import coyote.dx.ConfigTag;
import coyote.dx.context.ContextListener;
import coyote.dx.context.OperationalContext;
import coyote.dx.mail.MailException;
import coyote.dx.mail.MailProtocol;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This sends email when the transform completes.
 * 
 * <p>The normal use case is to send an email if a job fails, but there are 
 * also times when people want to be notified when a particular job runs and 
 * its output is ready to be viewed or processed. This listener supports both. 
 * 
 * <p>The status of the context is a textual description of the state of the 
 * context. For example "Pre-Processing Error" might indicate the context 
 * errored during pre-processing tasks.
 * 
 * <p>The error message is a more detailed message as to what the error was 
 * and why.
 */
public abstract class AbstractEmailListener extends AbstractListener implements ContextListener {

  abstract String getDefaultSubject();




  abstract String getDefaultBody();




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




  protected String getSubject() {
    String retval = getString( CMT.SUBJECT );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getDefaultSubject();
    }
    return retval;
  }




  /**
   * Retrieve the message body.
   * 
   * @return the body of the message
   */
  protected String getBody() {
    String retval = getString( CMT.BODY );
    if ( StringUtil.isBlank( retval ) ) {
      retval = getDefaultBody();
    }
    return retval;
  }




  protected void sendMessage( OperationalContext context ) {

    MailProtocol mailProtocol = CMT.loadProtocol( getProtocol() );

    if ( mailProtocol == null ) {
      Log.error( "Email notification on engine completion failed" );
      return;
    }

    String sender = getSender();
    String receiver = getReceiver();
    String subject = getSubject();
    String body = getBody();

    Config config = new Config( (DataFrame)configuration.clone() );

    if ( StringUtil.isBlank( subject ) ) {
      config.put( CMT.SUBJECT.toLowerCase(), "Data Exchange Job Status" );
    }
    if ( StringUtil.isBlank( body ) ) {
      if ( context.isInError() ) {
        config.put( CMT.BODY.toLowerCase(), "Data Exchange Job '" + getContext().getSymbols().getString( "JobName" ) + "' Failed" );
      } else {
        config.put( CMT.BODY.toLowerCase(), "Data Exchange Job '" + getContext().getSymbols().getString( "JobName" ) + "' Completed Successfully" );
      }
    }

    getContext().getSymbols().put( CMT.SENDER.toLowerCase(), sender );
    getContext().getSymbols().put( CMT.RECEIVER.toLowerCase(), receiver );

    String newSubject = Template.resolve( subject, getContext().getSymbols() );
    config.put( CMT.SUBJECT, newSubject );

    String newBody = Template.resolve( body, getContext().getSymbols() );
    config.put( CMT.BODY, newBody );

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
    }

  }

}
