/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.writer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mq.CMQ;


/**
 * 
 */
public class RabbitWriter extends AbstractFrameWriter implements FrameWriter, ConfigurableComponent {

  private static final String BINARY = "Binary";
  private static final String JSON = "JSON";
  private static final String XML = "XML";
  private Connection connection = null;
  private Channel channel = null;




  public URI getBrokerURI() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.TARGET)) {
      URI retval;
      try {
        retval = new URI(getConfiguration().getString(ConfigTag.TARGET));
        return retval;
      } catch (URISyntaxException e) {
        Log.debug(LogMsg.createMsg(CMQ.MSG, "Reader.config_attribute_is_not_valid_uri", ConfigTag.TARGET, getConfiguration().getString(ConfigTag.TARGET)));
      }
    }
    return null;
  }




  public String getUsername() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.USERNAME)) {
      return getConfiguration().getString(ConfigTag.USERNAME);
    } else if (getConfiguration().containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME)) {
      return CipherUtil.decryptString(getConfiguration().getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME));
    } else {
      return null;
    }
  }




  public boolean useSSL() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.USE_SSL)) {
      String fieldname = getConfiguration().getFieldIgnoreCase(ConfigTag.USE_SSL).getName();
      return getConfiguration().getBoolean(fieldname);
    }
    return false;
  }




  public String getPassword() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.PASSWORD)) {
      return getConfiguration().getString(ConfigTag.PASSWORD);
    } else if (getConfiguration().containsIgnoreCase(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD)) {
      return CipherUtil.decryptString(getConfiguration().getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD));
    } else {
      return null;
    }
  }




  public String getQueueName() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.QUEUE)) {
      return getConfiguration().getString(ConfigTag.QUEUE);
    }
    return null;
  }




  public String getFormat() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.FORMAT)) {
      return getConfiguration().getString(ConfigTag.FORMAT);
    }
    return null;
  }




  public String getEncoding() {
    if (getConfiguration().containsIgnoreCase(ConfigTag.ENCODING)) {
      return getConfiguration().getString(ConfigTag.ENCODING);
    }
    return null;
  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String format = getFormat();
    if (StringUtil.isNotBlank(format)) {
      if (format.equalsIgnoreCase(JSON) || format.equalsIgnoreCase(XML)) {
        String encoding = getEncoding();
        try {
          "Testing".getBytes(encoding);
        } catch (final java.io.UnsupportedEncodingException e) {
          Log.error("Unsupported string encoding of '" + encoding + "'");
          getContext().setError("Unsupported string encoding of '" + encoding + "'");
        }
      }
    } else {
      Log.error("Unsupported message format of '" + format + "' JSON, XML, and Binary are the currently supported options");
      getContext().setError("Unsupported message format of '" + format + "'");
    }

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
      channel.queueDeclare(getQueueName(), true, false, false, null);

    } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException | IOException | TimeoutException | ShutdownSignalException | ConsumerCancelledException e) {
      Log.error(e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace(e));
      getContext().setError("Could not open " + getClass().getSimpleName() + ": " + e.getMessage());
    }

  }




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (frame != null) {
      byte[] data;

      try {
        String format = getFormat();
        if (StringUtil.isNotBlank(format)) {

          if (format.equalsIgnoreCase(BINARY)) {
            data = frame.getBytes();
          } else {
            String datastring;
            if (format.equalsIgnoreCase(JSON)) {
              datastring = JSONMarshaler.marshal(frame);
            } else if (format.equalsIgnoreCase(XML)) {
              datastring = XMLMarshaler.marshal(frame);
            } else {
              Log.error("Unsupported message format of '" + format + "' JSON and XML are the currently supported options");
              getContext().setError("Unsupported message format of '" + format + "'");
              return;
            }
            String encoding = getEncoding();
            if (StringUtil.isNotBlank(encoding)) {
              try {
                data = datastring.getBytes(encoding);
              } catch (Exception e) {
                Log.error("Unsupported string encoding of '" + encoding + "'");
                getContext().setError("Unsupported string encoding of '" + encoding + "'");
                return;
              }
            } else {
              data = StringUtil.getBytes(datastring);
            }
          }
        } else {
          data = frame.getBytes();
        }

        channel.basicPublish("", getQueueName(), null, data);
        Log.debug("Sent " + data.length + " bytes to '" + getQueueName() + "'");
      } catch (IOException e) {
        Log.error(e.getClass().getSimpleName() + ":" + e.getMessage() + "\n" + ExceptionUtil.stackTrace(e));
      }
    }
  }




  /**
   * @see coyote.dx.writer.AbstractFrameFileWriter#close()
   */
  @Override
  public void close() throws IOException {
    if (connection != null) {
      try {
        connection.close();
      } catch (AlreadyClosedException e) {
        // not important during close, AlreadyClosedException is common
      }
    }

    super.close();
  }

}
