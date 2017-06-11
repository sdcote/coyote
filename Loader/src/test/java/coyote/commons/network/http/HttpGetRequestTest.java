package coyote.commons.network.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Test;


public class HttpGetRequestTest extends HttpServerTest {

  @Test
  public void testDecodingFieldWithEmptyValueAndFieldWithMissingValueGiveDifferentResults() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo&bar= HTTP/1.1" );
    assertTrue( testServer.decodedParamters.get( "foo" ) instanceof List );
    assertEquals( 0, testServer.decodedParamters.get( "foo" ).size() );
    assertTrue( testServer.decodedParamters.get( "bar" ) instanceof List );
    assertEquals( 1, testServer.decodedParamters.get( "bar" ).size() );
    assertEquals( "", testServer.decodedParamters.get( "bar" ).get( 0 ) );
  }




  @Test
  public void testDecodingMixtureOfParameters() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar&foo=baz&zot&zim= HTTP/1.1" );
    assertTrue( testServer.decodedParamters.get( "foo" ) instanceof List );
    assertEquals( 2, testServer.decodedParamters.get( "foo" ).size() );
    assertEquals( "bar", testServer.decodedParamters.get( "foo" ).get( 0 ) );
    assertEquals( "baz", testServer.decodedParamters.get( "foo" ).get( 1 ) );
    assertTrue( testServer.decodedParamters.get( "zot" ) instanceof List );
    assertEquals( 0, testServer.decodedParamters.get( "zot" ).size() );
    assertTrue( testServer.decodedParamters.get( "zim" ) instanceof List );
    assertEquals( 1, testServer.decodedParamters.get( "zim" ).size() );
    assertEquals( "", testServer.decodedParamters.get( "zim" ).get( 0 ) );
  }




  @Test
  public void testDecodingParametersFromParameterMap() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar&foo=baz&zot&zim= HTTP/1.1" );
    assertEquals( testServer.decodedParamters, testServer.decodedParamtersFromParameter );
  }



  @Test
  public void testDecodingParametersWithSingleValue() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar&baz=zot HTTP/1.1" );
    assertEquals( "foo=bar&baz=zot", testServer.queryParameterString );
    assertTrue( testServer.decodedParamters.get( "foo" ) instanceof List );
    assertEquals( 1, testServer.decodedParamters.get( "foo" ).size() );
    assertEquals( "bar", testServer.decodedParamters.get( "foo" ).get( 0 ) );
    assertTrue( testServer.decodedParamters.get( "baz" ) instanceof List );
    assertEquals( 1, testServer.decodedParamters.get( "baz" ).size() );
    assertEquals( "zot", testServer.decodedParamters.get( "baz" ).get( 0 ) );
  }




  @Test
  public void testDecodingParametersWithSingleValueAndMissingValue() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo&baz=zot HTTP/1.1" );
    assertEquals( "foo&baz=zot", testServer.queryParameterString );
    assertTrue( testServer.decodedParamters.get( "foo" ) instanceof List );
    assertEquals( 0, testServer.decodedParamters.get( "foo" ).size() );
    assertTrue( testServer.decodedParamters.get( "baz" ) instanceof List );
    assertEquals( 1, testServer.decodedParamters.get( "baz" ).size() );
    assertEquals( "zot", testServer.decodedParamters.get( "baz" ).get( 0 ) );
  }




  @Test
  public void testDecodingSingleFieldRepeated() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar&foo=baz HTTP/1.1" );
    assertTrue( testServer.decodedParamters.get( "foo" ) instanceof List );
    assertEquals( 2, testServer.decodedParamters.get( "foo" ).size() );
    assertEquals( "bar", testServer.decodedParamters.get( "foo" ).get( 0 ) );
    assertEquals( "baz", testServer.decodedParamters.get( "foo" ).get( 1 ) );
  }




  @Test
  public void testEmptyHeadersSuppliedToServeMethodFromSimpleWorkingGetRequest() {
    invokeServer( "GET " + HttpServerTest.URI + " HTTP/1.1" );
    assertNotNull( testServer.parms );
    assertNotNull( testServer.header );
    assertNotNull( testServer.body );
    assertNotNull( testServer.uri );
  }




  @Test
  public void testFullyQualifiedWorkingGetRequest() throws Exception {
    final ByteArrayOutputStream outputStream = invokeServer( "GET " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 0", "" };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testGetQueryParameterContainsAmpersand() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar%26 HTTP/1.1" );
    assertEquals( "Parameter count in URL and decodedParameters should match.", 1, testServer.decodedParamters.size() );
    assertEquals( "The query parameter value with ampersand decoding incorrect", "bar&", testServer.decodedParamters.get( "foo" ).get( 0 ) );
  }




  @Test
  public void testGetQueryParameterContainsQuestionMark() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar%3F HTTP/1.1" );
    assertEquals( "Parameter count in URL and decodedParameters should match.", 1, testServer.decodedParamters.size() );
    assertEquals( "The query parameter value with question mark decoding incorrect", "bar?", testServer.decodedParamters.get( "foo" ).get( 0 ) );
  }




  @Test
  public void testGetQueryParameterContainsSpace() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar%20baz HTTP/1.1" );
    assertEquals( "Parameter count in URL and decodedParameters should match.", 1, testServer.decodedParamters.size() );
    assertEquals( "The query parameter value with space decoding incorrect", "bar baz", testServer.decodedParamters.get( "foo" ).get( 0 ) );
  }




  @Test
  public void testGetQueryParameterContainsSpecialCharactersSingleFieldRepeated() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar%20baz&foo=bar%3F&foo=bar%26 HTTP/1.1" );
    assertEquals( "Parameter count in URL and decodedParameters should match.", 1, testServer.decodedParamters.size() );
    final String[] parametersAsArray = testServer.decodedParamters.get( "foo" ).toArray( new String[0] );
    final String[] expected = new String[] { "bar baz", "bar?", "bar&" };
    assertArrayEquals( "Repeated parameter not decoded correctly", expected, parametersAsArray );
  }




  @Test
  public void testMultipleGetParameters() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar&baz=zot HTTP/1.1" );
    assertEquals( "bar", testServer.parms.get( "foo" ) );
    assertEquals( "zot", testServer.parms.get( "baz" ) );
  }




  @Test
  public void testMultipleGetParametersWithMissingValue() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=&baz=zot HTTP/1.1" );
    assertEquals( "", testServer.parms.get( "foo" ) );
    assertEquals( "zot", testServer.parms.get( "baz" ) );
  }




  @Test
  public void testMultipleGetParametersWithMissingValueAndRequestHeaders() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=&baz=zot HTTP/1.1\nAccept: text/html" );
    assertEquals( "", testServer.parms.get( "foo" ) );
    assertEquals( "zot", testServer.parms.get( "baz" ) );
    assertEquals( "text/html", testServer.header.get( "accept" ) );
  }




  @Test
  public void testMultipleHeaderSuppliedToServeMethodFromSimpleWorkingGetRequest() {
    final String userAgent = "jUnit 4.8.2 Unit Test";
    final String accept = "text/html";
    invokeServer( "GET " + HttpServerTest.URI + " HTTP/1.1\nUser-Agent: " + userAgent + "\nAccept: " + accept );
    assertEquals( userAgent, testServer.header.get( "user-agent" ) );
    assertEquals( accept, testServer.header.get( "accept" ) );
  }




  @Test
  public void testOutputOfServeSentBackToClient() throws Exception {
    final String responseBody = "Success!";
    testServer.response = Response.createFixedLengthResponse( responseBody );
    final ByteArrayOutputStream outputStream = invokeServer( "GET " + HttpServerTest.URI + " HTTP/1.1" );
    final String[] expected = { "HTTP/1.1 200 OK", "Content-Type: text/html", "Date: .*", "Connection: keep-alive", "Content-Length: 8", "", responseBody };
    assertResponse( outputStream, expected );
  }




  @Test
  public void testSingleGetParameter() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo=bar HTTP/1.1" );
    assertEquals( "bar", testServer.parms.get( "foo" ) );
  }




  @Test
  public void testSingleGetParameterWithNoValue() {
    invokeServer( "GET " + HttpServerTest.URI + "?foo HTTP/1.1" );
    assertEquals( "", testServer.parms.get( "foo" ) );
  }




  @Test
  public void testSingleUserAgentHeaderSuppliedToServeMethodFromSimpleWorkingGetRequest() {
    final String userAgent = "jUnit 4.8.2 Unit Test";
    invokeServer( "GET " + HttpServerTest.URI + " HTTP/1.1\nUser-Agent: " + userAgent + "\n" );
    assertEquals( userAgent, testServer.header.get( "user-agent" ) );
    assertEquals( Method.GET, testServer.method );
    assertEquals( HttpServerTest.URI, testServer.uri );
  }

}
