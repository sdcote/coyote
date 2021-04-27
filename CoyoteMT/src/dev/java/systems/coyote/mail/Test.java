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
package systems.coyote.mail;

import java.util.Properties;
import java.util.Scanner;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;


/**
 * You have to configure the sending account to "Allow less secure apps" to 
 * perform a regular SSL\TLS connection as opposed to an OAuth login.
 */
public class Test {

  public static void main( String args[] ) {
    Scanner sc = new Scanner( System.in );
    System.out.print( "gmail username: " );
    String username = sc.next();
    System.out.print( "gmail password: " );
    String password = sc.next();
    System.out.print( "destination email address: " );
    String to = sc.next();
    System.out.print( "subject: " );
    String subject = sc.next();
    System.out.print( "email body: " );
    String email_body = sc.next();
    Test test = new Test();
    test.doSendMail( username, password, to, subject, email_body );
    sc.close();
  }




  // sends mail
  public void doSendMail( final String username, final String password, String to, String subject, String email_body ) {

    Properties props = new Properties();
    props.put( "mail.smtp.host", "smtp.gmail.com" );
    props.put( "mail.smtp.socketFactory.port", "587" );
    props.put( "mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
    props.put( "mail.smtp.auth", "true" );
    props.put( "mail.smtp.starttls.enable", "true" );
    props.put( "mail.smtp.port", "587" );

    Session session = Session.getDefaultInstance( props, new javax.mail.Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication( username, password );
      }
    } );
    try {
      Message message = new MimeMessage( session );
      message.setFrom( new InternetAddress( username ) );
      message.setRecipients( Message.RecipientType.TO, InternetAddress.parse( to ) );
      message.setSubject( subject );
      message.setText( email_body );
      Transport.send( message );
      System.out.println( "message sent" );
      JOptionPane.showMessageDialog( null, "Message Sent!", "Sent", JOptionPane.INFORMATION_MESSAGE );
    } catch ( Exception e ) {
      System.out.println( e );
      JOptionPane.showMessageDialog( null, e.toString() );
    }
  }
}