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
package coyote.dx.web.worker;

/**
 *
 */
public class Namespace {
  private String prefix = null;
  private String url = null;




  public Namespace( final String prefix, final String url ) {
    if ( prefix == null ) {
      throw new IllegalArgumentException( "Namespace prefix cannot be null" );
    }
    if ( url == null ) {
      throw new IllegalArgumentException( "Namespace URL cannot be null" );
    }

    setPrefix( prefix );

    setUrl( url.endsWith( "/" ) ? url : url + "/" );
  }




  /**
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }




  /**
   * @return the url
   */
  public String getUrl() {
    return url;
  }




  /**
   * @param prefix the prefix to set
   */
  public void setPrefix( final String prefix ) {
    this.prefix = prefix;
  }




  /**
   * @param url the url to set
   */
  public void setUrl( final String url ) {
    this.url = url;
  }

}
