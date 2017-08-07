/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.azure;

/**
 * Configuration for IoT hub.
 */
public class IotHubConfig {

  String hubname;
  String deviceId;
  String deviceKey;

  String iothostname = "azure-devices.net";

  String sasToken = "";
  String scheme = "ssl"; // "tcp" or "ssl"
  String port = "8883";

  char[] password = sasToken.toCharArray();

  /**
   * The number of seconds after which the generated SAS token for a message
   * will become invalid.
   */
  public static final long DEFAULT_TOKEN_VALID_SECS = 3600;




  /**
   * @param hubname name of the hub (excluding the host.domain)
   * @param deviceId the device identifier
   * @param deviceKey the primary or secondary key of the device
   */
  public IotHubConfig(String hubname, String deviceId, String deviceKey) {
    this.hubname = hubname;
    this.deviceId = deviceId;
    this.deviceKey = deviceKey;
  }




  /**
   * @return the full URI to the IoT Hub
   */
  public String getBrokerUri() {
    return scheme + "://" + getIotHubHostname() + ":" + port;
  }




  /**
   * @return the full hostname of the IoT Hub
   */
  private String getIotHubHostname() {
    return hubname + "." + iothostname;
  }




  /**
   * @return the client identifier
   */
  public String getClientId() {
    return deviceId;
  }




  /**
   * @return the name of the topic on which we can send data.
   */
  public String getSendTopic() {
    return "devices/" + deviceId + "/messages/events/";
  }




  /**
   * @return the topic on which we can receive data from the IoT hub.
   */
  public String getReceiveTopic() {
    return "devices/" + deviceId + "/messages/devicebound/#";
  }




  /**
   * @return the name of the user to be used to identify this client.
   */
  public String getUsername() {
    return getIotHubHostname() + "/" + deviceId;
  }




  /**
   * This generates the appropriate password for the Azure IoT hub.
   * 
   * <p>The creation of the password (actually a Sharded Access Signature or 
   * SAS token) involves several steps. First, the scope is determined based on 
   * the name of the hub and the device identifier. Next, the expiration time 
   * for the password is determined. The current default expiration is 1 hour.
   * A SAS token is generated using the scope, device key and the password 
   * expiration.</p>
   * 
   * <p>The creation of the SAS Token involves the following steps:<ol>
   * <li>Build the raw signature string as "[scope]\n[expiry]" and encode 
   * it into UTF-8 bytes,</li>
   * <li>Decode the device key using Base64,</li>
   * <li>Generate a HMAC digest of the raw signature using SHA256 has function 
   * (a.k.a HMACSHA256) with the decoded device key as the secret,</li>
   * <li>Encode the encrypted signature using Base64,</li>
   * <li>Encode the signature using the UTF-8 character set,</li>
   * <li>Make the string web safe.</li>
   * </ol>
   * 
   * @return the password to use when connecting to the broker
   */
  public char[] getPassword() {

    String scope = IotHubUri.getResourceUri(getIotHubHostname(), deviceId);

    long expiry = System.currentTimeMillis() / 1000L + getTokenValidSecs() + 1L;

    SasToken token = new SasToken(scope, deviceKey, expiry);

    return token.toString().toCharArray();
  }




  /**
   * @return the number of seconds any SAS token generated is to expire.
   */
  private long getTokenValidSecs() {
    return DEFAULT_TOKEN_VALID_SECS;
  }

}
