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
package coyote.dx.web;

import coyote.dx.web.worker.BasicWorker;
import coyote.dx.web.worker.HtmlWorker;
import coyote.dx.web.worker.JsonHttpWorker;
import coyote.dx.web.worker.JsonRestWorker;
import coyote.dx.web.worker.ResourceWorker;
import coyote.dx.web.worker.SoapWorker;
import coyote.dx.web.worker.XmlRestWorker;
import coyote.loader.log.Log;


/**
 * This models a type of exchange and create the type of worker which will 
 * mediate the exchange.
 * 
 * <p>It is expected that several different types of exchanges will be 
 * developed over time and this class allows them to be supported in a modular 
 * manner. Add the name of the exchange pattern and the class which handles it 
 * to this class and different exchanges can be called at runtime.  
 */
public enum ExchangeType {
  SOAP( "SOAP", SoapWorker.class), 
  JSON_REST( "JSON_REST", JsonRestWorker.class), 
  JSON_HTTP( "JSON_HTTP", JsonHttpWorker.class), 
  XML_REST( "XML_REST", XmlRestWorker.class), 
  BASIC( "BASIC", BasicWorker.class), 
  HTML( "HTML", HtmlWorker.class);

  private String name;
  private final Class<?> workerClass;




  private ExchangeType( String type, final Class<?> workerclass ) {
    name = type;
    workerClass = workerclass;
  }




  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return name;
  }




  /**
   * Return the ExchangeType with the given name.
   * 
   * @param name The name of the exchange type to retrieve, not case sensitive.
   * 
   * @return the ExchangeType with the matching name.
   */
  public static ExchangeType getTypeByName( String name ) {
    if ( name != null ) {
      for ( ExchangeType type : ExchangeType.values() ) {
        if ( name.equalsIgnoreCase( type.toString() ) ) {
          return type;
        }
      }
    }
    return null;
  }




  /**
   * @return a worker for this particular exchange type.
   */
  public ResourceWorker getWorker( Resource resource ) {
    try {
      final Object object = workerClass.getDeclaredConstructor( Resource.class ).newInstance( resource );
      if ( object instanceof ResourceWorker ) {
        return (ResourceWorker)object;
      }
      Log.error( "Class '" + workerClass.getName() + "' is not an instance of ResourceWorker, returning null" );
    } catch ( Exception e ) {
      Log.error( "Could not return worker: " + e.getClass().getSimpleName() + " - " + e.getMessage() );
    }
    return null;
  }

}
