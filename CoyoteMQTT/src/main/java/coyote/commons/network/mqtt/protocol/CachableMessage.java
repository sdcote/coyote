package coyote.commons.network.mqtt.protocol;

import coyote.commons.network.mqtt.MqttException;
import coyote.commons.network.mqtt.cache.CacheException;
import coyote.commons.network.mqtt.cache.Cacheable;


public abstract class CachableMessage extends AbstractMessage implements Cacheable {

  public CachableMessage( final byte type ) {
    super( type );
  }




  @Override
  public byte[] getHeaderBytes() throws CacheException {
    try {
      return getHeader();
    } catch ( final MqttException ex ) {
      throw new CacheException( ex.getCause() );
    }
  }




  @Override
  public int getHeaderLength() throws CacheException {
    return getHeaderBytes().length;
  }




  @Override
  public int getHeaderOffset() throws CacheException {
    return 0;
  }




  @Override
  public byte[] getPayloadBytes() throws CacheException {
    try {
      return getPayload();
    } catch ( final MqttException ex ) {
      throw new CacheException( ex.getCause() );
    }
  }




  @Override
  public int getPayloadLength() throws CacheException {
    return 0;
  }




  @Override
  public int getPayloadOffset() throws CacheException {
    return 0;
  }

}
