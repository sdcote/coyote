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
package coyote.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;

import coyote.commons.DateUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.i13n.StatBoard;
import coyote.loader.ConfigTag;
import coyote.loader.DefaultLoader;
import coyote.loader.Loader;
import coyote.loader.cfg.Config;
import coyote.loader.component.AbstractScheduledComponent;
import coyote.loader.log.ConsoleAppender;
import coyote.loader.log.Log;


/**
 * This component runs at regular intervals and checks in with a server 
 * publishing data used to discover and manage the component where ever it may 
 * be running.
 * 
 * <p>Consider running CoyoteDX on an embedded device which gets its address 
 * via DHCP. The address may change regularly and this component will publish 
 * its address and port to a central server, so it can be found.
 * 
 * <p>This is how this component can be configured to run every 5 minutes:<pre>
 * {"Class":"coyote.demo.CheckIn","target":"https://coyote.systems/api/checkin","Schedule":{"Pattern":"/5 * * * *"}}</pre>
 */
public class CheckIn extends AbstractScheduledComponent {
  private static final String STATUS = "Status";
  private static final String HOSTNAME = "DnsName";
  private static final String OS_ARCH = "OSArch";
  private static final String OS_NAME = "OSName";
  private static final String OS_VERSION = "OSVersion";
  private static final String RUNTIME_NAME = "RuntimeName";
  private static final String RUNTIME_VENDOR = "RuntimeVendor";
  private static final String RUNTIME_VERSION = "RuntimeVersion";
  private static final String STARTED = "Started";
  private static final String USER_NAME = "Account";
  private static final String VM_AVAIL_MEM = "AvailableMemory";
  private static final String VM_CURR_HEAP = "CurrentHeap";
  private static final String VM_FREE_HEAP = "FreeHeap";
  private static final String VM_FREE_MEM = "FreeMemory";
  private static final String VM_HEAP_PCT = "HeapPercentage";
  private static final String VM_MAX_HEAP = "MaxHeapSize";
  private static final String FIXTURE_ID = "InstanceId";
  private static final String FIXTURE_NAME = "InstanceName";
  private static final String HOSTADDR = "IpAddress";
  private static final String NAME = "Name";
  private static final String UPTIME = "Uptime";

  private String target = null;




  /**
   * @see coyote.loader.component.AbstractScheduledComponent#setConfiguration(coyote.loader.cfg.Config)
   */
  @Override
  public void setConfiguration( Config config ) {
    super.setConfiguration( config );
    target = config.getAsString( ConfigTag.TARGET );

    if ( StringUtil.isBlank( target ) ) {
      Log.fatal( "No target URL configured - cannot run" );
      super.setEnabled( false );
    } else {
      try {
        new URL( target );
      } catch ( MalformedURLException e ) {
        Log.fatal( "Target URL is invalid: " + e.getMessage() );
        super.setEnabled( false );
      }
    }
  }




  @Override
  public void doWork() {

    // this sets our execution time to the exact millisecond based on when 
    // this job ACTUALLY ran. This is to ensure slow running jobs don't cause
    // execution delays 
    setExecutionTime( cronentry.getNextTime() );

    // perfom the check-in
    checkIn();
  }




