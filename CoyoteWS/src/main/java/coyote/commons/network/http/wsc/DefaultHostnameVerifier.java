package coyote.commons.network.http.wsc;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;


/**
 * A HostnameVerifier consistent with <a
 * href="http://www.ietf.org/rfc/rfc2818.txt">RFC 2818</a>.
 */
final class OkHostnameVerifier implements HostnameVerifier {

  /**
   * Quick and dirty pattern to differentiate IP addresses from hostnames. This
   * is an approximation of Android's private InetAddress#isNumeric API.
   *
   * <p>This matches IPv6 addresses as a hex string containing at least one
   * colon, and possibly including dots after the first colon. It matches IPv4
   * addresses as strings containing only decimal digits and dots. This pattern
   * matches strings like "a:.23" and "54" that are neither IP addresses nor
   * hostnames; they will be verified as IP addresses (which is a more strict
   * verification).
   */
  private static final Pattern VERIFY_AS_IP_ADDRESS = Pattern.compile("([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)");
  public static final OkHostnameVerifier INSTANCE = new OkHostnameVerifier();
  private static final int ALT_DNS_NAME = 2;
  private static final int ALT_IPA_NAME = 7;




  private OkHostnameVerifier() {
    // no external instances
  }




  public static List<String> allSubjectAltNames(final X509Certificate certificate) {
    final List<String> altIpaNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
    final List<String> altDnsNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
    final List<String> result = new ArrayList<String>(altIpaNames.size() + altDnsNames.size());
    result.addAll(altIpaNames);
    result.addAll(altDnsNames);
    return result;
  }




  private static List<String> getSubjectAltNames(final X509Certificate certificate, final int type) {
    final List<String> result = new ArrayList<String>();
    try {
      final Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
      if (subjectAltNames == null) {
        return Collections.emptyList();
      }
      for (final Object subjectAltName : subjectAltNames) {
        final List<?> entry = (List<?>)subjectAltName;
        if (entry == null || entry.size() < 2) {
          continue;
        }
        final Integer altNameType = (Integer)entry.get(0);
        if (altNameType == null) {
          continue;
        }
        if (altNameType == type) {
          final String altName = (String)entry.get(1);
          if (altName != null) {
            result.add(altName);
          }
        }
      }
      return result;
    } catch (final CertificateParsingException e) {
      return Collections.emptyList();
    }
  }




  static boolean verifyAsIpAddress(final String host) {
    return VERIFY_AS_IP_ADDRESS.matcher(host).matches();
  }




  @Override
  public boolean verify(final String host, final SSLSession session) {
    try {
      final Certificate[] certificates = session.getPeerCertificates();
      return verify(host, (X509Certificate)certificates[0]);
    } catch (final SSLException e) {
      return false;
    }
  }




  public boolean verify(final String host, final X509Certificate certificate) {
    return verifyAsIpAddress(host) ? verifyIpAddress(host, certificate) : verifyHostName(host, certificate);
  }




