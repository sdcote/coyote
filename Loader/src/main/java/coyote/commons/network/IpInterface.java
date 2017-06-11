/*
 * Copyright (c) 2014 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial API and implementation
 */
package coyote.commons.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;


/**
 * The IpInterface class models a class that represents a detailed IP interface 
 * on the host platform.
 * 
 * <p>IpInterfaces contain a single name, IP address and an optional netmask 
 * and broadcast address if the netmask could be determined.
 * 
 * <p>The static accessors are used to get instances of this class.
 */
public class IpInterface {
  String name = null;
  String displayName = null;
  IpAddress address = null;
  IpAddress netmask = null;
  NetworkInterface netInterface = null;
  static IpInterface primaryInterface = null;

  public static IpAddress DEFAULT_NETMASK;
  static String[] outArray = new String[0];
  static String[] errArray = new String[0];

  final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
  private static InetAddress localAddress = null;

  private static final ArrayList<IpInterface> interfaces = new ArrayList<IpInterface>();

  /** Flag indicating the interfaces have been populated for the platform. */
  private static boolean initializedFlag = false;

  static {
    try {
      DEFAULT_NETMASK = new IpAddress( "0.0.0.0" );
    } catch ( IpAddressException e1 ) {}
  }




  private IpInterface() {}




  public Object clone() {
    IpInterface retval = new IpInterface();
    retval.name = name;
    retval.displayName = displayName;
    retval.address = address;
    retval.netmask = netmask;
    retval.netInterface = netInterface;
    return retval;
  }




