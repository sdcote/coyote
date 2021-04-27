package coyote.dx.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


public class WildcardFileFilterTest {

  @Test
  public void testAccept() {}




  @Test
  public void testWildcardMatch() {}




  @Test
  public void testCheckRegionMatches() {
    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 0, "" ) );
    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 0, "A" ) );
    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 0, "AB" ) );
    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 0, "ABC" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 0, "BC" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 0, "C" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 0, "ABCD" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "", 0, "ABC" ) );
    assertTrue( WildcardFileFilter.checkRegionMatches( "", 0, "" ) );

    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 1, "" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 1, "A" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 1, "AB" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 1, "ABC" ) );
    assertTrue( WildcardFileFilter.checkRegionMatches( "ABC", 1, "BC" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 1, "C" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "ABC", 1, "ABCD" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "", 1, "ABC" ) );
    assertFalse( WildcardFileFilter.checkRegionMatches( "", 1, "" ) );

    try {
      WildcardFileFilter.checkRegionMatches( "ABC", 0, null );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkRegionMatches( null, 0, "ABC" );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkRegionMatches( null, 0, null );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkRegionMatches( "ABC", 1, null );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkRegionMatches( null, 1, "ABC" );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkRegionMatches( null, 1, null );
      fail();
    } catch ( final NullPointerException ex ) {}
  }




  @Test
  public void testCheckIndexOf() {
    // start
    assertEquals( 0, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "A" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 1, "A" ) );
    assertEquals( 0, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "AB" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 1, "AB" ) );
    assertEquals( 0, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "ABC" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 1, "ABC" ) );

    // middle
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "D" ) );
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 3, "D" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 4, "D" ) );
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "DE" ) );
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 3, "DE" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 4, "DE" ) );
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "DEF" ) );
    assertEquals( 3, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 3, "DEF" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 4, "DEF" ) );

    // end
    assertEquals( 9, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "J" ) );
    assertEquals( 9, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 8, "J" ) );
    assertEquals( 9, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 9, "J" ) );
    assertEquals( 8, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "IJ" ) );
    assertEquals( 8, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 8, "IJ" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 9, "IJ" ) );
    assertEquals( 7, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 6, "HIJ" ) );
    assertEquals( 7, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 7, "HIJ" ) );
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 8, "HIJ" ) );

    // not found
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "ABCDEFGHIJ", 0, "DED" ) );

    // too long
    assertEquals( -1, WildcardFileFilter.checkIndexOf( "DEF", 0, "ABCDEFGHIJ" ) );

    try {
      WildcardFileFilter.checkIndexOf( "ABC", 0, null );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkIndexOf( null, 0, "ABC" );
      fail();
    } catch ( final NullPointerException ex ) {}
    try {
      WildcardFileFilter.checkIndexOf( null, 0, null );
      fail();
    } catch ( final NullPointerException ex ) {}
    assertEquals( 1, WildcardFileFilter.checkIndexOf( "ABC", 0, "BC" ) );
    assertEquals( 1, WildcardFileFilter.checkIndexOf( "ABC", 0, "Bc" ) );

    assertEquals( 1, WildcardFileFilter.checkIndexOf( "ABC", 0, "BC" ) );
    assertEquals( 1, WildcardFileFilter.checkIndexOf( "ABC", 0, "Bc" ) );

  }




  @Test
  public void testSplitOnTokens() {}

}
