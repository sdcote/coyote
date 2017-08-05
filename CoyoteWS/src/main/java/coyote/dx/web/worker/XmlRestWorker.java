package coyote.dx.web.worker;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTP;
import coyote.dataframe.DataFrame;
import coyote.dataframe.marshal.XMLMarshaler;
import coyote.dx.web.Parameters;
import coyote.dx.web.Resource;
import coyote.dx.web.Response;
import coyote.loader.log.Log;


public class XmlRestWorker extends AbstractWorker implements ResourceWorker {

  public XmlRestWorker( final Resource resource ) {
    super( resource );
  }




  /**
   * @see coyote.dx.web.worker.AbstractWorker#marshalRequestBody(org.apache.http.client.methods.HttpEntityEnclosingRequestBase, coyote.dx.web.Parameters)
   */
  @Override
  public void marshalRequestBody( final HttpEntityEnclosingRequestBase request, final Parameters params ) {
    if ( params.getPayload() != null ) {
      request.setHeader( HTTP.HDR_CONTENT_TYPE, MimeType.XML.getType() );
      final String requestBody = XMLMarshaler.marshal( params.getPayload() );
      try {
        request.setEntity( new StringEntity( requestBody ) );
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
    request.setHeader( HTTP.HDR_ACCEPT, "application/xml, text/plain, */*" );
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

      final List<DataFrame> responseFrames = XMLMarshaler.marshal( body );
      if ( responseFrames.size() > 0 ) {
        workerResponse.setResult( responseFrames.get( 0 ) );
      }
      return;
    } catch ( final Exception e ) {
      Log.error( e.getMessage() );
    }
    super.marshalResponseBody( workerResponse, httpResponse, params );
  }

}