  private static void initialize() {
    initializedFlag = true;

    // Prime the list if interfaces with the results of the runtime calls
    try {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      while ( e.hasMoreElements() ) {
        NetworkInterface netface = (NetworkInterface)e.nextElement();

        IpInterface ipi = new IpInterface();
        ipi.setName( netface.getName() );
        ipi.setDisplayName( netface.getDisplayName() );
        ipi.setNetmask( DEFAULT_NETMASK );

        Enumeration<InetAddress> e2 = netface.getInetAddresses();
        while ( e2.hasMoreElements() ) {
          InetAddress ip = (InetAddress)e2.nextElement();

          if ( ipi.getAddress() == null ) {
            try {
              ipi.setAddress( new IpAddress( ip ) );
            } catch ( IpAddressException e1 ) {
              e1.printStackTrace();
            }

            interfaces.add( ipi );
          } else {
            ipi = (IpInterface)ipi.clone();
            ipi.address = null;
          } // ipaddr null check
        } // while more ip addresses
      } // while more interfaces
    } catch ( SocketException e ) {
      e.printStackTrace();
    }

    String opsys = System.getProperty( "os.name" ).toUpperCase( Locale.US );

    if ( opsys.startsWith( "WINDOWS" ) ) {
      exec( "ipconfig /all" );

      String line = null;
      int mrk = 0;
      for ( int i = 0; i < outArray.length; i++ ) {
        line = outArray[i].toUpperCase().trim();
        mrk = line.indexOf( "IP ADDRESS" );
        if ( mrk > -1 ) {
          mrk = line.indexOf( ':', mrk );
          if ( mrk > -1 ) {
            try {
              IpAddress ipa = new IpAddress( line.substring( mrk + 1 ) );

              IpInterface ipi = getInterface( ipa );

              if ( ipi == null ) {
                ipi = new IpInterface();
                ipi.address = ipa;
                interfaces.add( ipi );
              }

              while ( line.length() > 0 && i < outArray.length ) {
                line = outArray[i++].toUpperCase().trim();
                mrk = line.indexOf( "SUBNET MASK" );
                if ( mrk > -1 ) {
                  mrk = line.indexOf( ':', mrk );
                  if ( mrk > -1 ) {
                    try {
                      ipi.setNetmask( new IpAddress( line.substring( mrk + 1 ) ) );
                      break;
                    } catch ( Exception ex ) {
                      ex.printStackTrace();
                    }
                  } // if value
                }// if mask tag
              } // while still in interface section
            } catch ( IpAddressException e ) {
              System.out.println( "PROBLEMS PARSING '" + line.substring( mrk + 1 ) + "'" );
              e.printStackTrace();
            }

          }
        }
        // look for
        // "Physical Address. . . . . . . . . : 00-13-21-0F-5D-B3"
      }

      // setup the loopback interface to 255.0.0.0
      try {
        IpInterface ipi = getInterface( new IpAddress( "127.0.0.1" ) );
        if ( ipi != null && DEFAULT_NETMASK.equals( ipi.getNetmask() ) ) {
          ipi.netmask = new IpAddress( "255.0.0.0" );
        }
      } catch ( IpAddressException e ) {
        // should always work
        e.printStackTrace();
      }
    } else if ( opsys.equals( "SOLARIS" ) || opsys.equals( "SUNOS" ) ) {
      // lo0: flags=1000849<UP,LOOPBACK,RUNNING,MULTICAST,IPv4> mtu 8232
      // index 1
      // inet 127.0.0.1 netmask ff000000
      // hme0: flags=1000843<UP,BROADCAST,RUNNING,MULTICAST,IPv4> mtu 1500
      // index 2
      // inet 10.8.96.39 netmask ff000000 broadcast 10.255.255.255
      // ether 8:0:20:ab:c1:b7

      exec( "/usr/sbin/ifconfig -a" );
      String line = null;
      int mrk = 0;

      // for( int x = 0; x < outArray.length; System.out.println(
      // outArray[x++] ) );

      for ( int i = 0; i < outArray.length; i++ ) {
        line = outArray[i];
        // System.out.println( "Parsing[" + i + "]:'" + line + "'" );

        if ( !Character.isWhitespace( ( line.charAt( 0 ) ) ) ) {
          mrk = line.lastIndexOf( ":" );
          if ( mrk > -1 ) {
            String name = line.substring( 0, mrk ).trim();
            String addr = null;
            String mask = null;

            // System.out.println( "  Intrfc:'" + name + "'" );

            while ( addr == null || mask == null ) {
              if ( i + 1 < outArray.length ) {
                line = outArray[++i];

                if ( !Character.isWhitespace( ( line.charAt( 0 ) ) ) ) {
                  i--;
                  break;
                }

                // System.out.println( "  ck[" + i + "]:'" +
                // line + "'" );

                StringTokenizer st = new StringTokenizer( line.trim(), " \t" );
                while ( st.hasMoreTokens() ) {
                  String token = st.nextToken();

                  if ( "inet".equalsIgnoreCase( token ) ) {
                    addr = st.nextToken();
                  } else if ( "netmask".equalsIgnoreCase( token ) ) {
                    mask = st.nextToken();
                  }
                } // for each token

              } // if more lines

            } // while either addr or mask is null

            try {
              IpAddress ipa = new IpAddress( addr );
              IpInterface ipi = getInterface( ipa );

              if ( ipi == null ) {
                ipi = new IpInterface();
                ipi.address = ipa;
                interfaces.add( ipi );
              }

              ipi.setDisplayName( name );

              if ( ipi.getName() == null )
                ipi.setName( name );

              if ( mask != null )
                ipi.setNetmask( new IpAddress( hexToBytes( mask ) ) );

              // System.out.println( ipi.toString() );

              addr = mask = null;
            } catch ( Exception e ) {
              e.printStackTrace();
            }
          }// another line available

        } // if line starts with a character

      } // for each line
    } else if ( opsys.equals( "HP-UX" ) ) {
      exec( "/usr/bin/netstat -i" );

      ArrayList<String> list = new ArrayList<String>();

      String line = null;
      int mrk = 0;
      String name = null;
      for ( int i = 0; i < outArray.length; i++ ) {
        line = outArray[i].trim();
        if ( line.length() > 0 ) {
          mrk = line.indexOf( " " );
          if ( mrk > -1 ) {
            name = line.substring( 0, mrk );
          } else {
            mrk = line.indexOf( "\t" );
            if ( mrk > -1 ) {
              name = line.substring( 0, mrk );
            }
          }

          if ( name != null && !name.equalsIgnoreCase( "Name" ) ) {
            list.add( name );
          }
        }
      }

      // should have a list of all the interfaces
      for ( int y = 0; y < list.size(); y++ ) {
        name = (String)list.get( y );

        String addr = null;
        String mask = null;

        // /usr/sbin/ifconfig lan13
        exec( "/usr/sbin/ifconfig " + name );
        for ( int i = 0; i < outArray.length; i++ ) {
          line = outArray[i].trim();

          if ( line.length() > 0 ) {
            StringTokenizer st = new StringTokenizer( line, " \t" );
            while ( st.hasMoreTokens() ) {
              String token = st.nextToken();

              if ( "inet".equalsIgnoreCase( token ) ) {
                addr = st.nextToken();
              } else if ( "netmask".equalsIgnoreCase( token ) ) {
                mask = st.nextToken();
              }
            }

          }
        }

        try {
          IpAddress ipa = new IpAddress( addr );
          IpInterface ipi = getInterface( name );

          if ( ipi == null ) {
            ipi = new IpInterface();
            ipi.address = ipa;
            interfaces.add( ipi );
          }

          ipi.setDisplayName( name );

          if ( ipi.getName() == null )
            ipi.setName( name );

          ipi.setNetmask( new IpAddress( hexToBytes( mask ) ) );
        } catch ( IpAddressException e ) {
          e.printStackTrace();
        }

        addr = null;
        mask = null;
      }

    } else if ( opsys.equals( "LINUX" ) ) {
      // eth0 Link encap:Ethernet HWaddr 00:04:75:17:CC:D0
      // inet addr:192.168.2.56 Bcast:192.168.2.255 Mask:255.255.255.0
      // UP BROADCAST RUNNING MULTICAST MTU:1500 Metric:1
      // RX packets:60073846 errors:0 dropped:0 overruns:0 frame:0
      // TX packets:72752155 errors:2 dropped:0 overruns:12 carrier:15
      // collisions:0 txqueuelen:100
      // RX bytes:3669049802 (3499.0 Mb) TX bytes:1618610375 (1543.6 Mb)
      // Interrupt:10 Base address:0x1000
      //
      // lo Link encap:Local Loopback
      // inet addr:127.0.0.1 Mask:255.0.0.0
      // UP LOOPBACK RUNNING MTU:16436 Metric:1
      // RX packets:85112910 errors:0 dropped:0 overruns:0 frame:0
      // TX packets:85112910 errors:0 dropped:0 overruns:0 carrier:0
      // collisions:0 txqueuelen:0
      // RX bytes:1518154601 (1447.8 Mb) TX bytes:1518154601 (1447.8 Mb)

      exec( "/sbin/ifconfig -a" );
      String line = null;
      int mrk = 0;
      String name = null;
      String addr = null;
      String mask = null;
      String token = null;

      for ( int i = 0; i < outArray.length; i++ ) {
        line = outArray[i].trim().toUpperCase();

        if ( line.length() != 0 ) {
          if ( name != null && addr != null ) {
            try {
              IpAddress ipa = new IpAddress( addr );
              IpInterface ipi = getInterface( ipa );

              if ( ipi == null ) {
                ipi = new IpInterface();
                ipi.address = ipa;
                interfaces.add( ipi );
                ipi.setDisplayName( name );
              }

              if ( ipi.getName() == null )
                ipi.setName( name );

              ipi.setNetmask( new IpAddress( mask ) );
            } catch ( IpAddressException e ) {
              e.printStackTrace();
            }

            name = null;
            addr = null;
            mask = null;
          }
        }

        mrk = line.indexOf( "LINK ENCAP" );
        if ( mrk > -1 ) {
          // we have a line that contains the name of the link
          name = line.substring( 0, line.indexOf( ' ' ) );
        }

        mrk = line.indexOf( "INET ADDR" );
        if ( mrk > -1 ) {
          token = line.substring( mrk + 9 );
          mrk = token.indexOf( ' ' );
          if ( mrk > -1 )
            token = token.substring( 0, mrk );

          if ( token.charAt( 0 ) == ':' )
            token = token.substring( 1 );

          addr = token;

        }

        mrk = line.indexOf( "MASK" );
        if ( mrk > -1 ) {
          token = line.substring( mrk + 4 );
          mrk = token.indexOf( ' ' );
          if ( mrk > -1 )
            token = token.substring( 0, mrk );

          if ( token.charAt( 0 ) == ':' )
            token = token.substring( 1 );

          mask = token;

        }

      } // for each line

      // finish up the last one
      if ( name != null && addr != null ) {
        try {
          IpAddress ipa = new IpAddress( addr );
          IpInterface ipi = getInterface( ipa );

          if ( ipi == null ) {
            ipi = new IpInterface();
            ipi.address = ipa;
            interfaces.add( ipi );
            ipi.setDisplayName( name );
          }

          if ( ipi.getName() == null )
            ipi.setName( name );

          ipi.setNetmask( new IpAddress( mask ) );
        } catch ( IpAddressException e ) {
          e.printStackTrace();
        }

      }

    } // Linux
    else {
      // unsupported OS
      System.err.println( "Unsupported OS: '" + opsys + "'" );
    }

  }




