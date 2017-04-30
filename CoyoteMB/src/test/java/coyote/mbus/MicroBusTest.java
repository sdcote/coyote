/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import coyote.commons.ExceptionUtil;
import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;
import coyote.mbus.message.Message;
import coyote.mbus.network.MessageChannel;
import coyote.mbus.network.MessageSession;


/**
 * 
 */
public class MicroBusTest {
  MicroBus mbus = null;




  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    mbus = new MicroBus();
    mbus.enableLogging( true );
  }




  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    mbus.close();
    mbus = null;
  }




  /**
   * Test method for {@link coyote.mbus.MicroBus#getLocalNode()}.
   */
  @Test
  public void differentPort() {
    // Set the default port to 54321
    mbus.setPort( 54321 );
    mbus.open();

    // Have the MicroBus create a channel we can use to receive messages. This
    // call will set a MessageSink (ListenAll) to handle all received messages.
    final MessageChannel channel = mbus.createChannel( new MessageHandler() );

    // Join all channels by using the wildcard of '>' for a group name
    channel.join( ">" );
    System.out.println( "Listening to network packets" );

    // run for 5 seconds
    try {
      Thread.sleep( 5000 );
    } catch ( final InterruptedException e ) {
      e.printStackTrace();
    }

    // Close the MicroBus node to network traffic
    mbus.close();

  }




  /**
   * Test the convenience methods which allow for the simple publication of 
   * messages.
   */
  @Test
  public void simpleSendTest() {
    Message msg = new Message();
    msg.setGroup( "OpenTopic" );
    msg.add( "MyField", "MyValue" );

    mbus.send( msg );

    mbus.send( msg, "DifferentGroup" );
  }




  //@Test
  public void testPrivateGroup() {
    final MessageChannel channel = mbus.createChannel( new MessageHandler() );

    final String privateGroup = channel.createPrivateGroup();
    System.out.println( "Our private group name is '" + privateGroup + "'" );
    channel.join( privateGroup );

    try {
      // try to join a group name not created by this transport
      channel.join( privateGroup + ".XXX" );
    } catch ( final RuntimeException e ) {
      System.out.println( "Private group names are really private: '" + e.getMessage() + "'" );
    }

  }




  //@Test
  public void privateBus() {
    // The MicroBus defaults to a delivery context limited to the JRE. Messages 
    // are only passed to the network if properly configured and the open() 
    // method is called. To keep the message passing limited to the JRE, simply 
    // do not call MicroBus.getInstance().open() or any of the other open() 
    // signatures.

    // create a channel through which we send our data
    final MessageChannel channel = mbus.createChannel();

    // Setup a listener on another channel to observe data published
    mbus.createChannel( new MessageHandler() ).join( "PRIVATE" );

    // Create a packet to send through the bus
    final Message msg = new Message( "Payload", "XOXOXOXOXOXOXOXO" );
    msg.setGroup( "PRIVATE" );

    // send the message, the local listener should receive the message but no
    // other remote listener should see it nor should any UDP datagrams be 
    // observed on the network.
    channel.send( msg );

  }




  //@Test
  public void simpleSession() {
    mbus.enableLogging( true );
    mbus.open();

    // Access the MicroBus class to start it operating and accepting connections
    // from this host only
    mbus.addAclEntry( IpNetwork.getLocalHost(), true );

    // wait for the MicroBus components to spin-up
    mbus.waitForBus( 5000 );

    // Create a new session using an instance of this class as a listener when 
    // events arrive. This will start the session in a background daemon thread 
    // to push packets received from the network to the packet sink.
    final MessageSession session = new MessageSession( new MessageHandler() );

    try {
      // Create a URI which represents this hosts MicroBus TCP service
      final URI serviceUri = new URI( "tcp://" + mbus.getTcpAddress() + ":" + mbus.getTcpPort() );

      // Tell the session to connect via the URI
      System.out.println( "Connecting to " + serviceUri + "..." );
      session.connect( serviceUri );
      System.out.println( "Session is running in separate thread" );

      // Tell the session that we want to start receiving messages
      System.out.println( "Subscribing to Health Status" );
      session.join( "HealthStatus" );
      Thread.sleep( 15000 );

      System.out.println( "Unsubscribing from Health Status" );
      session.leave( "HealthStatus" );
      Thread.sleep( 15000 );

      System.out.println( "Publishing to 'Login'" );

      for ( int i = 0; i < 1; i++ ) {
        final Message msg = new Message();
        msg.add( "ACTION", "LOGIN" );
        msg.add( "Account", "bob" );
        msg.add( "Password", "BobsPassword" );
        msg.setGroup( "Login" );
        session.send( msg );
        System.out.println( "Packet sent" );
      }

      Thread.sleep( 1000 );
    } catch ( final Exception e ) {
      System.err.println( "Error: " + e.getMessage() + "\r\n" + ExceptionUtil.stackTrace( e ) );
    }
    finally {
      // Disconnect from the service
      System.out.println( "Disconnecting..." );
      session.disconnect();
    }

    System.out.println( "Done" );
  }




  /**
   * This test illustrates how the MicroBus can be set to run on a specific IP 
   * address n multi-homed machines. This is handy when messages of one type 
   * are to be placed on one subnet and the rest on another.   
   */
  //@Test
  public void specificBind() {
    mbus.enableLogging( true );

    // Set the default port to 54321
    mbus.setPort( 54321 );

    // Set the bind address to a specific IP address
    try {
      mbus.setBindAddress( new IpAddress( "192.168.100.101" ) );
    } catch ( IpAddressException e ) {
      e.printStackTrace();
    }

    // Set the netmask to a specific mask
    try {
      mbus.setNetMask( new IpAddress( "255.0.0.0" ) );
    } catch ( IpAddressException e ) {
      e.printStackTrace();
    }

    // Open the MicroBus node so we can receive message from the network using 
    // the above IP, Port and NetMask
    mbus.open();

    System.out.println( "Listening to network packets" );

    final MessageChannel channel = mbus.createChannel( new MessageHandler() );

    channel.join( ">" );

    // run for 2 minutes
    try {
      Thread.sleep( 120000 );
    } catch ( final InterruptedException e ) {
      e.printStackTrace();
    }

    // Close the MicroBus node to network traffic
    mbus.close();

  }

}
