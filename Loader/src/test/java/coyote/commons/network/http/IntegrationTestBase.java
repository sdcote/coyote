package coyote.commons.network.http;

import java.io.IOException;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;


public abstract class IntegrationTestBase<T extends HTTPD> {

  protected HttpClient httpclient;
  protected CookieStore cookiestore;

  protected T testServer;




  public abstract T createTestServer();




  @Before
  public void setUp() {
    this.testServer = createTestServer();
    cookiestore = new BasicCookieStore();
    HttpClientContext context = HttpClientContext.create();
    context.setCookieStore( cookiestore );
    this.httpclient = HttpClients.custom().setDefaultRequestConfig( RequestConfig.custom().setCookieSpec( CookieSpecs.STANDARD ).build() ).setDefaultCookieStore( cookiestore ).build();

    try {
      this.testServer.start();
    } catch ( final IOException e ) {
      e.printStackTrace();
    }
  }




  @After
  public void tearDown() {
    this.httpclient.getConnectionManager().shutdown();
    this.testServer.stop();
  }
}
