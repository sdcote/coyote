/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.azure;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;


public final class IotHubUri {
  /**
   * The format of the URI path.  */
  public static final String PATH_FORMAT = "/devices/%s%s";

  /** The API version will be passed as a param in the URI. */
  public static final String API_VERSION = "api-version=2016-02-03";

  /** The charset used when URL-encoding the IoT Hub name and device ID. */
  public static final Charset IOTHUB_URL_ENCODING_CHARSET = StandardCharsets.UTF_8;

  protected String hostname;

  protected String path;

  protected String uri;




  public IotHubUri() {
    // default constructor
  }




  /**
   * URL-encodes the query param {@code name} and {@code value} using charset UTF-8 and
   * appends them to the URI.
   *
   * @param uriBuilder the URI.
   * @param name the query param name.
   * @param value the query param value.
   */
  protected static void appendQueryParam(final StringBuilder uriBuilder, final String name, final String value) {
    try {
      final String urlEncodedName = URLEncoder.encode(name, IOTHUB_URL_ENCODING_CHARSET.name());
      final String urlEncodedValue = URLEncoder.encode(value, IOTHUB_URL_ENCODING_CHARSET.name());
      uriBuilder.append(urlEncodedName);
      uriBuilder.append("=");
      uriBuilder.append(urlEncodedValue);
    } catch (final UnsupportedEncodingException e) {
      // should never happen, since the encoding is hard-coded.
      throw new IllegalStateException(e);
    }
  }




  /**
   * Returns the string representation of the IoT Hub resource URI. 
   * 
   * <p>The IoT Hub resource URI is the hostname and path component that is 
   * common to all IoT Hub communication methods between the given device and 
   * IoT Hub.</p>
   *
   * @param iotHubHostname the IoT Hub hostname.
   * @param deviceId the device ID.
   *
   * @return the string representation of the IoT Hub resource URI with the 
   *         format '[iotHubHostname]/devices/[deviceId]'.
   */
  public static String getResourceUri(final String iotHubHostname, final String deviceId) {
    final IotHubUri iotHubUri = new IotHubUri(iotHubHostname, deviceId, "");
    return iotHubUri.getHostname() + iotHubUri.getPath();
  }




  /**
   * URL-encodes each subdirectory in the path.
   *
   * @param path the path to be safely escaped.
   *
   * @return a path with each subdirectory URL-encoded.
   */
  protected static String urlEncodePath(final String path) {
    final String[] pathSubDirs = path.split("/");
    final StringBuilder urlEncodedPathBuilder = new StringBuilder();
    try {
      for (final String subDir : pathSubDirs) {
        if (subDir.length() > 0) {
          final String urlEncodedSubDir = URLEncoder.encode(subDir, IOTHUB_URL_ENCODING_CHARSET.name());
          urlEncodedPathBuilder.append("/");
          urlEncodedPathBuilder.append(urlEncodedSubDir);
        }
      }
    } catch (final UnsupportedEncodingException e) {
      // should never happen.
      throw new IllegalStateException(e);
    }

    return urlEncodedPathBuilder.toString();
  }




  /**
   * Equivalent to {@code new IotHubUri(iotHubHostname, deviceId, 
   * iotHubMethodPath, null)}.
   * 
   * <p>Results in a URI pointing to the address 
   * '[hostname]/devices/[deviceId]/[method path]?api-version=2016-02-03'
   *
   * @param iotHubHostname the IoT Hub hostname.
   * @param deviceId the device ID.
   * @param iotHubMethodPath the path from the IoT Hub resource to the method.
   */
  public IotHubUri(final String iotHubHostname, final String deviceId, final String iotHubMethodPath) {
    // Codes_SRS_IOTHUBURI_11_007: [The constructor shall return .]
    // Codes_SRS_IOTHUBURI_11_015: [The constructor shall URL-encode the device ID.]
    // Codes_SRS_IOTHUBURI_11_016: [The constructor shall URL-encode the IoT Hub method path.]
    this(iotHubHostname, deviceId, iotHubMethodPath, null);
  }




  /**
   * Creates a URI to an IoT Hub method. 
   * 
   * <p>The URI does not include a protocol. The function will safely escape 
   * the given arguments.</p>
   *
   * @param iotHubHostname the IoT Hub hostname.
   * @param deviceId the device ID.
   * @param iotHubMethodPath the path from the IoT Hub resource to the method.
   * @param queryParams the URL query parameters. Can be null.
   */
  public IotHubUri(final String iotHubHostname, final String deviceId, final String iotHubMethodPath, final Map<String, String> queryParams) {
    hostname = iotHubHostname;

    final String rawPath = String.format(PATH_FORMAT, deviceId, iotHubMethodPath);
    path = urlEncodePath(rawPath);

    final StringBuilder uriBuilder = new StringBuilder(hostname);
    uriBuilder.append(path);
    uriBuilder.append("?");
    uriBuilder.append(API_VERSION);
    if (queryParams != null) {
      final Iterator<Map.Entry<String, String>> paramIter = queryParams.entrySet().iterator();
      while (paramIter.hasNext()) {
        final Map.Entry<String, String> param = paramIter.next();
        uriBuilder.append("&");
        appendQueryParam(uriBuilder, param.getKey(), param.getValue());
      }
    }

    uri = uriBuilder.toString();
  }




  /**
   * @return the string representation of the IoT Hub hostname.
   */
  public String getHostname() {
    return hostname;
  }




  /**
   * @return the string representation of the IoT Hub path.
   */
  public String getPath() {
    return path;
  }




  /**
   * @return the string representation of the IoT Hub URI.
   */
  @Override
  public String toString() {
    return uri;
  }
}
