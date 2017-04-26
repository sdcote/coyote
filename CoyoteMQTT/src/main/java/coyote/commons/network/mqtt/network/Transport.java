package coyote.commons.network.mqtt.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import coyote.commons.network.mqtt.MqttException;


public interface Transport {

  public InputStream getInputStream() throws IOException;




  public OutputStream getOutputStream() throws IOException;




  public String getServerURI();




  public void start() throws IOException, MqttException;




  public void stop() throws IOException;

}
