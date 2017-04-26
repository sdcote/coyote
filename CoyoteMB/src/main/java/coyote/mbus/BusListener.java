/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.mbus;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import coyote.commons.network.IpInterface;
import coyote.mbus.network.Packet;


/**
 * The BusListener class models a utility that will listen for datagrams on a 
 * network and parse them into mBus packets for display and logging.
 * 
 * <p>This utility is helpful in trouble shooting messaging issues.</p>
 */
public class BusListener implements Runnable {
  private static final String CLASS_TAG = "BusListener";
  private static final String LF = System.getProperty( "line.separator" );
  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );

  private static volatile boolean shutdown = false;
  private static final int DATAGRAM_RECEIVE_BUFFER_SIZE = 131072;
  private InetAddress host = null;
  private int port = 7943;
  DatagramChannel channel = null;
  SocketAddress sa = null;
  private ByteBuffer buffer;
  static final int DATAGRAM_MTU = 65507;
  private boolean debug = false;




  public BusListener( final String[] args ) {
    // Add a shutdown hook into the JVM to help us shut everything down nicely
    try {
      Runtime.getRuntime().addShutdownHook( new Thread( "MicroBusShutdown" ) {
        public void run() {
          synchronized( BusListener.CLASS_TAG ) {
            BusListener.shutdown();
          }
        }
      } );
    } catch ( final Throwable ignoree ) {}

    parseArgs( args );
  }




  private static void usage( final String message ) {
    System.err.println( System.getProperty( "line.separator" ) + message );
    System.err.println( System.getProperty( "line.separator" ) + "Usage: java -jar buslistener.jar [-h][-i][-p]" );
    System.err.println( "  -h\tthis screen" );
    System.err.println( "  -i\tIP address to which the listener binds (defaults to primary interface)" );
    System.err.println( "  -p\tport on which to listen (defaults to 7943)" );
    System.err.println();
    System.err.flush();
    System.exit( 1 );
  }




  /**
   * Get the command-line parameters.
   *
   * @param args
   */
  private void parseArgs( final String[] args ) {
    int i = 0;

    while ( i < args.length ) {
      if ( args[i].equals( "-h" ) ) {
        BusListener.usage( "This utility listens to packets on the network and parses those packets into\nmessages. The output can be used to verify messages are actually being seen\non the bus and are available to other nodes." );
      } else if ( args[i].equals( "-d" ) ) {
        debug = true;
      }
      // Component Identifier
      else if ( args[i].equals( "-p" ) ) {
        if ( args.length > ( i + 1 ) ) {
          i++;
          port = Integer.parseInt( args[i] );
          System.out.println( "Using port " + port );
        } else {
          BusListener.usage( "Port flag (-p) requires additional parameter - exiting" );
        }
      } else if ( args[i].equals( "-i" ) ) {
        if ( args.length > ( i + 1 ) ) {
          i++;
          try {
            host = InetAddress.getByName( args[i] );
            System.out.println( "Using host " + host.getHostAddress() );
          } catch ( final UnknownHostException e ) {
            System.err.println( "Could not resolve host IP of " + args[i] + " - exiting" );
            System.exit( 1 );
          }
        } else {
          BusListener.usage( "IP flag (-i) requires additional parameter - exiting" );
        }
      } else {
        BusListener.usage( "Unknown argument '" + args[i] + "'" );
      }

      // shift the arguments
      i++;
    }

    if ( debug ) {
      System.out.println( "Debug messages enabled" );
    }
  }




  public static void shutdown() {
    BusListener.shutdown = true;
  }




  public void run() {
    if ( host == null ) {
      // set things up
      try {
        // host = InetAddress.getLocalHost();
        // host = IpAddress.getLocalAddress().toInetAddress();
        host = IpInterface.getPrimary().getAddress().toInetAddress();

        System.out.println( "Detected primary interface: " + IpInterface.getPrimary() );
      } catch ( final Exception e1 ) {
        BusListener.usage( e1.getLocalizedMessage() + " - exiting" );
      }
    }

    System.out.println( "Listening on " + host + " port:" + port + " for packets" );

    sa = new InetSocketAddress( host, port );

    buffer = ByteBuffer.allocate( BusListener.DATAGRAM_MTU );

    try {
      // Open a datagram channel so we can get an unbound Datagram Socket
      channel = DatagramChannel.open();
      channel.socket().setBroadcast( true );
      channel.socket().setReceiveBufferSize( BusListener.DATAGRAM_RECEIVE_BUFFER_SIZE );
      channel.socket().setReuseAddress( true );
      channel.socket().setSoTimeout( 500 );
    } catch ( final Exception e1 ) {
      BusListener.usage( e1.getLocalizedMessage() );
    }

    try {
      System.out.println( "Sniffer binding to " + sa );
      channel.socket().bind( sa );
    } catch ( final SocketException e1 ) {
      BusListener.usage( e1.getLocalizedMessage() );
    }

    long counter = 0;
    InetSocketAddress address;
    byte[] data;
    Packet frame = null;

    while ( !BusListener.shutdown ) {
      try {
        while ( ( address = (InetSocketAddress)channel.receive( buffer ) ) != null ) {
          buffer.flip();

          if ( buffer.remaining() > 0 ) {
            data = new byte[buffer.remaining()]; // allocate a byte array

            buffer.get( data ); // copy the data into the buffer
            buffer.clear(); // clear out the buffer

            try {
              frame = new Packet( data );
              counter++;

              frame.remoteAddress = address.getAddress();
              frame.remotePort = address.getPort();

              System.out.println( BusListener.DATE_FORMATTER.format( new Date() ) + "  len=" + data.length + "  frame=" + frame.getTypeName() + BusListener.LF + address + " (" + address.getHostName() + ")" );
              if ( frame.message != null ) {
                System.out.println( frame.message.toString() );
                System.out.println();
              }
            } catch ( final Exception e ) {
              System.err.println( e.getMessage() );
              e.printStackTrace();
            }
          }

        } // while

      } catch ( final Exception e ) {
        System.err.println( e.getMessage() );
        e.printStackTrace();
      }

    } // while !shutdown

    // tear things down

    try {
      channel.close();
    } catch ( final Exception ignore ) {}

    System.out.println( "Shutting down after observing " + counter + " frames" );
  }




  /**
   * @param args
   */
  public static void main( final String[] args ) {
    new BusListener( args ).run();
  }

}
