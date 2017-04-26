package coyote.commons.network.mqtt.utilities;

import java.util.Enumeration;
import java.util.Hashtable;

import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.Cacheable;
import coyote.commons.network.mqtt.cache.ClientCache;


/**
 * Persistence that uses memory
 * 
 * <p>In cases where reliability is not required across client or device 
 * restarts, this memory cache can be used. In cases where reliability is 
 * required like when clean session is set to false then a non-volatile form of 
 * persistence should be used.</p> 
 */
public class TestMemoryCache implements ClientCache {

  private Hashtable data;




  public void close() throws CacheException {
    //data.clear();
  }




  public Enumeration keys() throws CacheException {
    return data.keys();
  }




  public Cacheable get( String key ) throws CacheException {
    return (Cacheable)data.get( key );
  }




  public void open( String clientId, String serverURI ) throws CacheException {
    if ( this.data == null ) {
      this.data = new Hashtable();
    }
  }




  public void put( String key, Cacheable persistable ) throws CacheException {
    data.put( key, persistable );
  }




  public void remove( String key ) throws CacheException {
    data.remove( key );
  }




  public void clear() throws CacheException {
    data.clear();
  }




  public boolean containsKey( String key ) throws CacheException {
    return data.containsKey( key );
  }

}