  /**
   * Return an array of all the interfaces in the system.
   * 
   * <p>This is the only place where the interfaces are populated from the 
   * discovery processes. This means this method must be called at least once 
   * before all the interfaces are discovered on the system.
   * 
   * @return and array of discovered IP interfaces on this host.
   */
  public static IpInterface[] getIpInterfaces() {
    if ( !initializedFlag ) {
      initialize();
    }

    IpInterface[] retval = new IpInterface[interfaces.size()];

    if ( interfaces.size() > 0 ) {
      for ( int x = 0; x < interfaces.size(); retval[x] = (IpInterface)interfaces.get( x++ ) );
    }

    return retval;
  }




  /**
   * Return the IpInterface with the given IpAddress
   * 
   * @param addr The address bound to the interface.
   *  
   * @return the interface with the given address or null if the address is not 
   *         bound to any of the discovered interfaces.
   */
  public static IpInterface getInterface( IpAddress addr ) {
    IpInterface retval = null;

    if ( addr != null ) {
      for ( int x = 0; x < interfaces.size(); x++ ) {
        if ( addr.equals( ( (IpInterface)interfaces.get( x ) ).address ) ) {
          retval = (IpInterface)interfaces.get( x );
          break;
        }
      }
    }

    return retval;
  }




  /**
   * Return the first IpInterface with the given name.
   * 
   * @param name The name of the interface to retrieve.
   *  
   * @return the first interface with the given name or null if the name is not 
   *         found in any of the discovered interfaces.
   */
  public static IpInterface getInterface( String name ) {
    IpInterface retval = null;

    if ( name != null && name.length() > 0 ) {
      for ( int x = 0; x < interfaces.size(); x++ ) {
        if ( name.equals( ( (IpInterface)interfaces.get( x ) ).name ) ) {
          retval = (IpInterface)interfaces.get( x );
          break;
        }
      }
    }

    return retval;
  }




