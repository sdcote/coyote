/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */

package coyote.azure;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public final class Signature {

  /** The string representation of the signature. */
  protected String sig;

  /**
   * The device ID will be the prefix. The expiry time, as a UNIX timestamp, 
   * will be the suffix.
   */
  public static final String RAW_SIGNATURE_FORMAT = "%s\n%s";

  /** The charset used for the raw and hashed signature. */
  public static final Charset SIGNATURE_CHARSET = StandardCharsets.UTF_8;




  /**
   * Constructs a {@code Signature} instance from the given resource URI, 
   * expiry time and device key.
   * 
   * @param resourceUri the resource URI.
   * @param expiryTime the time, as a UNIX timestamp, after which the token
   *        will become invalid.
   *
   * @param deviceKey the device key.
   */
  public Signature(String resourceUri, long expiryTime, String deviceKey) {
    // Create the raw signature encoded in UTF-8
    byte[] rawSig = buildRawSignature(resourceUri, expiryTime);

    // Decode the device key using Base64
    byte[] decodedDeviceKey = Base64.getDecoder().decode(deviceKey.getBytes());

    // Generate a HMAC digest the raw signature using SHA256 with the decoded 
    // device key as the secret 
    byte[] encryptedSig = encryptSignature(rawSig, decodedDeviceKey);

    // Encode the encrypted signature using Base64
    byte[] encryptedSigBase64 = Base64.getEncoder().encode(encryptedSig);

    // Encode the signature using the UTF-8 character set
    String utf8Sig = new String(encryptedSigBase64, SIGNATURE_CHARSET);

    // Make the string web safe
    this.sig = encodeSignatureWebSafe(utf8Sig);
  }




  /**
   * Returns the string representation of the signature.
   *
   * @return the string representation of the signature.
   */
  @Override
  public String toString() {
    return this.sig;
  }




  /**
   * Builds the raw signature.
   * 
   * <p>This builds the raw signature string as "[scope]\n[expiry]" and encodes 
   * it into UTF-8 bytes.</p>
   *
   * @param scope the resource URI.
   * @param expiry the signature expiry time, as a UNIX timestamp.
   *
   * @return the raw signature.
   */
  public static byte[] buildRawSignature(String scope, long expiry) {
    return String.format(RAW_SIGNATURE_FORMAT, scope, expiry).getBytes(SIGNATURE_CHARSET);
  }




  /**
   * Encrypts the signature using HMAC-SHA256 using the device key is the 
   * secret for the algorithm.
   *
   * @param sig the unencrypted signature.
   * @param deviceKey the Base64-decoded device key.
   *
   * @return the HMAC-SHA256 encrypted signature.
   */
  public static byte[] encryptSignature(byte[] sig, byte[] deviceKey) {
    String hmacSha256 = "HmacSHA256";

    SecretKeySpec secretKey = new SecretKeySpec(deviceKey, hmacSha256);

    byte[] encryptedSig = null;
    try {
      Mac macSha256 = Mac.getInstance(hmacSha256);
      macSha256.init(secretKey);
      encryptedSig = macSha256.doFinal(sig);
    } catch (NoSuchAlgorithmException e) {
      // should never happen, since the algorithm is hard-coded.
    } catch (InvalidKeyException e) {
      // should never happen, since the input key type is hard-coded.
    }

    return encryptedSig;
  }




  /**
   * Safely escapes characters in the signature so that they can be transmitted 
   * over the Internet. 
   * 
   * <p>Replaces unsafe characters with a '%' followed by two hexadecimal 
   * digits (i.e. %2d). This function also replaces spaces with '+' signs.</p>
   *
   * @param sig the HMAC-SHA256 encrypted, Base64-encoded, UTF-8 encoded
   *            signature.
   *
   * @return the web-safe encoding of the signature.
   */
  public static String encodeSignatureWebSafe(String sig) {
    String strSig = "";
    try {
      strSig = URLEncoder.encode(sig, SIGNATURE_CHARSET.name());
    } catch (UnsupportedEncodingException e) {
      // should never happen, since the encoding is hard-coded.
      throw new IllegalStateException(e);
    }

    return strSig;
  }

}