  /**
   * Returns {@code true} iff {@code hostName} matches the domain name {@code pattern}.
   *
   * @param hostName lower-case host name.
   * @param pattern domain name pattern from certificate. May be a wildcard pattern such as
   *        {@code *.android.com}.
   */
  private boolean verifyHostName(String hostName, String pattern) {
    // Basic sanity checks
    // Check length == 0 instead of .isEmpty() to support Java 5.
    if ((hostName == null) || (hostName.length() == 0) || (hostName.startsWith(".")) || (hostName.endsWith(".."))) {
      // Invalid domain name
      return false;
    }
    if ((pattern == null) || (pattern.length() == 0) || (pattern.startsWith(".")) || (pattern.endsWith(".."))) {
      // Invalid pattern/domain name
      return false;
    }

    // Normalize hostName and pattern by turning them into absolute domain names if they are not
    // yet absolute. This is needed because server certificates do not normally contain absolute
    // names or patterns, but they should be treated as absolute. At the same time, any hostName
    // presented to this method should also be treated as absolute for the purposes of matching
    // to the server certificate.
    //   www.android.com  matches www.android.com
    //   www.android.com  matches www.android.com.
    //   www.android.com. matches www.android.com.
    //   www.android.com. matches www.android.com
    if (!hostName.endsWith(".")) {
      hostName += '.';
    }
    if (!pattern.endsWith(".")) {
      pattern += '.';
    }
    // hostName and pattern are now absolute domain names.

    pattern = pattern.toLowerCase(Locale.US);
    // hostName and pattern are now in lower case -- domain names are case-insensitive.

    if (!pattern.contains("*")) {
      // Not a wildcard pattern -- hostName and pattern must match exactly.
      return hostName.equals(pattern);
    }
    // Wildcard pattern

    // WILDCARD PATTERN RULES:
    // 1. Asterisk (*) is only permitted in the left-most domain name label and must be the
    //    only character in that label (i.e., must match the whole left-most label).
    //    For example, *.example.com is permitted, while *a.example.com, a*.example.com,
    //    a*b.example.com, a.*.example.com are not permitted.
    // 2. Asterisk (*) cannot match across domain name labels.
    //    For example, *.example.com matches test.example.com but does not match
    //    sub.test.example.com.
    // 3. Wildcard patterns for single-label domain names are not permitted.

    if ((!pattern.startsWith("*.")) || (pattern.indexOf('*', 1) != -1)) {
      // Asterisk (*) is only permitted in the left-most domain name label and must be the only
      // character in that label
      return false;
    }

    // Optimization: check whether hostName is too short to match the pattern. hostName must be at
    // least as long as the pattern because asterisk must match the whole left-most label and
    // hostName starts with a non-empty label. Thus, asterisk has to match one or more characters.
    if (hostName.length() < pattern.length()) {
      // hostName too short to match the pattern.
      return false;
    }

    if ("*.".equals(pattern)) {
      // Wildcard pattern for single-label domain name -- not permitted.
      return false;
    }

    // hostName must end with the region of pattern following the asterisk.
    final String suffix = pattern.substring(1);
    if (!hostName.endsWith(suffix)) {
      // hostName does not end with the suffix
      return false;
    }

    // Check that asterisk did not match across domain name labels.
    final int suffixStartIndexInHostName = hostName.length() - suffix.length();
    if ((suffixStartIndexInHostName > 0) && (hostName.lastIndexOf('.', suffixStartIndexInHostName - 1) != -1)) {
      // Asterisk is matching across domain name labels -- not permitted.
      return false;
    }

    // hostName matches pattern
    return true;
  }




  /**
   * Returns true if {@code certificate} matches {@code hostName}.
   */
  private boolean verifyHostName(String hostName, final X509Certificate certificate) {
    hostName = hostName.toLowerCase(Locale.US);
    boolean hasDns = false;
    final List<String> altNames = getSubjectAltNames(certificate, ALT_DNS_NAME);
    for (int i = 0, size = altNames.size(); i < size; i++) {
      hasDns = true;
      if (verifyHostName(hostName, altNames.get(i))) {
        return true;
      }
    }

    if (!hasDns) {
      final X500Principal principal = certificate.getSubjectX500Principal();
      // RFC 2818 advises using the most specific name for matching.
      final String cn = new DistinguishedNameParser(principal).findMostSpecific("cn");
      if (cn != null) {
        return verifyHostName(hostName, cn);
      }
    }

    return false;
  }




  /**
   * Returns true if {@code certificate} matches {@code ipAddress}.
   */
  private boolean verifyIpAddress(final String ipAddress, final X509Certificate certificate) {
    final List<String> altNames = getSubjectAltNames(certificate, ALT_IPA_NAME);
    for (int i = 0, size = altNames.size(); i < size; i++) {
      if (ipAddress.equalsIgnoreCase(altNames.get(i))) {
        return true;
      }
    }
    return false;
  }
}
