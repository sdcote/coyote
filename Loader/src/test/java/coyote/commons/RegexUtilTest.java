package coyote.commons;

//import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


public class RegexUtilTest {

  private void assertMatch( String glob, String argument ) {
    assertMatch( glob, argument, false, false, 0 );
  }




  private void assertMatch( String glob, String argument, boolean extended, boolean global, int flags ) {

    Pattern pattern = RegexUtil.globToRegex( glob, extended, global, flags );
    Matcher matcher = pattern.matcher( argument );

    assertTrue( matcher.matches() );
  }




  private void assertNotMatch( String glob, String argument ) {
    assertNotMatch( glob, argument, false, false, 0 );
  }




  private void assertNotMatch( String glob, String argument, boolean extended, boolean global, int flags ) {

    Pattern pattern = RegexUtil.globToRegex( glob, extended, global, flags );
    Matcher matcher = pattern.matcher( argument );

    assertFalse( matcher.matches() );
  }




  @Test
  public void testGlobToRegex() {
    // Match everything
    assertMatch( "*", "foo" );
    assertMatch( "*", "foo", false, true, 0 );

    // Match the end
    assertMatch( "f*", "foo" );
    assertMatch( "f*", "foo", false, true, 0 );

    // Match the start
    assertMatch( "*o", "foo" );
    assertMatch( "*o", "foo", false, true, 0 );

    // Match the middle
    assertMatch( "fire*uck", "firetruck" );
    assertMatch( "fire*uck", "firetruck", false, true, 0 );

    // Don't match without Regexp 'g'
    assertNotMatch( "uc", "firetruck" );
    // Match anywhere with RegExp 'g'
    //assertMatch( "uc", "firetruck", false, true, 0 );

    // Match zero characters
    assertMatch( "b*eer", "beer" );
    assertMatch( "b*eer", "beer", false, true, 0 );

    // More complex matches
    assertMatch( "*.min.js", "http://example.com/jquery.min.js" );
    assertMatch( "*.min.*", "http://example.com/jquery.min.js" );
    assertMatch( "*/js/*.js", "http://example.com/js/jquery.min.js" );

    // More complex matches with RegExp 'g' flag (complex regression)
    assertMatch( "*.min.*", "http://example.com/jquery.min.js", false, true, 0 );
    assertMatch( "*.min.js", "http://example.com/jquery.min.js", false, true, 0 );
    assertMatch( "*/js/*.js", "http://example.com/js/jquery.min.js", false, true, 0 );

    String testStr = "\\/$^+?.()=!|{},[].*";
    assertMatch( testStr, testStr );
    assertMatch( testStr, testStr, false, true, 0 );

    // Equivalent matches without/with using RegExp 'g'
    assertNotMatch( ".min.", "http://example.com/jquery.min.js" );
    assertMatch( "*.min.*", "http://example.com/jquery.min.js" );

    assertNotMatch( "http:", "http://example.com/jquery.min.js" );
    assertMatch( "http:*", "http://example.com/jquery.min.js" );

    assertNotMatch( "min.js", "http://example.com/jquery.min.js" );
    assertMatch( "*.min.js", "http://example.com/jquery.min.js" );

    // Match anywhere (globally) using RegExp 'g'
    assertMatch( "*min*", "http://example.com/jquery.min.js", false, true, 0 );
    assertMatch( "*/js/*", "http://example.com/js/jquery.min.js", false, true, 0 );

    assertNotMatch( "/js*jq*.js", "http://example.com/js/jquery.min.js" );
    assertMatch( "*/js*jq*.js", "http://example.com/js/jquery.min.js" );
    assertMatch( "*/js*jq*.js*", "http://example.com/js/jquery.min.js", false, true, 0 );

    // Extended mode

    // ?: Match one character, no more and no less
    assertMatch( "f?o", "foo", true, false, 0 );
    assertNotMatch( "f?o", "fooo", true, false, 0 );
    assertNotMatch( "f?oo", "foo", true, false, 0 );

    // ?: Match one character with RegExp 'g'
    assertMatch( "f?o", "foo", true, true, 0 );
    assertNotMatch( "f?o", "fooo", true, true, 0 );
    assertMatch( "f?o?", "fooo", true, true, 0 );
    assertNotMatch( "?fo", "fooo", true, true, 0 );
    assertNotMatch( "f?oo", "foo", true, true, 0 );
    assertNotMatch( "foo?", "foo", true, true, 0 );

    // []: Match a character range
    assertMatch( "fo[oz]", "foo", true, false, 0 );
    assertMatch( "fo[oz]", "foz", true, false, 0 );
    assertNotMatch( "fo[oz]", "fog", true, false, 0 );

    // []: Match a character range and RegExp 'g' (regression)
    assertMatch( "fo[oz]", "foo", true, true, 0 );
    assertMatch( "fo[oz]", "foz", true, true, 0 );
    assertNotMatch( "fo[oz]", "fog", true, true, 0 );

    // {}: Match a choice of different substrings
    assertMatch( "foo{bar,baaz}", "foobaaz", true, false, 0 );
    assertMatch( "foo{bar,baaz}", "foobar", true, false, 0 );
    assertNotMatch( "foo{bar,baaz}", "foobuzz", true, false, 0 );
    assertMatch( "foo{bar,b*z}", "foobuzz", true, false, 0 );

    // {}: Match a choice of different substrings and RegExp 'g' (regression)
    assertMatch( "foo{bar,baaz}", "foobaaz", true, true, 0 );
    assertMatch( "foo{bar,baaz}", "foobar", true, true, 0 );
    assertNotMatch( "foo{bar,baaz}", "foobuzz", true, true, 0 );
    assertMatch( "foo{bar,b*z}", "foobuzz", true, true, 0 );

    // More complex extended matches
    assertMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://foo.baaz.com/jquery.min.js", true, false, 0 );
    assertMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.buzz.com/index.html", true, false, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.buzz.com/index.htm", true, false, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.bar.com/index.html", true, false, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://flozz.buzz.com/index.html", true, false, 0 );

    // More complex extended matches and RegExp 'g' (regresion)
    assertMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://foo.baaz.com/jquery.min.js", true, true, 0 );
    assertMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.buzz.com/index.html", true, true, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.buzz.com/index.htm", true, true, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://moz.bar.com/index.html", true, true, 0 );
    assertNotMatch( "http://?o[oz].b*z.com/{*.js,*.html}", "http://flozz.buzz.com/index.html", true, true, 0 );

  }

}
