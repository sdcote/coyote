package coyote.commons.network.mqtt;

import java.util.Timer;
import java.util.TimerTask;

import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * Default ping sender implementation
 *
 * <p>This class implements the {@link PingSender} pinger interface
 * allowing applications to send ping packet to server every keep alive interval.
 * </p>
 *
 * @see PingSender
 */
public class TimerPingSender implements PingSender {
  private class PingTask extends TimerTask {

    @Override
    public void run() {
      Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "pingtimer.run", System.currentTimeMillis() ) );
      connection.checkForActivity();
    }
  }

  private Connection connection;

  private Timer timer;




  @Override
  public void init( final Connection conn ) {
    if ( conn == null ) {
      throw new IllegalArgumentException( "Connection cannot be null." );
    }
    connection = conn;
  }




  @Override
  public void schedule( final long delayInMilliseconds ) {
    timer.schedule( new PingTask(), delayInMilliseconds );
  }




  @Override
  public void start() {
    final String clientid = connection.getClient().getClientId();

    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "pingtimer.start", clientid ) );

    timer = new Timer( "MQTT Ping: " + clientid );
    //Check ping after first keep alive interval.
    timer.schedule( new PingTask(), connection.getKeepAlive() );
  }




  @Override
  public void stop() {
    Log.append( MQTT.EVENT, LogMsg.createMsg( MQTT.MSG, "pingtimer.stopped" ) );
    if ( timer != null ) {
      timer.cancel();
    }
  }

}
