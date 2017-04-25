package coyote.dx.web.worker;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTP;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.loader.log.Log;


public class JsonRestWorker extends AbstractWorker implements ResourceWorker {

  public JsonRestWorker( final Resource resource ) {
    super( resource );
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalRequestBody( final HttpEntityEnclosingRequestBase request, final Parameters params ) {
    StringBuffer body = new StringBuffer();
    if ( params.getPayload() != null ) {
      body.append( JSONMarshaler.marshal( params.getPayload() ) );
    }

    if ( params.getBody() != null ) {
      body.append( params.getBody() );
    }

    if ( body.length() > 0 ) {
      request.setHeader( HTTP.HDR_CONTENT_TYPE, MimeType.JSON.getType() );
      try {
        request.setEntity( new StringEntity( body.toString() ) );
      } catch ( final UnsupportedEncodingException e ) {
        Log.error( e.getMessage() );
      }
    }
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#setRequestHeaders(org.apache.http.HttpRequest, coyote.dx.web.Parameters)
   */
  @Override
  public void setRequestHeaders( HttpRequest request, Parameters params ) {
    request.setHeader( HTTP.HDR_ACCEPT, "application/json, text/plain, */*" );
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#marshalResponseBody(coyote.dx.web.Response, org.apache.http.HttpResponse, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalResponseBody( final Response workerResponse, final HttpResponse httpResponse, final Parameters params ) {
    try {
      final org.apache.http.entity.ContentType ctype = org.apache.http.entity.ContentType.getOrDefault( httpResponse.getEntity() );
      final String body = EntityUtils.toString( httpResponse.getEntity(), ctype.getCharset() );
      log.debug( this.getClass().getSimpleName() + " marshaling response body of '%s%s", body.substring( 0, body.length() > 500 ? 500 : body.length() ), body.length() <= 500 ? "'" : " ...'" );
      
      workerResponse.setBody(body);
      final List<DataFrame> responseFrames = JSONMarshaler.marshal( body );

      if ( responseFrames.size() > 0 ) {
        workerResponse.setResult( responseFrames.get( 0 ) );
      }
      return;
    } catch ( final Exception e ) {
      Log.error( e.getMessage() );
    }
    super.marshalResponseBody( workerResponse, httpResponse, params );
  }




  @Override
  public void send( final Parameters params ) {}

}
