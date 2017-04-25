/*
 * Copyright (c) 2016 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and initial implementation
 */
package coyote.dx.writer;

import java.io.IOException;
import java.net.URISyntaxException;

import coyote.commons.StringUtil;
import coyote.commons.template.Template;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.AbstractConfigurableComponent;
import coyote.dx.CDX;
import coyote.dx.CWS;
import coyote.dx.ConfigTag;
import coyote.dx.ConfigurableComponent;
import coyote.dx.FrameWriter;
import coyote.dx.TransformContext;
import coyote.dx.eval.Evaluator;
import coyote.dx.web.InvocationException;
import coyote.dx.web.Parameters;
import coyote.dx.web.Proxy;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.dx.web.auth.AuthenticationException;
import coyote.dx.web.auth.Authenticator;
import coyote.dx.web.auth.NullAuthenticator;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;


/**
 * This component simply PUTs or POSTs the data frame to a particular service endpoint.
 */
public class WebServiceWriter extends AbstractConfigurableComponent implements FrameWriter, ConfigurableComponent {
  private Evaluator evaluator = new Evaluator();
  private String expression = null;
  private String servicePath = null;
  private int rowCounter = 0;
  private TransformContext context = null;
  private DataFrame lastRequest = null;

  private Response lastResponse = null;

  private Resource resource = null;
  private Authenticator authenticator = new NullAuthenticator();
  private Proxy proxy = null;
  private Parameters parameters = null;




  /**
   * @see coyote.dx.AbstractConfigurableComponent#setConfiguration(coyote.dataframe.DataFrame)
   */
  @Override
  public void setConfiguration( DataFrame frame ) throws ConfigurationException {
    super.setConfiguration( frame );

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Look for a conditional statement the writer may use to control if it is 
    // to write the record or not
    expression = findString( ConfigTag.CONDITION );
    if ( StringUtil.isNotBlank( expression ) ) {
      expression = expression.trim();

      try {
        evaluator.evaluateBoolean( expression );
      } catch ( final IllegalArgumentException e ) {
        context.setError( "Invalid boolean expression in writer: " + e.getMessage() );
      }
    }

    // look for a path
    servicePath = findString( ConfigTag.PATH );
  }




  /**
   * @see coyote.dx.Component#open(coyote.dx.TransformContext)
   */
  @Override
  public void open( TransformContext context ) {
    setContext( context );

    // get the resource from the context, and if it does not exist, create one 
    // for subsequent use.
    if ( resource == null ) {

      // If we don't have an existing instance, create our own
      if ( resource == null ) {
        String targetUrl = getString( ConfigTag.TARGET );
        Log.debug( LogMsg.createMsg( CDX.MSG, "Writer.using_target", this.getClass().getSimpleName(), targetUrl ) );

        if ( StringUtil.isBlank( targetUrl ) ) {
          context.setError( "The Writer configuration did not contain the '" + ConfigTag.TARGET + "' element" );
          context.setStatus( "Configuration Error" );
          return;
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

        // Look for the proxy section
        for ( DataField field : getConfiguration().getFields() ) {
          if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.PROXY ) ) {
            if ( field.isFrame() ) {
              try {
                proxy = CWS.configProxy( (DataFrame)field.getObjectValue() );
              } catch ( ConfigurationException e ) {
                Log.fatal( e );
                context.setError( "Could not configure proxy: " + e.getMessage() );
                return;
              }
              Log.debug( "Found a proxy: " + proxy.toString() );
              break;
            } else {
              context.setError( "Invalid proxy configuration, expected a section not a scalar" );
              context.setStatus( "Configuration Error" );
              return;
            }
          }
        }

        // look for a Protocol section
        for ( DataField field : getConfiguration().getFields() ) {
          if ( ( field.getName() != null ) && field.getName().equalsIgnoreCase( CWS.PROTOCOL ) ) {
            if ( field.isFrame() ) {
              try {
                parameters = CWS.configParameters( (DataFrame)field.getObjectValue() );
              } catch ( ConfigurationException e ) {
                Log.fatal( e );
                context.setError( "Could not configure protocol: " + e.getMessage() );
                return;
              }
              Log.debug( "Found a protocol: " + parameters.toString() );
              break;
            } else {
              context.setError( "Invalid protocol configuration, expected a section not an attribute" );
              context.setStatus( "Configuration Error" );
              return;
            }
          }
        }
        try {
          resource = new Resource( targetUrl, parameters, proxy );

          resource.setAuthenticator( authenticator );

          // Now look for Request Decorators 
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

          resource.open();

        } catch ( IOException e ) {
          context.setError( "The Writer could not connect the resource: " + e.getMessage() );
          context.setStatus( "Connection Error" );
          return;
        } catch ( AuthenticationException e ) {
          context.setError( "The Writer could not authenticate the resource: " + e.getMessage() );
          context.setStatus( "Authentication Error" );
        }
      } else {
        Log.debug( "Using resource set in the transform context" );
      }
    } else {
      Log.debug( "Using existing resource" );
    }