  public static String bytesToHex( byte[] bytes ) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String( hexChars );
  }




  /**
   * Convert hex representation of bytes to an array bytes
   * 
   * @param s The string to parse
   * 
   * @return an array of bytes represented by the string
   */
  // Quick hack, need to clean up and test
  public static byte[] hexToBytes( String s ) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for ( int i = 0; i < len; i += 2 ) {
      data[i / 2] = (byte)( ( Character.digit( s.charAt( i ), 16 ) << 4 ) + Character.digit( s.charAt( i + 1 ), 16 ) );
    }
    return data;
  }




  /**
   * Get the IP Address by which the rest of the world knows us.
   *
   * <p>This is useful in helping insure that we don't accidently start binding
   * to or otherwise using the local loopback address.
   *
   * <p>This requires some type of IP address resolver to be installed, like
   * DNS, NIS or at least hostname lookup.
   *
   * @return The InetAddress representing the host on the network and NOT the
   *         loopback address.
   */
  public static InetAddress getLocalAddress() {
    // If we already looked this up, use the cached result to save time
    if ( localAddress != null ) {
      return localAddress;
    }

    // No cached result, figure it out and cache it for later
    InetAddress addr = null;

    // Make sure we get the IP Address by which the rest of the world knows
    // us
    // or at least, our host's default network interface
    try {
      // This helps insure that we do not get localhost (127.0.0.1)
      addr = InetAddress.getByName( InetAddress.getLocalHost().getHostName() );
    } catch ( UnknownHostException e ) {
      // Aaaaww Phooey! DNS is not working or we are not in it.
      addr = null;
    }

    // If it looks like a unique address, return it, otherwise try again
    if ( ( addr != null ) && !addr.getHostAddress().equals( "127.0.0.1" ) && !addr.getHostAddress().equals( "0.0.0.0" ) ) {
      localAddress = addr;

      return addr;
    }

    // Try it the way it's supposed to work
    try {
      addr = InetAddress.getLocalHost();
    } catch ( Exception ex ) {
      addr = null;
    }

    localAddress = addr;

    return addr;
  }




  public static String dump() {
    StringBuffer buffer = new StringBuffer( "-------- [ Runtime Values ] --------\r\n" );

    try {
      Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
      while ( e.hasMoreElements() ) {
        NetworkInterface netface = (NetworkInterface)e.nextElement();
        buffer.append( "Net interface: " );
        buffer.append( netface.getName() );
        buffer.append( "\r\n" );
        buffer.append( "Display Name: " );
        buffer.append( netface.getDisplayName() );
        buffer.append( "\r\n" );

        Enumeration<InetAddress> e2 = netface.getInetAddresses();
        while ( e2.hasMoreElements() ) {
          InetAddress ip = (InetAddress)e2.nextElement();
          buffer.append( "IP address: " + ip.toString() );
          buffer.append( "\r\n" );
        }

        buffer.append( "\r\n" );

      }
    } catch ( SocketException e ) {
      e.printStackTrace();
    }
    buffer.append( "--------[ IpInterface Values ] --------\r\n" );

    IpInterface[] ipifs = IpInterface.getIpInterfaces();
    for ( int x = 0; x < ipifs.length; x++ ) {
      buffer.append( ipifs[x].toString() );
      buffer.append( "\r\n" );
    }

    buffer.append( "\r\nPrimary: " );
    buffer.append( IpInterface.getPrimary() );
    buffer.append( "\r\n" );

    return buffer.toString();
  }




  /**
   * @return Returns the displayName.
   */
  public String getDisplayName() {
    return displayName;
  }




  /**
   * @param displayName The displayName to set.
   */
  private void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }




  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }




  /**
   * @param name The name to set.
   */
  private void setName( String name ) {
    this.name = name;
  }




  /**
   * @return Returns the address.
   */
  public IpAddress getAddress() {
    return address;
  }




  /**
   * @param address The address to set.
   */
  private void setAddress( IpAddress addr ) {
    address = addr;
  }




  /**
   * @return Returns the netmask.
   */
  public IpAddress getNetmask() {
    return netmask;
  }




  /**
   * Get the DNS name of this interface.
   * 
   * <p>The name will be calculated the first time this method is called and 
   * its value will be cached for all future accesses.
   * 
   * <p>It is important to keep in mind that the return value will depend upon 
   * this host name resolver. If the IP address assigned to this interface is 
   * located in a local host resolver file, then the return value will be 
   * whatever is specified in that file and a DNS lookup may not take place.
   * 
   * @return The name to which this interfaces IP address resolves on this 
   *         hosts resolver, or null if the address is not valid or in DNS or 
   *         the hosts resolver.
   */
  public String getDnsName() {
    return address.getDnsName();
  }




  /**
   * @return Just the domain portion of this interfaces name.
   */
  public String getDomain() {
    return address.getDomain();
  }




  /**
   * @return The hostname of this interface without the domain portion.
   */
  public String getRelativeHostname() {
    return address.getRelativeHostname();
  }




  /**
   * @param netmask The netmask to set.
   */
  private void setNetmask( IpAddress mask ) {
    netmask = mask;
  }




  /**
   * @return Returns the broadcast address of this interface.
   */
  public IpAddress getBroadcast() {
    return new IpNetwork( address, netmask ).getBroadcastAddress();
  }




  /**
   * @return the network address of this interface in CIDR format.
   */
  public IpNetwork getSubnet() {
    return new IpNetwork( address, netmask );
  }




  /**
   * Return a human-readable representation of the interface.
   *  
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer();
    b.append( name );
    b.append( ":" );
    b.append( address );
    b.append( " mask:" );
    b.append( netmask );
    b.append( " net:" );
    b.append( getSubnet() );
    b.append( " bcast:" );
    b.append( getBroadcast() );

    return b.toString();
  }




  public static IpInterface getPrimary() {
    // make sure we have a cached value
    if ( primaryInterface == null ) {
      IpInterface retval = null;

      try {
        // make sure the interfaces have been discovered
        getIpInterfaces();

        // get the IpAddress by which the rest of the world knows this
        // host
        IpAddress addr = new IpAddress( getLocalAddress() );

        if ( !IpAddress.IP4_LOOPBACK.equals( addr.toString() ) ) {
          // Search for the interface that matches that primary host
          // address
          for ( int x = 0; x < interfaces.size(); x++ ) {
            IpInterface ipi = (IpInterface)interfaces.get( x );
            if ( addr.equals( ipi.getAddress() ) ) {
              retval = ipi;
              break;
            }
          }
        } else {
          // if there is only one return that interface
          if ( interfaces.size() == 1 ) {
            retval = (IpInterface)interfaces.get( 0 );
          } else if ( interfaces.size() == 2 ) {
            // if one of interfaces is the loop-back, return the
            // other
            if ( "127.0.0.1".equals( ( (IpInterface)interfaces.get( 0 ) ).getAddress().toString() ) ) {
              retval = (IpInterface)interfaces.get( 1 );
            } else {
              retval = (IpInterface)interfaces.get( 0 );
            }
          } else {
            // find the first non-loopback interface
            for ( int x = 0; x < interfaces.size(); x++ ) {
              IpInterface ipi = (IpInterface)interfaces.get( x );
              if ( !IpAddress.IP4_LOOPBACK.equals( ipi.getAddress().toString() ) ) {
                retval = ipi;
                break;
              }
            }
          }
        }
      } catch ( IpAddressException e ) {
        e.printStackTrace();
      } catch ( ArrayIndexOutOfBoundsException e ) {
        e.printStackTrace();
      }

      // cache the value for later retrieval
      primaryInterface = retval;
    }

    return primaryInterface;
  }




  /**
   * @return Returns the network interface behind this IP Interface.
   */
  public NetworkInterface getNetworkInterface() {
    return netInterface;
  }




  /**
   * @param netntrfc The netInterface to set.
   */
  public void setNetworkInterface( NetworkInterface netntrfc ) {
    this.netInterface = netntrfc;
  }




  public static void main( String[] args ) {
    System.out.println( IpInterface.dump() );
    // System.out.println( IpInterface.getPrimary() );
  }




  static String[] exec( String command ) {

    // System.out.println( "EXEC (String) called with command \"" + command
    // + "\"" );

    Process process = null;

    try {

      // System.out.println( "EXEC calling runtime exec" );

      process = Runtime.getRuntime().exec( command );

      // System.out.println( "EXEC runtime exec returned" );

      // /////////////////////////////////////////////////////////////////////////////
      // Why won't this work?!?!
      // /////////////////////////////////////////////////////////////////////////////
      // // fetch the stdout and stderr output
      // StringBuffer stderr = new StringBuffer();
      // stderr.setLength(0);
      // BufferedReader errstream = new BufferedReader( new
      // InputStreamReader(process.getErrorStream()));
      // String s;
      // // We have to read the error stream first on Windows (JVM bug?)
      // while ((s = errstream.readLine()) != null) errArray =
      // (String[])ArrayUtil.addElement( errArray, s );
      //
      // StringBuffer stdout = new StringBuffer();
      // stdout.setLength(0);
      // BufferedReader outstream = new BufferedReader( new
      // InputStreamReader(process.getInputStream()));
      // while ((s = outstream.readLine()) != null) outArray =
      // (String[])ArrayUtil.addElement( outArray, s );
      // /////////////////////////////////////////////////////////////////////////////

      ErrorReader errreader = new IpInterface().new ErrorReader();
      errreader.stream = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );

      errreader.start();

      do {
        String s1 = readLine( process.getInputStream() );

        if ( s1 == null ) {
          break;
        }

        outArray = (String[])addElement( outArray, s1 );
      }
      while ( true );

      // System.out.println( "EXEC calling waitFor" );

      process.waitFor();

      // System.out.println( "EXEC waitFor returned" );

      process = null;

      // Get our errors from the error collector
      errArray = errreader.collected;
    } catch ( Exception exception ) {
      // System.out.println( "*** exec Exception" );
      // System.out.println( "  command: " + command );
      // System.out.println( "     exit: " + exitValue );
      // System.out.println( "OUT lines: " + outArray.length );
      // System.out.println( "ERR lines: " + errArray.length );
      // System.out.println( "    error: " + exception.toString() );
    }
    finally {}

    if ( process != null ) {
      process.destroy();
    }

    // System.out.println( "*** exec" );
    // System.out.println( "  command: " + command );
    // System.out.println( "     exit: " + exitValue );
    // System.out.println( "OUT lines: " + outArray.length );
    // System.out.println( "ERR lines: " + errArray.length );
    // System.out.println( "     time: " + duration + "ms" );

    return outArray;
  }




  /**
   * Construct a string by reading bytes in from the given inputstream until
   * the NL sequence is observed.
   *
   * <p>All CR characters will be ignored and stripped from the returned
   * string.
   *
   * <p>This will NOT work on Macintosh files which only use CR as a line
   * terminator.
   *
   * @param inputstream The stream to read
   *
   * @return the string read in without any CR or NL characters, null if the
   *         stream is EOF or closed
   *         
   * @throws IOException if there are problems reading from the stream
   */
  private static String readLine( InputStream inputstream ) throws IOException {
    StringBuffer stringbuffer = new StringBuffer();

    do {
      int i = inputstream.read();

      if ( i == -1 ) {
        return ( stringbuffer.length() != 0 ) ? stringbuffer.toString() : null;
      }

      // line-feeds represent the end of line
      if ( i == 10 ) {
        return stringbuffer.toString();
      }

      // Ignore carriage returns
      if ( i != 13 ) {
        stringbuffer.append( (char)i );
      }
    }
    while ( true );
  }




  /**
   * Return a new array that is a copy of the array plus a new element.
   *
   * <p>The component type of the array must be the same as that type of the
   * element.
   *
   * @param array An array
   * @param element The element to append.
   *
   * @return the array with the added element
   */
  private static Object addElement( Object array, Object element ) {
    int length = Array.getLength( array );
    Object newarray = Array.newInstance( array.getClass().getComponentType(), length + 1 );
    System.arraycopy( array, 0, newarray, 0, length );
    Array.set( newarray, length, element );

    return newarray;
  }




  /**
   * Remove the element at the given position from the given array.
   *
   * @param oldarray
   * @param index
   *
   * @return the array without the element
   */
  private static Object removeElementAt( Object oldarray, int index ) {
    int length = Array.getLength( oldarray );
    Object newarray = Array.newInstance( oldarray.getClass().getComponentType(), length - 1 );
    System.arraycopy( oldarray, 0, newarray, 0, index );
    System.arraycopy( oldarray, index + 1, newarray, index, length - index - 1 );

    return newarray;
  }

  /**
   * Class ErrorReader
   */
  private class ErrorReader extends Thread {

    public BufferedReader stream;
    public String[] collected;
    public int collectedMax;




    public ErrorReader() {
      collected = new String[0];
      collectedMax = 500;
    }




    public void run() {
      try {
        String s;

        while ( ( s = stream.readLine() ) != null ) {
          collected = (String[])addElement( collected, s );

          if ( collected.length > collectedMax ) {
            collected = (String[])removeElementAt( collected, 0 );
          }
        }

        return;
      } catch ( Exception exception ) {
        System.out.println( exception );
      }
    }
  }

}
