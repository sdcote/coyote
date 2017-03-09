/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.batch.http;

import java.io.IOException;

import coyote.batch.ConfigTag;
import coyote.batch.Service;
import coyote.batch.http.nugget.CommandHandler;
import coyote.batch.http.nugget.HealthCheckHandler;
import coyote.batch.http.nugget.LogApiHandler;
import coyote.batch.http.nugget.PingHandler;
import coyote.commons.StringUtil;
import coyote.commons.network.IpAcl;
import coyote.commons.network.IpAddress;
import coyote.commons.network.IpAddressException;
import coyote.commons.network.IpNetwork;
import coyote.commons.network.http.nugget.HTTPDRouter;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.loader.cfg.Config;
import coyote.loader.log.Log;


/**
 * This defines the API for a Batch Service.
 */
public class DefaultHttpManager extends HTTPDRouter implements HttpManager {

  private final Service service;




  /**
   * Create the server instance with all the defaults.
   * 
   * @param service the service we are to manage
   */
  public DefaultHttpManager( int port, Config cfg, Service service ) throws IOException {
    super( port );

    if ( service == null )
      throw new IllegalArgumentException( "Cannot create HttpManager without a service reference" );

    // Our connection to the service instance we are managing
    this.service = service;

    if ( cfg != null ) {
      // Setup auth provider from configuration - No configuration results in deny-all operation
      DataFrame authConfig = null;
      for ( DataField field : cfg.getFields() ) {
        if ( BatchAuthProvider.AUTH_SECTION.equalsIgnoreCase( field.getName() ) && field.isFrame() ) {
          setAuthProvider( new BatchAuthProvider( new Config( (DataFrame)field.getObjectValue() ) ) );
        }
      }

      // Configure the IP Access Control List
      configIpACL( cfg );

      // Configure Denial of Service frequency tables
      configDosTables( cfg );
    }

    // Set the default mappings
    addMappings();

    // REST interfaces with a default priority of 100
    addRoute( "/api/cmd/:command", CommandHandler.class, service );
    addRoute( "/api/ping/:id", PingHandler.class, service );
    addRoute( "/api/log/:logname/:action", LogApiHandler.class, service );
    addRoute( "/api/health", HealthCheckHandler.class, service );
  }




  /**
   * Parse the IP Access Control List section of the configuration.
   * 
   * <p>This allows the operator to define access control rules for the server.
   * When the connection is accepted by the server, the remote address is 
   * checked against the IP ACL. If the ACL denies access, the socket is closed 
   * and the request is not passed to a thread for processing.</p>
   * 
   * <p>The following is an example configuration:<pre>
   * "IPACL":{
   *   "default": "deny",
   *   "127.0.0.1": "allow",
   *   "172.28.147.6/0": "allow",
   *   "192.168/16": "allow",
   *   "10/8": "deny"
   * },</pre></p>
   * 
   * <p>If a network does not contain a / character, it is assumed to be an 
   * address and will be converted to a /0 network which is a network with no 
   * netmask (i.e. a single address network).</p>
   * 
   * <p>The {@code default} rule allow the operator to turn the ACL into either 
   * a whitelist or a blacklist. The default is a whitelist where all 
   * connections are denied by default. A blacklist is created by setting 
   * default to "allow" which allows everything unless specifically denied by a 
   * rule. The ACL is set to a whitelist ({@code "default":"deny"}) by 
   * default.</p>
   * 
   * <p>Order is important. The first rule matching the address is what is used
   * to determine access. Therefore, place all /0 networks first in the list 
   * then broader networks later to make sure evaluation occurs as 
   * expected.</p>
   * 
   * <p>For example, if you want to include everyone in the 192.168/16 network 
   * except for those in the 192.168.100/24 network, your configuration should 
   * look like this:<pre>
   * "IPACL":{
   *   "192.168.100/24": "deny",
   *   "192.168/16": "allow"
   * },</pre></p>
   * @param cfg The configuration to parse
   */
  private void configIpACL( Config cfg ) {
    Config aclCfg = cfg.getSection( ConfigTag.IPACL );
    if ( aclCfg != null ) {
      for ( DataField field : aclCfg.getFields() ) {
        String network = field.getName();
        String access = field.getStringValue();

        IpNetwork ipNet = null;
        if ( StringUtil.isNotBlank( network ) ) {

          if ( "DEFAULT".equalsIgnoreCase( network.trim() ) ) {
            if ( StringUtil.isNotBlank( access ) ) {
              super.setDefaultAllow( IpAcl.ALLOW_TAG.equalsIgnoreCase( access.trim() ) );
            } else {
              Log.debug( "Blank access on default ACL rule" );
            }
          } else {

            try {
              ipNet = new IpNetwork( network );
            } catch ( IpAddressException e ) {
              // maybe it is an address
              try {
                IpAddress ipAdr = new IpAddress( network );
                ipNet = new IpNetwork( ipAdr.toString() + "/0" );
              } catch ( IpAddressException ex ) {
                Log.error( "Invalid network specification '" + network + "' - " + ex.getMessage() );
                ipNet = null;
              }
            }

            if ( ipNet != null ) {
              boolean allows = true;
              if ( StringUtil.isNotBlank( access ) ) {
                allows = IpAcl.ALLOW_TAG.equalsIgnoreCase( access.trim() );
              }
              Log.append( EVENT, "Adding " + ipNet + " to IP Access Control List with allows = " + allows );
              addToACL( ipNet, allows );
            } else {
              Log.error( "Network: " + ipNet + " not added to IP Access Control List" );
            }

          } // if default

        } else {
          Log.debug( "No network or address" );
        }

      } // for each field
    }

  }




  /**
   * <pre>
   * "Frequency":{
   *   "default": { "Requests": 10, "Interval": 1000, "Threshold": 3, "Window": 3000, "Breach": "blacklist"},
   *   "192.168.100/24": { "Requests": 10, "Interval": 1000, "Threshold": 3, "Window": 3000, "Breach": "throttle:3000"}
   *  }</pre>
   *  requests are the number of requests to allow in an interval
   *  interval is the number of milliseconds in the interval
   *  threshold is the number of failures allows in a window of time
   *  window is the number of milliseconds in the window
   *  breach is what to do when the number of failures are exceeded in the window; options are 
   *   - Blacklist the IP
   *   - Throttle the connection for the amount of milliseconds but allow it after waiting
   *   - FUTURE: Retract shutdown the server for the number of milliseconds then restart
   *   - FUTURE: Terminate terminate the server
   * @param cfg
   */
  private void configDosTables( Config cfg ) {
    Config freqCfg = cfg.getSection( ConfigTag.FREQUENCY );
    // TODO: Make this work
  }

}
