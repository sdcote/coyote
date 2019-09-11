package coyote.dx.writer;

import coyote.commons.CipherUtil;
import coyote.commons.ExceptionUtil;
import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.CMC;
import coyote.dx.ConfigTag;
import coyote.dx.FrameWriter;
import coyote.dx.context.TransformContext;
import coyote.loader.Loader;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;
import coyote.mc.prom.BasicAuthHttpConnectionFactory;
import coyote.mc.prom.DefaultHttpConnectionFactory;
import coyote.mc.prom.HttpConnectionFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * This writer collects all the metric sample and generates an OpenMetric payload to sent to a Prometheus PushGateway.
 * You can see the metrics on the PushGateway by going to its "metric" endpoint: http://localhost:9091/metrics
 *
 * <p>The basic operation is the writer will look for an "action" field which will either be "POST" or "DELETE". If no
 * action field is found, a POST will be assumed.</p>
 *
 * <p>Received dataframes will be processed in the following manner:<ul>
 * <li>The "name" field will be used as the name of the metric.</li>
 * <li>The "value" field will be used as the value of the of the metric.</li>
 * <li>The "type" field will be used as the type of the metric.</li>
 * <li>The "job" field(optional) will be used as the job grouping key with "coyotemc" being used as the default.</li>
 * <li>The "help" field (optional) will be used as help description of the metric.</li>
 * <li>The "instance" field (optional) will be used for the instance grouping key with the local hostname being used as the default.</li>
 * <li>The "action" field (optional) will be used to control the HTTP method used. The default is "POST" but "DELETE" and "PUT" is also supported.</li>
 * <li>All other fields will be used as labels if they contain non-blank data in both name and value.</li>
 * </ul>
 *
 * <p>The basic configuration is as follows:<pre>
 * "Writer": {
 * 	  "class": "PushGatewayWriter",
 * 	  "target": "http://localhost:9091",
 * }</pre>
 * <p>If you want to use Basic Authentication when posting metrics, use the following format:<pre>
 * "Writer": {
 * 	  "class": "PushGatewayWriter",
 * 	  "target": "http://localhost:9091",
 *   "username": "jqpublic",
 *   "password": "s0mep4s5word"
 * }</pre>
 * <p>The above will send a preemptive basic authentication header with each request.</p>
 */
public class PushGatewayWriter extends AbstractFrameWriter implements FrameWriter {
  public final static String CONTENT_TYPE_004 = "text/plain; version=0.0.4; charset=utf-8";
  private static final String DEFAULT_JOB_NAME = "coyotemc";
  private static final String DELETE = "DELETE";
  private static final String POST = "POST";
  private static final String DEFAULT_ACTION = POST;
  private static final String JOB_FIELD = "job";
  private static final String ACTION_FIELD = "action";
  private static final String NAME_FIELD = "name";
  private static final String HELP_FIELD = "help";
  private static final String TYPE_FIELD = "type";
  private static final String VALUE_FIELD = "value";
  private static final String INSTANCE_FIELD = "instance";

  private String gatewayUrl;
  private HttpConnectionFactory connectionFactory = new DefaultHttpConnectionFactory();

  private int rowCounter = 0;

  private static String base64url(String value) {
    return DatatypeConverter.printBase64Binary(value.getBytes(StandardCharsets.UTF_8)).replace("+", "-").replace("/", "_");
  }

