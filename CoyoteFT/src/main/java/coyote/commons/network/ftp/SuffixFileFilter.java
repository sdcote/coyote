package coyote.commons.network.ftp;

import java.util.List;

import coyote.commons.CollectionUtil;
import coyote.commons.StringUtil;
import coyote.commons.network.RemoteFile;


/**
 * File Filter that matches files with the defined suffixes.
 * 
 * <p>This is performed with a simple "endsWith" check so to '.' has no special 
 * meaning in determining a match. The all return true for a suffix check of 
 * "SV":<ul>
 * <li>test.SV</li>
 * <li>test.DSV</li>
 * <li>test.CSV</li>
 * <li>test.TSV</li></ul>
 */
public class SuffixFileFilter implements FileFilter {

  public SuffixFileFilter( final List<String> suffixes ) {
    this.suffixes = suffixes;
  }

  private List<String> suffixes;




  public List<String> getSuffixes() {
    return suffixes;
  }




  public void setSuffixes( final List<String> suffixes ) {
    this.suffixes = suffixes;
  }




  private boolean checkFileName( final String name ) {
    // If there are no suffixes to filter, match on any non-blank name
    if ( CollectionUtil.isEmpty( suffixes ) && StringUtil.isNotBlank( name ) ) {
      return true;
    } else if ( CollectionUtil.isNotEmpty( suffixes ) && StringUtil.isNotBlank( name ) ) {
      for ( final String suffix : suffixes ) {
        final boolean match = checkEndsWith( name, suffix, true );
        if ( match ) {
          return true;
        }
      }
    }
    return false;
  }




  /**
   * Checks if one string ends with another using the case-sensitivity rule.
   * <p>
   * This method mimics {@link String#endsWith} but takes case-sensitivity
   * into account.
   * 
   * @param str  the string to check, not null
   * @param end  the end to compare against, not null
   * @return true if equal using the case rules
   * @throws NullPointerException if either string is null
   */
  public boolean checkEndsWith( String str, String end, boolean ignorecase ) {
    int endLen = end.length();
    return str.regionMatches( ignorecase, str.length() - endLen, end, 0, endLen );
  }




  @Override
  public boolean accept( RemoteFile entry ) {
    return ( null != entry ) && ( null != entry.getAttrs() ) && !entry.getAttrs().isDirectory() ? checkFileName( entry.getName() ) : false;
  }




  @Override
  public String toString() {
    final StringBuilder b = new StringBuilder();
    b.append( this.getClass().getSimpleName() );
    b.append( " {" );
    for ( final String token : suffixes ) {
      b.append( '"' );
      b.append( token );
      b.append( '"' );
      b.append( ", " );
    }
    b.delete( b.length() - 2, b.length() );
    b.append( "}" );

    return b.toString();
  }

}
