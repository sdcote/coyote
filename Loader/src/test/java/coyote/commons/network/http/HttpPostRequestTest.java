package coyote.commons.network.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;


public class HttpPostRequestTest extends HttpServerTest {

  public static final String CONTENT_LENGTH = "Content-Length: ";
  public static final String FIELD = "caption";
  public static final String VALUE = "Summer vacation";
  public static final String TYPE = "image/jpeg";
  public static final String FIELD2 = "location";
  public static final String VALUE2 = "Grand Canyon";
  public static final String POST_RAW_CONTENT_FILE_ENTRY = "postData";
  public static final String VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPERSAND = "Test raw data & Result value";




  /**
   * contains common preparation steps for testing POST with Multipart Form
   *
   * @param fileName Name of file to be uploaded
   * @param fileContent Content of file to be uploaded
   * 
   * @return input String with POST request complete information including 
   *         header, length and content
   */
  private String preparePostWithMultipartForm( final String fileName, final String fileContent ) {
    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data, boundary=" + divider + "\r\n";
    final String content = "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"; filename=\"" + fileName + "\"\r\n" + "Content-Type: " + HttpPostRequestTest.TYPE + "\r\n" + "\r\n" + fileContent + "\r\n" + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 5 ) + "\r\n\r\n" + content;
    return input;
  }




  @Test
  public void testPostWithMultipartFormFieldsAndFile() throws IOException {
    final String fileName = "GrandCanyon.txt";
    final String fileContent = HttpPostRequestTest.VALUE;

    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\n";
    final String content = "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"; filename=\"" + fileName + "\"\r\n" + "Content-Type: " + HttpPostRequestTest.TYPE + "\r\n" + "\r\n" + fileContent + "\r\n" + "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE2 + "\r\n" + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( "Parameter count did not match.", 2, testServer.parms.size() );
    assertEquals( "Parameter value did not match", HttpPostRequestTest.VALUE2, testServer.parms.get( HttpPostRequestTest.FIELD2 ) );
    ContentType type = testServer.body.getEntityType( HttpPostRequestTest.FIELD );
    assertEquals( "Content Type did not match", type.getContentType(), HttpPostRequestTest.TYPE );
    String strdata = testServer.body.getAsString( HttpPostRequestTest.FIELD );
    final List<String> lines = Arrays.asList( strdata.split( "\\n" ) );
    assertLinesOfText( new String[] { fileContent }, lines );
  }




  @Test
  public void testPostWithMultipartFormUpload() throws Exception {
    final String filename = "GrandCanyon.txt";
    final String fileContent = HttpPostRequestTest.VALUE;
    final String input = preparePostWithMultipartForm( filename, fileContent );
    invokeServer( input );
    assertEquals( 1, testServer.parms.size() );
    String strdata = testServer.body.getAsString( HttpPostRequestTest.FIELD );
    final List<String> lines = Arrays.asList( strdata.split( "\\n" ) );
    assertLinesOfText( new String[] { fileContent }, lines );
  }




  @Test
  public void testPostWithMultipartFormUploadFilenameHasSpaces() throws Exception {
    final String fileNameWithSpace = "Grand Canyon.txt";
    final String fileContent = HttpPostRequestTest.VALUE;
    final String input = preparePostWithMultipartForm( fileNameWithSpace, fileContent );
    invokeServer( input );
    final String fileNameAfter = new ArrayList<String>( testServer.parms.values() ).get( 0 );
    assertEquals( fileNameWithSpace, fileNameAfter );
  }




  @Test
  public void testPostWithMultipartFormUploadFileWithMultilineContent() throws Exception {
    final String filename = "GrandCanyon.txt";
    final String lineSeparator = "\n";
    final String fileContent = HttpPostRequestTest.VALUE + lineSeparator + HttpPostRequestTest.VALUE + lineSeparator + HttpPostRequestTest.VALUE;
    final String input = preparePostWithMultipartForm( filename, fileContent );
    invokeServer( input );
    assertEquals( "Parameter count did not match.", 1, testServer.parms.size() );
    String strdata = testServer.body.getAsString( HttpPostRequestTest.FIELD );
    final List<String> lines = Arrays.asList( strdata.split( "\\n" ) );
    assertLinesOfText( fileContent.split( lineSeparator ), lines );
  }




  @Test
  public void testPostWithMultipartFormUploadMultipleFiles() throws IOException {
    final String fileName = "GrandCanyon.txt";
    final String fileContent = HttpPostRequestTest.VALUE;
    final String file2Name = "AnotherPhoto.txt";
    final String file2Content = HttpPostRequestTest.VALUE2;
    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\n";
    final String content = "--" + divider + "\r\n"//
        + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"; filename=\"" + fileName + "\"\r\n" //
        + "Content-Type: image/jpeg\r\n" + "\r\n" //
        + fileContent + "\r\n" //
        + "--" + divider + "\r\n" //
        + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"; filename=\"" + file2Name + "\"\r\n" //
        + "Content-Type: image/jpeg\r\n" + "\r\n" //
        + file2Content + "\r\n" //
        + "\r\n" //
        + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( "Parameter count did not match.", 2, testServer.parms.size() );
    String strdata = testServer.body.getAsString( HttpPostRequestTest.FIELD );
    List<String> lines = Arrays.asList( strdata.split( "\\n" ) );
    assertLinesOfText( new String[] { fileContent }, lines );
    String fileName2 = testServer.body.getAsString( HttpPostRequestTest.FIELD2 );
    int testNumber = 0;
    while ( ( fileName2 == null ) && ( testNumber < 5 ) ) {
      testNumber++;
      fileName2 = testServer.body.getAsString( HttpPostRequestTest.FIELD2 + testNumber );
    }
    lines = Arrays.asList( fileName2.split( "\\r\\n" ) );
    assertLinesOfText( new String[] { file2Content }, lines );
  }




  @Test
  public void testPostWithMultipleMultipartFormFields() throws Exception {
    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\n";
    final String content = "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE + "\r\n" + "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE2 + "\r\n" + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( 2, testServer.parms.size() );
    assertEquals( HttpPostRequestTest.VALUE, testServer.parms.get( HttpPostRequestTest.FIELD ) );
    assertEquals( HttpPostRequestTest.VALUE2, testServer.parms.get( HttpPostRequestTest.FIELD2 ) );
  }




  @Test
  public void testPostWithMultipleMultipartFormFieldsWhereContentTypeWasSeparatedByComma() throws Exception {
    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data, boundary=" + divider + "\r\n";
    final String content = "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE + "\r\n" + "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD2 + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE2 + "\r\n" + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( 2, testServer.parms.size() );
    assertEquals( HttpPostRequestTest.VALUE, testServer.parms.get( HttpPostRequestTest.FIELD ) );
    assertEquals( HttpPostRequestTest.VALUE2, testServer.parms.get( HttpPostRequestTest.FIELD2 ) );
  }




  @Test
  public void testSimplePostWithSingleMultipartFormField() throws Exception {
    final String divider = UUID.randomUUID().toString();
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\nContent-Type: " + "multipart/form-data; boundary=" + divider + "\r\n";
    final String content = "--" + divider + "\r\n" + "Content-Disposition: form-data; name=\"" + HttpPostRequestTest.FIELD + "\"\r\n" + "\r\n" + HttpPostRequestTest.VALUE + "\r\n" + "--" + divider + "--\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( 1, testServer.parms.size() );
    assertEquals( HttpPostRequestTest.VALUE, testServer.parms.get( HttpPostRequestTest.FIELD ) );
  }




  @Test
  public void testSimpleRawPostData() throws Exception {
    final String header = "POST " + HttpServerTest.URI + " HTTP/1.1\n";
    final String content = HttpPostRequestTest.VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPERSAND + "\r\n";
    final int size = content.length() + header.length();
    final int contentLengthHeaderValueSize = String.valueOf( size ).length();
    final int contentLength = size + contentLengthHeaderValueSize + HttpPostRequestTest.CONTENT_LENGTH.length();
    final String input = header + HttpPostRequestTest.CONTENT_LENGTH + ( contentLength + 4 ) + "\r\n\r\n" + content;
    invokeServer( input );
    assertEquals( 0, testServer.parms.size() );
    assertEquals( 1, testServer.body.size() );
    assertEquals( HttpPostRequestTest.VALUE_TEST_SIMPLE_RAW_DATA_WITH_AMPERSAND, testServer.body.get( HttpPostRequestTest.POST_RAW_CONTENT_FILE_ENTRY ) );
  }

}
