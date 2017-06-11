/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.dataframe.marshal.xml;

import java.util.HashMap;


/**
 * 
 */
public class Tag {
  private String name = null;
  private String namespace = null;
  private boolean closetag = false;
  private boolean emptytag = false;
  private boolean commentFlag = false;
  private HashMap<String, String> attributes = new HashMap<String, String>();
  private boolean preambleFlag = false;




  // tags must be parsed
  Tag() {}




  /**
   * @param token
   */
  public Tag( String token ) {

    // Empty tag check
    if ( token.endsWith( "/" ) ) {
      setEmptyTag( true );
      token = token.substring( 0, token.length() - 1 );
    }

    // end tag check
    if ( token.startsWith( "/" ) ) {
      setEndTag( true );
      token = token.substring( 1 );
    }

    // Preamble identification
    if ( token.startsWith( "?" ) ) {
      setPreamble( true );
      token = token.substring( 1 );

      if ( token.endsWith( "?" ) ) {
        token = token.substring( 0, token.length() - 1 );
      }
    } else {
      setPreamble( false );
    }

    // Comment check
    if ( token.startsWith( "!--" ) ) {
      setComment( true );
      token = token.substring( 3 );
    }
    if ( token.endsWith( "--" ) ) {
      token = token.substring( 0, token.length() - 2 );
    }

    // See if there are attributes
    if ( token.indexOf( ' ' ) > -1 ) {
      setName( token.substring( 0, token.indexOf( ' ' ) ) );
      processAttributes( token.substring( token.indexOf( ' ' ) ) );
    } else {
      setName( token );
    }

    // split the name into namespace and name
    if ( name.indexOf( ':' ) > -1 ) {
      setNamespace( name.substring( 0, token.indexOf( ':' ) ) );
      setName( name.substring( token.indexOf( ':' ) + 1 ) );
    } else {
      setName( name );
    }

  }




  /**
   * @param substring
   */
  private void processAttributes( String substring ) {

    // TODO now look for attributes
    // scan for '=' everything up to the '=' is the name
    // scan for quoted string or the next ' '

  }




  /**
   * @param flag
   */
  private void setComment( boolean flag ) {
    commentFlag = flag;
  }




  /**
   * @param flag
   */
  private void setPreamble( boolean flag ) {
    preambleFlag = flag;
  }




  /**
   * @return the name
   */
  public String getName() {
    return name;
  }




  public boolean isCloseTag() {
    return closetag;
  }




  public boolean isOpenTag() {
    return !closetag;
  }




  /**
   * @param name the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }




  /**
   * @return the namespace
   */
  public String getNamespace() {
    return namespace;
  }




  /**
   * @param namespace the namespace to set
   */
  public void setNamespace( String namespace ) {
    this.namespace = namespace;
  }




  /**
   * @param flag true to represent the tag as a terminator
   */
  public void setEndTag( boolean flag ) {
    closetag = flag;
  }




  /**
   * @return true if there is a value following this tag, false if it is empty
   */
  public boolean isEmptyTag() {
    return emptytag;
  }




  public boolean isNotEmptyTag() {
    return !emptytag;
  }




  /**
   * @param flag true, the tag is empty, false, the tag is followed by a value 
   *        and an end tag.
   */
  public void setEmptyTag( boolean flag ) {
    emptytag = flag;
  }




  /**
   * @return true if the tag represents a comment, false otherwise.
   */
  public boolean isComment() {
    return commentFlag;
  }




  /**
   * @return true if the tag represents a preamble or processing instruction, 
   *         false otherwise.
   */
  public boolean isPreamble() {
    return preambleFlag;

  }




  /**
   * Return the attribute with the given (case sensitive) name.
   * 
   * @param name The name of the attribute to retrieve.
   * 
   * @return the value of the attribute or null if the attribute with the given name was not found.
   */
  public String getAttribute( String name ) {
    return attributes.get( name );
  }

}
