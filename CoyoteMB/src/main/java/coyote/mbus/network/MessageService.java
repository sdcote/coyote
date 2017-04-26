/*
 * Copyright (c) 2006 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.mbus.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;

import coyote.commons.ByteUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.UriUtil;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;
import coyote.mbus.DefaultLogAppender;
import coyote.mbus.LogAppender;


/**
 * The MessageService class models a TCP socket server for bridges.
 * 
 * <p>This network service handler accepts TCP connections and allows messages 
 * to be passed across networks and devices which do not pass multicast.
 * 
 * <p>Instances of this class run in a NetworkService.
 */
public class MessageService implements NetworkServiceHandler {
  ServerSocketChannel serverChannel = null;

  /** maps a SelectionKey -> bridge */
  private final HashMap bridges = new HashMap( 5 );

  private final ByteBuffer readBuffer;

  /** The default URI we use to specify where we listen for messages */
  public static final URI DEFAULT_URI = UriUtil.parse( "tcp://0.0.0.0:" + MessageBus.DEFAULT_PORT );

  /** The URI representing the bus interface for this instance */
  private URI serviceUri = MessageService.DEFAULT_URI;

  /** Access Control List for TCP connections. */
  private final IpAcl ACL = new IpAcl();

  private volatile boolean shutdown = false;

  /** The object we use to append log messages */
  private LogAppender LOG = new DefaultLogAppender( System.out );

  /** Error messages are logged here */
  private LogAppender ERR = new DefaultLogAppender( System.err );




  public MessageService( final ServerSocketChannel ssc ) {
    serverChannel = ssc;

    try {
      serviceUri = new URI( "tcp://" + ssc.socket().getInetAddress().getHostAddress() + ":" + ssc.socket().getLocalPort() );
    } catch ( final URISyntaxException e ) {
      // shouldn't happen
    }

    readBuffer = ByteBuffer.allocateDirect( Bridge.SOCKET_BUFFER_SIZE );

    // By default we disallow everything
    ACL.setDefaultAllow( false );
  }




  /**
   * 
   */
  public MessageService( final URI uri ) {
    if ( uri == null ) {
      throw new IllegalArgumentException( "Null URI passed to MicroBus Service" );
    }

    serviceUri = uri;

    readBuffer = ByteBuffer.allocateDirect( Bridge.SOCKET_BUFFER_SIZE );

    // By default we disallow everything
    ACL.setDefaultAllow( false );
  }




