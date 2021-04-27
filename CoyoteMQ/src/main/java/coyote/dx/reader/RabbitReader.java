/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.reader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.MarshalException;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.CMQ;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * 
 */
public class RabbitReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  private static final boolean NO_AUTO_ACK = false;
  private static final boolean REQUEUE = true;
  private static final boolean DURABLE = true;
  private static final boolean PUBLIC = false;
  private static final boolean KEEP = false;
  private static final Map<String, Object> NO_ARGUMENTS = null;

  private Connection connection = null;
  private Channel channel = null;
  private int prefetchCount = 1;
  private boolean peekEofCheck = true;




  public URI getBrokerURI() {
    if (configuration.containsIgnoreCase(ConfigTag.SOURCE)) {
      URI retval;
      try {
        retval = new URI(configuration.getString(ConfigTag.SOURCE));
        return retval;
      } catch (URISyntaxException e) {
        Log.debug(LogMsg.createMsg(CMQ.MSG, "Reader.config_attribute_is_not_valid_uri", ConfigTag.SOURCE, configuration.getString(ConfigTag.SOURCE)));
      }
    }
    return null;
  }




  public String getPassword() {
    if (configuration.containsIgnoreCase(ConfigTag.PASSWORD)) {
      return configuration.getString(ConfigTag.PASSWORD);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      return CipherUtil.decryptString(configuration.getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
    } else {
      return null;
    }
  }




  public String getUsername() {
    if (configuration.containsIgnoreCase(ConfigTag.USERNAME)) {
      return configuration.getString(ConfigTag.USERNAME);
    } else if (configuration.containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      return CipherUtil.decryptString(configuration.getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
    } else {
      return null;
    }
  }




  public boolean useSSL() {
    if (configuration.containsIgnoreCase(ConfigTag.USE_SSL)) {
      return configuration.getBoolean(ConfigTag.USE_SSL);
    }
    return false;
  }




  public boolean isListening() {
    if (configuration.containsIgnoreCase(ConfigTag.LISTEN)) {
      return configuration.getBoolean(ConfigTag.LISTEN);
    }
    return false;
  }




  public String getQueueName() {
    if (configuration.containsIgnoreCase(ConfigTag.QUEUE)) {
      return configuration.getString(ConfigTag.QUEUE);
    }
    return null;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    ConnectionFactory factory = new ConnectionFactory();

    try {
      factory.setUri(getBrokerURI());
      if (useSSL()) {
        factory.useSslProtocol();
      }

      String username = getUsername();
      if (StringUtil.isNotBlank(username)) {
        factory.setUsername(username);
        factory.setPassword(getPassword());
      }

      connection = factory.newConnection();
      channel = connection.createChannel();
      channel.basicQos(prefetchCount);
      channel.queueDeclare(getQueueName(), DURABLE, PUBLIC, KEEP, NO_ARGUMENTS);
    } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException | ShutdownSignalException | ConsumerCancelledException e) {
      Log.error(e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace(e));
      getContext().setError("Could not open " + getClass().getSimpleName() + ": " + e.getMessage());
    }

  }




  /**
   * @see coyote.dx.FrameReader#read(coyote.dx.context.TransactionContext)
   */
  @Override
  public DataFrame read(TransactionContext context) {
    DataFrame retval = null;
    try {
      while (retval == null) {
        GetResponse response = channel.basicGet(getQueueName(), NO_AUTO_ACK);
        if (response != null) {
          byte[] data = null;
          try {
            data = response.getBody();
            channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
          } catch (IOException e) {
            Log.error("Could not get data from message body: " + e.getClass().getName() + " - " + e.getMessage());
          }
          if (data != null) {
            try {
              retval = new DataFrame(data);
            } catch (Exception e) {
              Log.debug("Received data not in dataframe wire format");
              String text = StringUtil.getString(data);
              try {
                List<DataFrame> frames = JSONMarshaler.marshal(text);
                if (frames != null && frames.size() > 0) {
                  retval = frames.get(0);
                } else {
                  Log.notice("Received an empty JSON message");
                }
              } catch (MarshalException e1) {
                Log.debug("Received data not in JSON format");
                try {
                  List<DataFrame> frames = XMLMarshaler.marshal(text);
                  if (frames != null && frames.size() > 0) {
                    retval = frames.get(0);
                  } else {
                    Log.notice("Received an empty XML message");
                  }
                } catch (MarshalException e2) {
                  Log.error("Could not parse the data received from " + channel.toString());
                }
              }
            }
          } else {
            Log.warn("Retrieved an empty body from a message: " + response.getEnvelope().getDeliveryTag());
          }
        } else {
          // If we are not in listen mode, break out of the loop and return null, otherwise loop
          if (!isListening()) {
            break;
          }
        }
      }
    } catch (IOException e) {
      Log.warn("Exception on message retrieval: " + e.getClass().getName() + " - " + e.getMessage());
    }
    return retval;
  }




  /**
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    boolean retval = true;
    if (isListening()) {
      retval = false;
    } else {
      if (peekEofCheck) {
        try {
          GetResponse response = channel.basicGet(getQueueName(), NO_AUTO_ACK);
          if (response != null) {
            retval = false;
            try {
              channel.basicReject(response.getEnvelope().getDeliveryTag(), REQUEUE);
            } catch (Exception e) {
              Log.error("Could not requeue message on EOF check: " + e.getClass().getName() + " - " + e.getMessage());
            }
          }
        } catch (IOException e) {
          Log.error("Exception on EOF check: " + e.getClass().getName() + " - " + e.getMessage());
        }
      }
    }
    return retval;
  }




  /**
   * @see coyote.dx.reader.AbstractFrameReader#close()
   */
  @Override
  public void close() throws IOException {
    // perform our closing functions first
    if (connection != null) {
      try {
        connection.close();
      } catch (AlreadyClosedException e) {
        // not important during close, AlreadyClosedException is common
      }
    }

    // perform base class closing functions last
    super.close();
  }

}
