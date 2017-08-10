/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.mq;

import java.io.Closeable;
import java.io.IOException;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;


/**
 * Wrapper for an AMQP broker.
 */
public class TestBroker implements Closeable {

  private static final String INITIAL_CONFIG_PATH = "src/test/resources/qpid.json";
  public static final int DEFAULT_PORT = 7762;
  private static final String BROKER_URI = "amqp://guest:guest@localhost";
  public int port;
  private final Broker broker = new Broker();




  public TestBroker() {
    this(DEFAULT_PORT);
  }




  public TestBroker(int port) {
    this.port = port;
  }




  public String getBrokerUri() {
    return BROKER_URI + ":" + Integer.toString(port);
  }




  public void open() throws Exception {
    final BrokerOptions brokerOptions = new BrokerOptions();
    brokerOptions.setConfigProperty("qpid.amqp_port", Integer.toString(port));
    brokerOptions.setInitialConfigurationLocation(INITIAL_CONFIG_PATH);
    broker.startup(brokerOptions);
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    broker.shutdown();
  }

}
