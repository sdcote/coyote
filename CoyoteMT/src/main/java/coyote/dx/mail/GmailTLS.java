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
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import coyote.commons.FileUtil;
import coyote.commons.StringUtil;
import coyote.loader.log.Log;


/**
 * Sends mail through Gmail using SSL/TLS.
 */
public class GmailTLS extends AbstractMailProtocol implements MailProtocol {

  /**
   * @see coyote.dx.mail.AbstractMailProtocol#send()
   */
  @Override
  public void send() throws MailException {

    String username = getUsername();
    String password = getPassword();
    String sender = getSender();
    String receiver = getReceiver();
    File attachment = getAttachment();
    String body = getBody();
    String subject = getSubject();

    //    System.out.println( host );
    //    System.out.println( port );
    //    System.out.println( protocol );
    //    System.out.println( username );
    //    System.out.println( password );
    //    System.out.println( sender );
    //    System.out.println( receiver );
    //    System.out.println( attachment == null ? "No attachment" : attachment.getAbsolutePath() );
    //    System.out.println( body );

    if ( attachment != null && !attachment.exists() ) {
      Log.warn( "Cannot find attachment file '" + attachment.getAbsolutePath() + "' - skipping attachment" );
      attachment = null;
    }

    final Properties props = new Properties();
    props.put( "mail.smtp.auth", "true" );
    props.put( "mail.smtp.starttls.enable", "true" );
    props.put( "mail.smtp.host", getHost() );
    props.put( "mail.smtp.port", getPort() );

    final Session session = Session.getDefaultInstance( props, new javax.mail.Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication( username, password );
      }
    } );

    if ( attachment != null || StringUtil.isNotBlank( body ) ) {
      try {
        Message message = new MimeMessage( session );
        message.setFrom( new InternetAddress( sender ) );
        message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( receiver ) );
        message.setSubject( subject );
        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = null;

        if ( StringUtil.isNotBlank( body ) ) {
          messageBodyPart = new MimeBodyPart();
          messageBodyPart.setText( body );
          multipart.addBodyPart( messageBodyPart );
        }

        if ( attachment != null ) {
          messageBodyPart = new MimeBodyPart();
          DataSource source = new FileDataSource( attachment.getAbsolutePath() );
          messageBodyPart.setDataHandler( new DataHandler( source ) );
          messageBodyPart.setFileName( FileUtil.getName( attachment.getAbsolutePath() ) );
          multipart.addBodyPart( messageBodyPart );
        }

        message.setContent( multipart );
        Transport.send( message );
      } catch ( MessagingException e ) {
        throw new MailException( "Could not send mail message: " + e.getMessage(), e );
      }
    } else {
      try {
        final Message message = new MimeMessage( session );
        message.setFrom( new InternetAddress( sender ) );
        message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( receiver ) );
        message.setSubject( subject );
        if ( StringUtil.isNotBlank( body ) ) {
          message.setText( body );
        } else {
          Log.warn( "No attachment and null or empty message body - Sending mail with empty body" );
        }
        Transport.send( message );
      } catch ( final Exception e ) {
        throw new MailException( "Could not send mail message: " + e.getMessage(), e );
      }
    }
  }

}