  public void accept( final SelectionKey key ) {
    try {
      final SocketChannel socketChannel = ( (ServerSocketChannel)key.channel() ).accept();

      // Use ACL to determine if the connection should be accepted
      if ( ACL.allows( socketChannel.socket().getInetAddress() ) ) {
        LOG.append( "Connection passed ACL check" );
        socketChannel.socket().setSendBufferSize( Bridge.SOCKET_BUFFER_SIZE );
        socketChannel.socket().setReceiveBufferSize( Bridge.SOCKET_BUFFER_SIZE );
        socketChannel.configureBlocking( false );

        if ( socketChannel != null ) {

          final Selector selector = key.selector();
          synchronized( selector ) {
            final SelectionKey clientKey = socketChannel.register( selector, SelectionKey.OP_READ );
            LOG.append( "Accepted connection from " + socketChannel.socket().getRemoteSocketAddress() );

            // Create a new instance of a bridge to handle this connection
            final Bridge bridge = new Bridge( clientKey );

            // Initialize the bridge
            bridge.initialize();

            // attach the bridge to the selection key
            clientKey.attach( bridge );

            // store it for later
            bridges.put( clientKey, new Bridge( clientKey ) );
          }

        }
      } else {
        LOG.append( "Connection from " + socketChannel.socket().getInetAddress().toString() + " failed ACL check, closing connection." );
        try {
          socketChannel.close();
        } catch ( final Exception e ) {
          // ignored for closed socket
        }
        return;
      }

    } catch ( final IOException e ) {
      if ( !shutdown ) {
        ERR.append( "ERROR accepting connection: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) + ExceptionUtil.stackTrace( e ) );
      }
    }

  }




  public void connect( final SelectionKey key ) {
    LOG.append( "Connected to " + key.attachment() );
  }




  public AbstractSelectableChannel getChannel() {
    return serverChannel;
  }




  public SelectionKey getKey() {
    return null;
  }




  /**
   * @return  the serviceUri
   */
  public URI getServiceUri() {
    return serviceUri;
  }




  public void initialize() {
    shutdown = false;
  }




  /**
   * @see coyote.mbus.network.NetworkServiceHandler#read(java.nio.channels.SelectionKey)
   */
  public void read( final SelectionKey key ) {
    // read the data and pass it to the proper bridge
    try {
      final int read = ( (SocketChannel)key.channel() ).read( readBuffer );

      if ( read > 0 ) {
        byte[] data;
        Packet frame = null;

        readBuffer.flip();

        if ( readBuffer.remaining() > 0 ) {
          data = new byte[readBuffer.remaining()]; // allocate a byte array

          readBuffer.get( data ); // copy the data into the buffer
          readBuffer.clear(); // clear out the buffer

          LOG.append( "Receive TCP data:\n" + ByteUtil.dump( data ) );
          try {
            frame = new Packet( data );
            LOG.append( frame.toString() );
          } catch ( final Exception ex ) {
            ERR.append( ex.getMessage() );
          }
        }

      } else if ( read == -1 ) {
        throw new IOException( "Connection closed by peer." );
      }

      if ( readBuffer.remaining() == 0 ) {
        readBuffer.flip();
      }
    } catch ( final Exception e ) {
      ERR.append( "Problems reading connection: " + e.getClass().getName() + " - " + e.getMessage() + System.getProperty( "line.separator" ) + ExceptionUtil.stackTrace( e ) );

      cancelBridge( key );

    }

  }




  private void cancelBridge( final SelectionKey key ) {
    // clean up key
    try {
      // key.interestOps( 0 ); // is this really necessary?
      key.channel().close();
    } catch ( final Exception ignore ) {
      // exceptions OK, closing
    }
    finally {
      key.cancel();
    }

    // remove the bridge;
    final Bridge bridge = (Bridge)bridges.remove( key );

    if ( bridge != null ) {
      bridge.close();
    }
  }




  /**
   * Expecting a ServerSocketChannel to be set here.
   * 
   * @see coyote.mbus.network.NetworkServiceHandler#setChannel(java.nio.channels.spi.AbstractSelectableChannel)
   */
  public void setChannel( final AbstractSelectableChannel channel ) {
    serverChannel = (ServerSocketChannel)channel;
  }




  public void setKey( final SelectionKey key ) {}




  public void shutdown() {
    shutdown = true;
  }




  public void write( final SelectionKey key ) {
    LOG.append( "Writing" );
  }




  public void fireGroupJoined( final String group, final MessageChannel channel ) {}




  public void fireGroupLeave( final String group, final MessageChannel channel ) {}




  public InetAddress getAddress() {
    if ( serverChannel != null ) {
      return serverChannel.socket().getInetAddress();
    }

    return null;
  }




  public int getPort() {
    if ( serverChannel != null ) {
      return serverChannel.socket().getLocalPort();
    }

    return -1;
  }




  /**
   * Add a network specification to the ACL with the given allowance.
   * 
   * <p>Network specification of 127.0.0.1/32 represents the entire loopback
   * address.
   * 
   * <p>Passing the string of &quot;DEFAULT&quot; will assign the default 
   * access to the given allowed argument. If false, all addresses that do not 
   * match an entry in the ACL will be denied, while a value of true will 
   * result in allowing a connection from a network not otherwise specified in 
   * the ACL.
   *
   * @param network the network specification to add (e.g. "192.168/16", "10/8")
   * @param allowed whether or not connections from the specified network will
   *        be accepted.
   * 
   * @throws IpAddressException if the specified network is not valid.
   */
  public void addAclEntry( final String network, final boolean allowed ) throws IpAddressException {
    if ( "DEFAULT".equalsIgnoreCase( network ) ) {
      ACL.setDefaultAllow( allowed );
    } else {
      ACL.add( new IpNetwork( network ), allowed );
    }
  }




  /**
   * Add a network specification to the ACL with the given allowance.
   * 
   * @param network the network specification to add.
   * @param allowed whether or not TCP connections from the specified network 
   *        will be accepted.
   */
  public void addAclEntry( final IpNetwork network, final boolean allowed ) {
    ACL.add( network, allowed );
  }




  /**
   * Add one or more new rules to the Access Control List.
   *
   * <p>The argument String should adhere to the following format:
   * <pre>
   *     network:allowed;network:allowed;network:allowed...
   * </pre>
   * Where "network" is a CIDR representation of a network against which a
   * match is to be made, and "allowed" is either 'ALLOW' or 'DENY'. There is a
   * special expression of 'DEFAULT' which represents the default rule (what
   * should  happen if no expression is matched when performing a check).
   *
   * <p>Examples include:
   * <pre>
   *     192.168/16:ALLOW;DEFAULT:DENY
   * </pre>
   *
   * Where everything coming from the 192.168.0.0/255.255.0.0 network is
   * allowed and everything else is denied.
   *
   * @param rules A semicolon delimited list of rules
   */
  public void addAclEntry( final String rules ) throws IpAddressException {
    ACL.parse( rules );
  }

}