  private int checkIn() {
    int responseCode = 0;
    StringBuffer response = new StringBuffer();

    //HttpsURLConnection con = null;
    HttpURLConnection con = null;

    try {
      URL url = new URL( target );

      Log.debug( "Checking in with " + target );

      // con = (HttpsURLConnection)obj.openConnection();
      con = (HttpURLConnection)url.openConnection();

      //add request header
      con.setRequestMethod( "PUT" );
      con.setRequestProperty( "User-Agent", "CheckIn" );
      con.setRequestProperty( "Accept-Language", "en-US,en;q=0.5" );
      con.setDoOutput( true );
      con.setDoInput( true );

      String json = createStatus();

      if ( json != null ) {
        Log.debug( "Sending:\n" + json );
        con.addRequestProperty( "Content-Type", MimeType.JSON.getType() );
        con.setRequestProperty( "Content-Length", Integer.toString( json.length() ) );
        try (OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() )) {
          writer.write( json );
          writer.flush();
        } catch ( Exception e ) {
          Log.error( e );
        }
      }

      responseCode = con.getResponseCode();
      Log.debug( "Response Code : " + responseCode );

      try (BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) )) {
        String inputLine;
        while ( ( inputLine = in.readLine() ) != null ) {
          response.append( inputLine );
        }
      }
    } catch ( IOException e ) {
      Log.warn( "Whoops:\n" + ExceptionUtil.stackTrace( e ) );
    }
    finally {
      if ( con != null ) {
        con.disconnect();
      }
    }

    Log.debug( "Response from server: " + response.toString() );

    return responseCode;
  }




  private String createStatus() {
    StatBoard statboard = loader.getStats();

    DataFrame retval = new DataFrame();
    retval.add( NAME, STATUS );

    // Add information from the loaders statistics board 
    retval.add( FIXTURE_ID, statboard.getId() );
    retval.add( FIXTURE_NAME, loader.getName() );
    retval.add( OS_NAME, System.getProperty( "os.name" ) );
    retval.add( OS_ARCH, System.getProperty( "os.arch" ) );
    retval.add( OS_VERSION, System.getProperty( "os.version" ) );
    retval.add( RUNTIME_VERSION, System.getProperty( "java.version" ) );
    retval.add( RUNTIME_VENDOR, System.getProperty( "java.vendor" ) );
    retval.add( RUNTIME_NAME, "Java" );
    retval.add( STARTED, DateUtil.ISO8601Format( statboard.getStartedTime() ) );
    retval.add( UPTIME, DateUtil.formatSignificantElapsedTime( ( System.currentTimeMillis() - statboard.getStartedTime() ) / 1000 ) );
    retval.add( USER_NAME, System.getProperty( "user.name" ) );
    retval.add( VM_AVAIL_MEM, new Long( statboard.getAvailableMemory() ) );
    retval.add( VM_CURR_HEAP, new Long( statboard.getCurrentHeapSize() ) );
    retval.add( VM_FREE_HEAP, new Long( statboard.getFreeHeapSize() ) );
    retval.add( VM_FREE_MEM, new Long( statboard.getFreeMemory() ) );
    retval.add( VM_MAX_HEAP, new Long( statboard.getMaxHeapSize() ) );
    retval.add( VM_HEAP_PCT, new Float( statboard.getHeapPercentage() ) );
    String text = statboard.getHostname();
    retval.add( HOSTNAME, ( text == null ) ? "unknown" : text );
    InetAddress addr = statboard.getHostIpAddress();
    retval.add( HOSTADDR, ( addr == null ) ? "unknown" : addr.getHostAddress() );

    return JSONMarshaler.toFormattedString( retval );
  }




  /**
   * A way to test the operation of this component.
   * 
   * @param args
   */
  public static void main( String[] args ) {
    // Add a logger that will send log messages to the console 
    Log.addLogger( Log.DEFAULT_LOGGER_NAME, new ConsoleAppender( -1L ) );

    // Create an instance of the component to "run"
    CheckIn cmpnt = new CheckIn();

    // Construct a configuration 
    Config cfg = new Config();
    cfg.add( "Class", CheckIn.class.getName() );

    // where we send our checkin data
    cfg.add( ConfigTag.TARGET, "https://coyote.systems/api/checkin" );

    // how often, (cron pattern)
    Config schedule = new Config();
    schedule.add( ConfigTag.PATTERN, "/5 * * * *" );
    cfg.add( ConfigTag.SCHEDULE, schedule );

    Log.info( "Configuring component with:\n" + cfg.toString() );

    // now set the configuration in the component
    cmpnt.setConfiguration( cfg ); // configure the monitor

    // Set a loader in the component so it has something to report against
    Loader loader = new DefaultLoader();
    cmpnt.setLoader( loader );

    cmpnt.doWork();
  }




  /**
   * @see coyote.loader.thread.ScheduledJob#toString()
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName()+" : "+target;
  }

  
  
}
