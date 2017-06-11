/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.network.http;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import coyote.commons.FileUtil;
import coyote.loader.log.Log;


/**
 * Represents a collection of entities in a body of an http message.
 */
public class Body {
  public static final String CONTENT = "content";
  private final Map<String, Object> entities = new HashMap<String, Object>();
  private final Map<String, ContentType> entityTypes = new HashMap<String, ContentType>();

  public static Charset DEFAULT_CHARSET = Charset.forName( "UTF-8" );
  public static CharsetDecoder DEFAULT_DECODER = DEFAULT_CHARSET.newDecoder();




  /**
   * Associates the specified value with the specified key in this body.
   * 
   * <p>If the body previously contained a mapping for the key, the old value 
   * is replaced by the specified value
   * 
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @param type optional content type of the value if known
   * 
   * @return the reference to this body to enable invocation chaining
   */
  public Body put( String key, ByteBuffer value, ContentType type ) {
    entities.put( key, value );
    if ( type != null ) {
      entityTypes.put( key, type );
    }
    return this;
  }




  /**
   * Associates the specified value with the specified key in this body.
   * 
   * <p>If the body previously contained a mapping for the key, the old value 
   * is replaced by the specified value
   * 
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @param type optional contnent type of the value if known
   * 
   * @return the reference to this body to enable invocation chaining
   */
  public Body put( String key, File value, ContentType type ) {
    entities.put( key, value );
    if ( type != null ) {
      entityTypes.put( key, type );
    }
    return this;
  }




  /**
   * Associates the specified value with the specified key in this body.
   * 
   * <p>If the body previously contained a mapping for the key, the old value 
   * is replaced by the specified value
   * 
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * 
   * @return the reference to this body to enable invocation chaining
   */
  public Body put( String key, String value ) {
    entities.put( key, value );
    return this;
  }




  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified 
   * key.  
   * 
   * <p>More formally, returns <tt>true</tt> if and only if this body map 
   * contains a mapping for a key <tt>k</tt> such that <tt>(key==null ? k==null 
   * : key.equals(k))</tt>.  (There can be at most one such mapping.)
   *
   * @param key key whose presence in this map is to be tested
   * @return <tt>true</tt> if this map contains a mapping for the specified key
   */
  public boolean containsKey( String key ) {
    return entities.containsKey( key );
  }




  /**
   * Returns a {@link Set} view of the keys for entities contained in this 
   * body.
   * 
   * @return a set view of the entity keys contained in this body   
   */
  public Set<String> keySet() {
    return entities.keySet();
  }




  /**
   * Returns the number of key-value mappings in this body.  
   * 
   * <p>If the body contains more than <tt>Integer.MAX_VALUE</tt> elements, 
   * returns <tt>Integer.MAX_VALUE</tt>.
   * 
   * @return the number of key-value mappings in this body
   */
  public int size() {
    return entities.size();
  }




  /**
   * Returns the value to which the specified key is mapped, or {@code null} 
   * if this map contains no mapping for the key.
   * 
   * @param key the key whose associated value is to be returned
   * 
   * @return the value to which the specified key is mapped, or {@code null} 
   *         if this body contains no mapping for the key
   */
  public Object get( String key ) {
    return entities.get( key );
  }




  /**
   * Returns a {@link Set} view of the entity mappings contained in this body.
   * 
   * @return a set view of the entity mappings contained in this body
   */
  public Set<Map.Entry<String, Object>> entrySet() {
    return entities.entrySet();
  }




  /**
   * Return the named entity as a string.
   * 
   * <p><strong>NOTE:</strong> entities are stored as the raw bytes read from 
   * the request body and this method attempts to determine the encodeing 
   * based on the content type present when the body was parsed. If no content 
   * type was present, did not contain an encoding or contained an unsupported 
   * character encoding, this method will default to the UTF-8 character set.
   *   
   * @param key the name of the entity to retrieve (e.g."content")
   * 
   * @return a UTF-8 representation of the bytes for that entity or null if 
   *         the entity was not found.
   */
  public String getAsString( String key ) {
    String retval = null;
    Object obj = entities.get( key );
    if ( obj != null ) {
      if ( obj instanceof ByteBuffer ) {
        ByteBuffer bb = (ByteBuffer)obj;
        CharsetDecoder decoder = getDecoder( key );
        try {
          retval = decoder.decode( bb ).toString();
          bb.position( 0 );
        } catch ( CharacterCodingException e ) {
          e.printStackTrace();
        }
      } else if ( obj instanceof File ) {
        CharsetDecoder decoder = getDecoder( key );
        try {
          final byte[] data = FileUtil.read( (File)obj );
          retval = decoder.decode( ByteBuffer.wrap( data ) ).toString();
        } catch ( IOException e ) {
          Log.error( "Could not read cached entity:" + e.getMessage() + " for " + ( (File)obj ).getAbsolutePath() );
        }
      } else {
        retval = obj.toString();
      }
    }
    return retval;
  }




  private CharsetDecoder getDecoder( String key ) {
    CharsetDecoder retval = null;
    ContentType type = entityTypes.get( key );
    if ( type != null && type.getEncoding() != null ) {
      Charset charset = null;
      try {
        charset = Charset.forName( type.getEncoding() );
      } catch ( Exception e ) {
        try {
          charset = Charset.forName( type.getEncoding().toUpperCase() );
        } catch ( Exception ex ) {}
      }

      if ( charset != null ) {
        retval = charset.newDecoder();
      }
    }

    if ( retval == null ) {
      retval = DEFAULT_DECODER;
    }
    return retval;
  }




  /**
   * Return the entity type provided in the request for this entity.
   * 
   * @param entityKey the entity to query
   * 
   * @return the content type provided in the request message or null if no 
   *         content type was provided or the entity was not found;
   */
  public ContentType getEntityType( String entityKey ) {
    return entityTypes.get( entityKey );
  }




  /**
   * PUT operations will often have a body and if so, it will be accessible 
   * via this method.
   * 
   * @return String representation of the PUT content request body or null if 
   *         it does not exist.
   */
  public String getContent() {
    return getAsString( CONTENT );
  }

}