  private static String readFromStream(InputStream is) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = is.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }
    return result.toString("UTF-8");
  }

  private static void writeEscapedHelp(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }

  private static void writeEscapedLabelValue(Writer writer, String s) throws IOException {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          writer.append("\\\\");
          break;
        case '\"':
          writer.append("\\\"");
          break;
        case '\n':
          writer.append("\\n");
          break;
        default:
          writer.append(c);
      }
    }
  }

  /**
   * Initialize the writer
   *
   * @param context The transformation context in which this component should be opened.
   */
  @Override
  public void open(TransformContext context) {
    super.open(context);

    String targetUrl = getTarget();
    if (StringUtil.isBlank(targetUrl)) {
      context.setError("The Writer configuration did not contain the '" + ConfigTag.TARGET + "' element");
      context.setState("Configuration Error");
      return;
    } else {
      gatewayUrl = URI.create(targetUrl + "/metrics/").normalize().toString();
      Log.debug(LogMsg.createMsg(CDX.MSG, "Writer.using_target", this.getClass().getSimpleName(), targetUrl));
    }

    // Look for credentials
    String username = getConfiguration().getString(ConfigTag.USERNAME);
    if (username == null) {
      String encryptedUsername = getConfiguration().getString(Loader.ENCRYPT_PREFIX + ConfigTag.USERNAME);
      if (encryptedUsername != null) username = CipherUtil.decryptString(encryptedUsername);
    }
    String password = getConfiguration().getString(ConfigTag.PASSWORD);
    if (password == null) {
      String encryptedPassword = getConfiguration().getString(Loader.ENCRYPT_PREFIX + ConfigTag.PASSWORD);
      if (encryptedPassword != null) password = CipherUtil.decryptString(encryptedPassword);
    }
    if (StringUtil.isNotEmpty(username)) {
      connectionFactory = new BasicAuthHttpConnectionFactory(connectionFactory, username, password);
    }

    Log.debug(LogMsg.createMsg(CMC.MSG, "Writer.init_complete"));
  }

  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    Log.debug(LogMsg.createMsg(CMC.MSG, "Writer.records_processed", rowCounter, (context != null) ? context.getRow() : 0));
    super.close();
  }

  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write(DataFrame frame) {
    if (expression != null) {
      try {
        if (evaluator.evaluateBoolean(expression)) {
          if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Condition is true...writing frame to resource");
          writeFrame(frame);
        } else {
          if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Expression evaluated to false - frame not written");
        }
      } catch (final IllegalArgumentException e) {
        Log.warn(LogMsg.createMsg(CMC.MSG, "Writer.boolean_evaluation_error", e.getMessage()));
      }
    } else {
      if (Log.isLogging(Log.DEBUG_EVENTS)) Log.debug("Unconditionally writing frame");
      writeFrame(frame);
    }
  }

  /**
   * This is where we actually write a frame to the web service endpoint
   *
   * @param frame the frame to write
   * @return the number of bytes written
   */
  private void writeFrame(DataFrame frame) {
    String jobName = frame.getAsString(JOB_FIELD);
    if (StringUtil.isBlank(jobName)) jobName = DEFAULT_JOB_NAME;

    String action = frame.getAsString(ACTION_FIELD);
    if (StringUtil.isBlank(action)) action = DEFAULT_ACTION;
    action = action.toUpperCase();

    if (!frame.contains(NAME_FIELD)) {
      Log.error("Cannot write metric without the '" + NAME_FIELD + "' field");
      return;
    }
    if (!frame.contains(VALUE_FIELD)) {
      Log.error("Cannot write metric without the '" + VALUE_FIELD + "' field");
      return;
    }
    if (!frame.contains(TYPE_FIELD)) {
      Log.error("Cannot write metric without the '" + TYPE_FIELD + "' field");
      return;
    }

    Map<String, String> groupingKey = new HashMap<>();
    if (frame.contains(INSTANCE_FIELD) && StringUtil.isNotBlank(frame.getAsString(INSTANCE_FIELD)))
      groupingKey.put(INSTANCE_FIELD, frame.getAsString(INSTANCE_FIELD));

    try {
      publishMetric(jobName, groupingKey, action, frame);
    } catch (IOException e) {
      Log.error("Could not push metric: " + ExceptionUtil.toString(e));
    }
    rowCounter++;
  }

  /**
   * Publish the given frame as an OpenMetrics formatted metric.
   *
   * @param job         primary grouping element representing the name of the job to which these metrics apply.
   * @param groupingKey additional grouping pairs such as "instance-myhost"
   * @param method      One of the HTTP methods (e.g. POST, PUT, and DELETE)
   * @param frame       the dataframe containing the metric to push.
   * @throws IOException if there were problems sending metrics to the push gateway
   */
  void publishMetric(String job, Map<String, String> groupingKey, String method, DataFrame frame) throws IOException {
    String url = gatewayUrl;
    if (job.contains("/")) {
      url += "job@base64/" + base64url(job);
    } else {
      url += "job/" + URLEncoder.encode(job, "UTF-8");
    }

    if (groupingKey != null) {
      for (Map.Entry<String, String> entry : groupingKey.entrySet()) {
        if (entry.getValue().contains("/")) {
          url += "/" + entry.getKey() + "@base64/" + base64url(entry.getValue());
        } else {
          url += "/" + entry.getKey() + "/" + URLEncoder.encode(entry.getValue(), "UTF-8");
        }
      }
    }

    HttpURLConnection connection = connectionFactory.create(url);
    connection.setRequestProperty("Content-Type", CONTENT_TYPE_004);
    if (!method.equals(DELETE)) {
      connection.setDoOutput(true);
    }
    connection.setRequestMethod(method);
    connection.setConnectTimeout(10000);
    connection.setReadTimeout(10000);
    connection.connect();

    try {
      if (!method.equals(DELETE)) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
        convertToOpenMetrics(writer, frame);
        writer.flush();
        writer.close();
      }

      int response = connection.getResponseCode();
      if (response != HttpURLConnection.HTTP_ACCEPTED) {
        String errorMessage;
        InputStream errorStream = connection.getErrorStream();
        if (response >= 400 && errorStream != null) {
          String errBody = readFromStream(errorStream);
          errorMessage = "Response code from " + url + " was " + response + ", response body: " + errBody;
        } else {
          errorMessage = "Response code from " + url + " was " + response;
        }
        throw new IOException(errorMessage);
      }
    } finally {
      connection.disconnect();
    }
  }

  /**
   * Convert the given frame into an OpenMetrics formatted representation
   *
   * @param writer the writer to use in outputing the representation
   * @param frame  the frame to format
   * @throws IOException if problems were encountered writing the data
   */
  private void convertToOpenMetrics(BufferedWriter writer, DataFrame frame) throws IOException {
    String metricName = frame.getAsString(NAME_FIELD);

    if (frame.contains(HELP_FIELD) && frame.getAsString(HELP_FIELD).trim().length() > 0) {
      writer.append("# HELP ");
      writer.append(metricName);
      writer.write(' ');
      writeEscapedHelp(writer, frame.getAsString(HELP_FIELD).trim());
      writer.append("\n");
    }
    writer.append("# TYPE ");
    writer.append(metricName);
    writer.append(" ");
    writer.append(frame.getAsString(TYPE_FIELD).trim());
    writer.append("\n");
    writer.append(metricName);
    writer.append(" ");

    List<String> names = getLabelNames(frame);
    if (names.size() > 0) {
      writer.write('{');
      for (int i = 0; i < names.size(); ++i) {
        writer.write(names.get(i));
        writer.write("=\"");
        writeEscapedLabelValue(writer, frame.getAsString(names.get(i)));
        writer.write("\"");
        if (i + 1 < names.size()) writer.write(",");
      }
      writer.write("} ");
    }

    writer.append(frame.getAsString(VALUE_FIELD));
    writer.append("\n");
  }

  /**
   * Get all the "other" fileds in the frame and use them as label named.
   *
   * @param frame the frame to examine
   * @return a list o label names
   */
  private List<String> getLabelNames(DataFrame frame) {
    HashSet<String> hset = new HashSet<String>();
    for (DataField field : frame.getFields()) {
      String name = field.getName();
      if (name != null &&
              !NAME_FIELD.equals(name) &&
              !VALUE_FIELD.equals(name) &&
              !TYPE_FIELD.equals(name) &&
              !JOB_FIELD.equals(name) &&
              !HELP_FIELD.equals(name) &&
              !INSTANCE_FIELD.equals(name) &&
              !ACTION_FIELD.equals(name)) hset.add(field.getName());
    }
    return new ArrayList<String>(hset);
  }
}



