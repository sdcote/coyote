/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 */
package coyote.commons.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import coyote.commons.network.IpAddress;


/**
 * Handles one session, i.e. parses the HTTP request and returns the
 * response.
 */
public interface IHTTPSession {

  void execute() throws IOException;




  CookieHandler getCookies();




  /**
   * @return a reference to the request header map; the headers in the request message.
   */
  Map<String, String> getRequestHeaders();




  /**
   * @return a reference to the response header map; the headers to go into the response message.
   */
  Map<String, String> getResponseHeaders();




  InputStream getInputStream();




  Method getMethod();




  Map<String, String> getParms();




  String getQueryParameterString();




  /**
   * Set the name of the user associated with this session.
   * 
   * <p>This is expected to be populated by the Auth Provider, but it will 
   * usually be null as not all resources will invoke the AuthProvider.
   * 
   * @param user the name of the user associated with this session, may be null.
   */
  void setUserName( String user );




  /**
   * Retrive the name of the user associated with this session.
   * 
   * <p>This is expected to be populated by the Auth Provider, but it will 
   * usually be null as not all resources will invoke the AuthProvider.
   */
  String getUserName();




  /**
   * Set the names of groups associated to the user associated to this session.
   * 
   * @param groups List of group names associated to the user in this session.
   */
  void setUserGroups( List<String> groups );




  /**
   * @return the list of group names associated to the user associated to this 
   *         session. Should not be null, but may be an empty list.
   */
  List<String> getUserGroups();




  /**
   * Get the remote IP address of the requester.
   * 
   * @return the IP address.
   */
  IpAddress getRemoteIpAddress();




  /**
   * Get the remote IP port of the requester.
   * 
   * @return the IP port.
   */
  int getRemoteIpPort();




  /**
   * @return the path part of the URL.
   */
  String getUri();




  /**
   * Parsed through the request body and returns a Body object with request 
   * entities.
   * 
   * <p>These entities are either in the form of a ByteBuffer or a File 
   * reference depending on the type of data contained in the request.
   * 
   * @return A collection of body entities
   * 
   * @throws IOException if there are problems reading the request stream
   * @throws ResponseException if there a logical HTTP issues with the format or encoding of the body 
   */
  Body parseBody() throws IOException, ResponseException;




  /**
   * @return true if the connection originated on a secured (encrypted) socket server, false if connection is over an unencrypted socket.
   */
  boolean isSecure();

}