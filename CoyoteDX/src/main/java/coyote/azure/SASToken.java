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

/**
 * 
 */
public class SASToken {
  /**
   * The SAS token format. The parameters to be interpolated are, in order:
   * the signature, the expiry time, the key name (device ID), and the
   * resource URI.
   */
  public static final String TOKEN_FORMAT = "SharedAccessSignature sig=%s&se=%s&sr=%s";

  /** Components of the SAS token. */
  protected final String signature;
  
  /** The time, as a UNIX timestamp, before which the token is valid. */
  protected final long expiry;
  
  /**
   * The URI for a connection from a device to an IoT Hub. Does not include a
   * protocol.
   */
  protected final String scope;




  /**
   * Constructs a SAS token that grants access to an IoT Hub for the specified 
   * amount of time.
   *
   * @param scope the resource URI.
   * @param key the device key.
   * @param expiry the time, as a UNIX timestamp, after which the token
   *        will become invalid.
   */
  public SASToken( String scope, String key, long expiry ) {
    this.scope = scope;
    this.expiry = expiry;
    Signature sig = new Signature( this.scope, this.expiry, key );
    this.signature = sig.toString();
  }




  /**
   * Returns the string representation of the SAS token.
   *
   * @return the string representation of the SAS token.
   */
  @Override
  public String toString() {
    return buildSasToken();
  }



/**
 * Format the token into a string with the following format:<pre>
 * SharedAccessSignature sig=[signature]&#38;se=[expiry]&#38;sr=[URI]"</pre>

 * @return a formatted SAS Token suitable for use in connection parameters
 */
  protected String buildSasToken() {
    return String.format( TOKEN_FORMAT, this.signature, this.expiry, this.scope );
  }




  
}