    Log.debug( LogMsg.createMsg( CWS.MSG, "Writer.init_complete", resource ) );
  }




  /**
   * @see coyote.dx.FrameWriter#write(coyote.dataframe.DataFrame)
   */
  @Override
  public void write( DataFrame frame ) {
    // If there is a conditional expression
    if ( expression != null ) {

      try {
        // if the condition evaluates to true
        if ( evaluator.evaluateBoolean( expression ) ) {
          Log.debug( "Condition is true...writing frame to resource" );
          writeFrame( frame );
        } else {
          if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
            Log.debug( "Expression evaluated to false - frame not written" );
          }
        }
      } catch ( final IllegalArgumentException e ) {
        Log.warn( LogMsg.createMsg( CWS.MSG, "Writer.boolean_evaluation_error", e.getMessage() ) );
      }
    } else {
      Log.debug( "Unconditionally writing frame" );
      writeFrame( frame );
    }
  }




  /**
   * This is where we actually write a frame to the web service endpoint
   * 
   * @param frame the frame to write
   * 
   * @return the number of bytes written
   */
  private int writeFrame( DataFrame frame ) {
    int bytesWritten = 0;
    lastRequest = frame;

    Log.info( frame.toString() );
    // Set up an object to hold our request parameters these will over-ride 
    // the default protocol parameters
    Parameters params = new Parameters();

    // Place the request payload in the request parameters
    params.setPayload( frame );

    // Treat the resource URI as a template, substituting variables in the URI 
    // (e.g. ReST identifiers in the path) for data in the transaction context
    // which may heve been changed by listeners or other components to ensure 
    // the data was written to the correct ReSTful resource URI.
    // This sets the path portion of the resource using a template
    if ( StringUtil.isNotBlank( servicePath ) ) {
      try {
        resource.setPath( Template.resolve( servicePath, getContext().getTransaction().getSymbols() ) );
      } catch ( URISyntaxException e ) {
        context.setError( "The Writer could not generate URI path: " + e.getMessage() );
        context.setStatus( "Resource Path Error" );
        return bytesWritten;
      }
    }
    try {
      // invoke the operation and receive an object representing our results
      lastResponse = resource.request( params );

      // wait for results (invocation may be asynchronous)
      while ( !lastResponse.isComplete() ) {
        if ( lastResponse.isTimedOut() ) {
          // nothing happened
          System.err.println( "Operation timed-out" );
          System.exit( 1 );
        } else if ( lastResponse.isInError() ) {
          // we received one or more errors
          System.err.println( "Operation failed" );
          System.exit( 2 );
        } else {
          // wait for the results to arrive
          lastResponse.wait( 100 );
        }
      }

      if ( Log.isLogging( Log.DEBUG_EVENTS ) ) {
        Log.debug( "Performance Metric: Write " + lastResponse.getOperationTime() );
        Log.debug( "Performance Metric: Transaction " + lastResponse.getTransactionTime() );
        Log.debug( "Performance Metric: WebResponse " + lastResponse.getRequestTime() );
        Log.debug( "Performance Metric: Parsing " + lastResponse.getParsingTime() );
      }

      rowCounter++;
    } catch ( InvocationException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch ( InterruptedException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return bytesWritten;
  }




  /**
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() throws IOException {
    // close our stuff first
    Log.debug( LogMsg.createMsg( CWS.MSG, "Writer.records_processed", rowCounter, ( context != null ) ? context.getRow() : 0 ) );

    if ( resource != null ) {
      try {
        resource.close();
      } catch ( Exception e ) {
        Log.warn( LogMsg.createMsg( CWS.MSG, "Writer.close_error", e.getLocalizedMessage() ) );
      }
    }

  }




  /**
   * @return the last response generated from the last write operation
   */
  public Response getLastResponse() {
    return lastResponse;
  }




  /**
   * @return the last Request payload
   */
  public DataFrame getLastRequest() {
    return lastRequest;
  }

}
