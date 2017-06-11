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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coyote.commons.StringUtil;


class ContentType {

  private static final String ASCII_ENCODING = "US-ASCII";
  private static final String MULTIPART_FORM_DATA_HEADER = "multipart/form-data";
  private static final String CONTENT_REGEX = "[ |\t]*([^/^ ^;^,]+/[^ ^;^,]+)";
  private static final Pattern MIME_PATTERN = Pattern.compile( CONTENT_REGEX, Pattern.CASE_INSENSITIVE );
  private static final String CHARSET_REGEX = "[ |\t]*(charset)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
  private static final Pattern CHARSET_PATTERN = Pattern.compile( CHARSET_REGEX, Pattern.CASE_INSENSITIVE );
  private static final String BOUNDARY_REGEX = "[ |\t]*(boundary)[ |\t]*=[ |\t]*['|\"]?([^\"^'^;^,]*)['|\"]?";
  private static final Pattern BOUNDARY_PATTERN = Pattern.compile( BOUNDARY_REGEX, Pattern.CASE_INSENSITIVE );

  private final String contentTypeHeader;
  private final String contentType;
  private final String encoding;
  private final String boundary;




  public ContentType( final String contentTypeHeader ) {
    this.contentTypeHeader = contentTypeHeader;
    if ( contentTypeHeader != null ) {
      contentType = getDetailFromContentHeader( contentTypeHeader, MIME_PATTERN, "", 1 );
      encoding = getDetailFromContentHeader( contentTypeHeader, CHARSET_PATTERN, null, 2 );
    } else {
      contentType = "";
      encoding = "UTF-8";
    }
    if ( MULTIPART_FORM_DATA_HEADER.equalsIgnoreCase( contentType ) ) {
      boundary = getDetailFromContentHeader( contentTypeHeader, BOUNDARY_PATTERN, null, 2 );
    } else {
      boundary = null;
    }
  }




  public String getBoundary() {
    return boundary;
  }




  public String getContentType() {
    return contentType;
  }




  public String getContentTypeHeader() {
    return contentTypeHeader;
  }




  private String getDetailFromContentHeader( final String contentTypeHeader, final Pattern pattern, final String defaultValue, final int group ) {
    final Matcher matcher = pattern.matcher( contentTypeHeader );
    return matcher.find() ? matcher.group( group ) : defaultValue;
  }




  public String getEncoding() {
    return encoding == null ? ASCII_ENCODING : encoding;
  }




  public boolean isMultipart() {
    return MULTIPART_FORM_DATA_HEADER.equalsIgnoreCase( contentType );
  }




  public ContentType tryUTF8() {
    if ( encoding == null ) {
      return new ContentType( contentTypeHeader + "; charset=UTF-8" );
    }
    return this;
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer( getClass().getSimpleName() );
    b.append( ": " );
    b.append( contentType );
    if ( StringUtil.isNotBlank( encoding ) ) {
      b.append( "; " );
      b.append( encoding );
    }
    b.append( " (" );
    b.append( contentTypeHeader );
    b.append( ")" );
    return b.toString();
  }

}