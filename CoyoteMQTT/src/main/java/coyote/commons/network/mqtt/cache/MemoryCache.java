package coyote.commons.network.mqtt.cache;

import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Persistence that uses memory
 * 
 * In cases where reliability is not required across client or device 
 * restarts memory this memory persistence can be used. In cases where
 * reliability is required like when clean session is set to false
 * then a non-volatile form of persistence should be used. 
 * 
 */
public class MemoryCache implements ClientCache {

  private Hashtable data;




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#clear()
   */
  @Override
  public void clear() throws CacheException {
    data.clear();
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#close()
   */
  @Override
  public void close() throws CacheException {
    data.clear();
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#containsKey(java.lang.String)
   */
  @Override
  public boolean containsKey( final String key ) throws CacheException {
    return data.containsKey( key );
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#get(java.lang.String)
   */
  @Override
  public Cacheable get( final String key ) throws CacheException {
    return (Cacheable)data.get( key );
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#keys()
   */
  @Override
  public Enumeration keys() throws CacheException {
    return data.keys();
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#open(java.lang.String, java.lang.String)
   */
  @Override
  public void open( final String clientId, final String serverURI ) throws CacheException {
    data = new Hashtable();
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#put(java.lang.String, coyote.commons.network.mqtt.cache.Cacheable)
   */
  @Override
  public void put( final String key, final Cacheable persistable ) throws CacheException {
    data.put( key, persistable );
  }




  /**
   * @see coyote.commons.network.mqtt.cache.ClientCache#remove(java.lang.String)
   */
  @Override
  public void remove( final String key ) throws CacheException {
    data.remove( key );
  }

}
