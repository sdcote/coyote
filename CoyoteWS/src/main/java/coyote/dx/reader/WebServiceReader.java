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
package coyote.dx.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import coyote.dx.web.InvocationException;
import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameReader;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This is a reader which makes web service call as a client and uses the 
 * results as the data for the transformation.
 * 
 * <p>This means the first read call will detect that there is no data and a 
 * web service request will be performed to create a data set. That set will 
 * be used for that read and all subsequent reads. Data is removed from the 
 * data set on each read and EOF is true when the data set is empty.
 * 
 * <p>Some API support the concept of pagination, only returning a set of X 
 * records even though the result set is larger. For example, a web service 
 * query may result in 100 records being found, but the API will only return 
 * 20 at a time. THis means the same query may have to be executed 5 times, 
 * each with a different offset or page number. These cases are rare, but when 
 * they do occur, it is often easier to subclass this reader and handle 
 * pagination according to the particular API being called. 
 */
public class WebServiceReader extends AbstractFrameReader implements FrameReader, ConfigurableComponent {

  private Resource resource = null;
  private Authenticator authenticator = new NullAuthenticator();
  private String selectorPattern = null;

  private Response lastResponse = null;

  List<DataFrame> dataframes = null;




  /**
   * This is called every time the transform runs.
   * 
   * @see coyote.dx.Component#open(coyote.dx.context.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    setContext( context );

    String resourceUrl = null;
    Proxy proxy = null;

    // Get the resource - this is usually the host as in https://host.com
    DataField dfld = getConfiguration().getFieldIgnoreCase( ConfigTag.SOURCE );
    if ( dfld != null ) {
      resourceUrl = dfld.getStringValue();
    }

    // Get proxy data if defined
    for ( DataField field : getConfiguration().getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.PROXY ) ) {
        if ( field.isFrame() ) {
          try {
            proxy = CWS.configProxy( (DataFrame)field.getObjectValue() );
          } catch ( ConfigurationException e ) {
            Log.fatal( e );
            context.setError( "Could not create proxy: " + e.getMessage() );
            return;
          }
          break;
        } else {
          Log.error( "Invalid proxy configuration, expected a section not a scalar" );
        }
      }
    }

    // Get authenticator, if defined
    for ( DataField field : getConfiguration().getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.AUTHENTICATOR ) ) {
        if ( field.isFrame() ) {
          DataFrame cfg = (DataFrame)field.getObjectValue();
          try {
            authenticator = CWS.configAuthenticator( cfg );
            break;
          } catch ( ConfigurationException e ) {
            Log.fatal( e );
            context.setError( "Could not create authenticator: " + e.getMessage() );
            return;
          }
        } else {
          Log.error( "Invalid authenticator configuration, expected a section not an attribute" );
        }
      }
    }

    // look for a Protocol section
    Parameters protocol = null;
    for ( DataField field : getConfiguration().getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.PROTOCOL ) ) {
        if ( field.isFrame() ) {
          try {
            protocol = CWS.configParameters( (DataFrame)field.getObjectValue() );
          } catch ( ConfigurationException e ) {
            Log.fatal( e );
            context.setError( "Could not configure protocol: " + e.getMessage() );
            return;
          }
          Log.debug( "Found a protocol: " + protocol.toString() );
          break;
        } else {
          context.setError( "Invalid protocol configuration, expected a section not an attribute" );
          context.setStatus( "Configuration Error" );
          return;
        }
      }
    }

    // Get the request body (a.k.a. the payload or message) and place it in 
    // our request parameters
    for ( DataField field : getConfiguration().getFields() ) {
      if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.BODY ) ) {
        if ( field.isFrame() ) {
          DataFrame cfg = (DataFrame)field.getObjectValue();
          protocol.setPayload( cfg );
          break;
        } else {
          protocol.setBody( field.getStringValue() );
        }
      }
    }

    // Get the selector pattern
    selectorPattern = getConfiguration().getAsString( CWS.SELECTOR );

    try {

      // treat the source as a template
      String url = Template.resolve( resourceUrl, getContext().getSymbols() );

      // Create a new web resource
      resource = new Resource( url, protocol, proxy );

      // Set the configured authenticator
      resource.setAuthenticator( authenticator );

      // Now look for a Request Decorator configuration frame and add the 
      // reqest decorators to the new resource 
      for ( DataField field : getConfiguration().getFields() ) {
        if ( field.getName() != null && field.getName().equalsIgnoreCase( CWS.DECORATOR ) ) {
          if ( field.isFrame() ) {
            DataFrame cfgFrame = (DataFrame)field.getObjectValue();
            for ( DataField cfgfield : cfgFrame.getFields() ) {
              if ( cfgfield.isFrame() ) {
                if ( StringUtil.isNotBlank( cfgfield.getName() ) ) {
                  CWS.configDecorator( cfgfield.getName(), (DataFrame)cfgfield.getObjectValue(), resource );
                } else {
                  Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.configuration_must_be_named" ) );
                }
              } else {
                Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.invalid_decorator_configuration_section" ) );
              }
            }
          } else {
            Log.error( LogMsg.createMsg( CWS.MSG, "Decorator.invalid_decorator_configuration_section" ) );
          }
        }
      }

      // Open the resource performing any authentication exchanges
      resource.open();

    } catch ( IOException e ) {
      Log.fatal( e );
      context.setError( "Could not create resource: " + e.getMessage() );
    } catch ( AuthenticationException e ) {
      Log.fatal( e );
      context.setError( "Could not authenticate resource: " + e.getMessage() );
    }

    // The resource should be open and ready for reads
  }




  @Override
  public DataFrame read( TransactionContext context ) {

    if ( dataframes == null ) {
      dataframes = retrieveData();
    }

    if ( dataframes.size() > 0 )
      return dataframes.remove( 0 );

    return null;
  }




  /**
   * Read data from the web service call and populate a list of data frames 
   * representing the retrieved data.
   * 
   * @return a list of retrieved data frames; it may be empty but never null.
   */
  private List<DataFrame> retrieveData() {
    List<DataFrame> retval = new ArrayList<DataFrame>();

    Response response = null;
    try {
      response = resource.request();
    } catch ( InvocationException e ) {
      e.printStackTrace();
    }

    while ( !response.isComplete() ) {
      Thread.yield();
    }

    DataFrame result = response.getResult();

    //TODO apply the selector o the results
    
    retval.add( CWS.flatten( result ) );

    return retval;
  }




  /**
   * If we have not run a query to load data frames or if we have and there
   * are no frames, then we are EOF
   * 
   * @see coyote.dx.FrameReader#eof()
   */
  @Override
  public boolean eof() {
    return ( dataframes != null && dataframes.size() == 0 );
  }




  /**
   * @return the last response generated from the last read operation
   */
  public Response getLastResponse() {
    return lastResponse;
  }

}
