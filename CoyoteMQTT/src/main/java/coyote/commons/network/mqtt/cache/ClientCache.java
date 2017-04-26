package coyote.commons.network.mqtt.cache;

import java.util.Enumeration;

import coyote.commons.network.mqtt.MqttClient;


/**
 * Represents a persistent data store, used to store outbound and inbound 
 * messages while they are in flight, enabling delivery to the QoS specified. 
 * 
 * <p>You can specify an implementation of this interface using {@code 
 * MqttClientImpl(String, String, ClientCache)},
 * which the {@link MqttClient} will use to persist QoS 1 and 2 messages.</p>
 * 
 * <p>If the methods defined throw the MqttPersistenceException then the state 
 * of the data persisted should remain as prior to the method being called. 
 * For example, if {@link #put(String, Cacheable)} throws an exception at any 
 * point then the data will be assumed to not be in the cache.
 * Similarly if {@link #remove(String)} throws an exception then the data will 
 * be assumed to still be held in the cache.</p>
 * 
 * <p>It is up to the cache implementation to log any exceptions or error 
 * information which may be required when diagnosing a persistence failure.</p>
 */
public interface ClientCache {
  /**
   * Clears persistence, so that it no longer contains any persisted data.
   */
  public void clear() throws CacheException;




  /**
   * Close the cache that was previously opened.
   * 
   * <p>This will be called when a client application disconnects from the 
   * broker.</p>
   * 
   * @throws CacheException 
   */
  public void close() throws CacheException;




  /**
   * Returns whether or not data is persisted using the specified key.
   * 
   * @param key the key for data, which was used when originally saving it.
   */
  public boolean containsKey( String key ) throws CacheException;




  /**
   * Gets the specified data out of the cache.
   * 
   * @param key the key for the data, which was used when originally saving it.
   * 
   * @return the un-persisted data
   * 
   * @throws CacheException if there was a problem getting the data from the 
   *         cache.
   */
  public Cacheable get( String key ) throws CacheException;




  /**
   * Returns an Enumeration over the keys in this persistent data store.
   * 
   * @return an enumeration of {@link String} objects.
   */
  public Enumeration keys() throws CacheException;




  /**
   * Initialize the cache.
   * 
   * <p>If a cache exists for this client ID then open it, otherwise create a 
   * new one. If the cache is already open then just return. An application may 
   * use the same client ID to connect to many different brokers, so the client 
   * ID in conjunction with the connection will uniquely identify the cache
   * required.</p>
   * 
   * @param clientId The client for which the cache should be opened.
   * @param serverURI The connection string as specified when the MQTT client 
   *        instance was created.
   * 
   * @throws CacheException if there was a problem opening the cache.
   */
  public void open( String clientId, String serverURI ) throws CacheException;




  /**
   * Puts the specified data into the cache.
   * 
   * @param key the key for the data, which will be used later to retrieve it.
   * @param persistable the data to persist
   * 
   * @throws CacheException if there was a problem putting the data
   *         into the cache.
   */
  public void put( String key, Cacheable persistable ) throws CacheException;




  /**
   * Remove the data for the specified key.
   */
  public void remove( String key ) throws CacheException;

}
