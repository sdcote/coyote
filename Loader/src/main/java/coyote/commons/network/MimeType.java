/*
 * Copyright (c) 2015 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 *
 * Contributors:
 *   Stephan D. Cote 
 *      - Initial concept and implementation
 */
package coyote.commons.network;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import coyote.commons.StringUtil;


/**
 * Models a MIME type for attachments and web applications.
 * 
 * <p>Order is important. Most code will only select the first MIME type 
 * returned, so make sure the preferred MIME type appears first.</p>
 */
public class MimeType {

  private static final MimeType UNKNOWN = new MimeType( "unknown", "application/octet-stream", true );
  private static final List<MimeType> MIMES = new ArrayList<MimeType>();

  // handy constants for more readable code
  public static final MimeType JSON = new MimeType( "json", "application/json", false );
  public static final MimeType XML = new MimeType( "xml", "application/xml", false );
  public static final MimeType SOAP = new MimeType( "xml", "application/soap+xml", false );
  public static final MimeType TEXT = new MimeType( "text", "text/plain", false );
  public static final MimeType HTML = new MimeType( "html", "text/html", false );
  public static final MimeType PDF = new MimeType( "pdf", "application/pdf", true );
  public static final MimeType ANY = new MimeType( "*", "*/*", true );
  public static final MimeType MULTIPART_FORM = new MimeType( "", "multipart/form-data", false );
  public static final MimeType APPLICATION_FORM = new MimeType( "", "application/x-www-form-urlencoded", false );
  public static final MimeType ATOM_XML = new MimeType( "", "application/atom+xml", false );
  public static final MimeType XHTML_XML = new MimeType( "", "application/xhtml+xml", false );
  public static final MimeType SVG_XML = new MimeType( "", "application/svg+xml", false );

  private String extension, type;
  private boolean binary;

  static {
    MIMES.add( JSON );
    MIMES.add( XML );
    MIMES.add( new MimeType( "3dm", "x-world/x-3dmf", true ) );
    MIMES.add( new MimeType( "3dmf", "x-world/x-3dmf", true ) );
    MIMES.add( new MimeType( "a", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "aab", "application/x-authorware-bin", true ) );
    MIMES.add( new MimeType( "aam", "application/x-authorware-map", true ) );
    MIMES.add( new MimeType( "aas", "application/x-authorware-seg", true ) );
    MIMES.add( new MimeType( "abc", "text/vnd.abc", false ) );
    MIMES.add( new MimeType( "acgi", "text/html", false ) );
    MIMES.add( new MimeType( "afl", "video/animaflex", true ) );
    MIMES.add( new MimeType( "ai", "application/postscript", true ) );
    MIMES.add( new MimeType( "ai", "application/postscript", true ) );
    MIMES.add( new MimeType( "aif", "audio/aiff", true ) );
    MIMES.add( new MimeType( "aif", "audio/x-aiff", true ) );
    MIMES.add( new MimeType( "aifc", "audio/aiff", true ) );
    MIMES.add( new MimeType( "aifc", "audio/x-aiff", true ) );
    MIMES.add( new MimeType( "aifc", "audio/x-aiff", true ) );
    MIMES.add( new MimeType( "aiff", "audio/aiff", true ) );
    MIMES.add( new MimeType( "aiff", "audio/x-aiff", true ) );
    MIMES.add( new MimeType( "aim", "application/x-aim", true ) );
    MIMES.add( new MimeType( "aip", "text/x-audiosoft-intra", false ) );
    MIMES.add( new MimeType( "ani", "application/x-navi-animation", true ) );
    MIMES.add( new MimeType( "aos", "application/x-nokia-9000-communicator-add-on-software", true ) );
    MIMES.add( new MimeType( "aps", "application/mime", true ) );
    MIMES.add( new MimeType( "arc", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "arj", "application/arj", true ) );
    MIMES.add( new MimeType( "arj", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "art", "image/x-jg", true ) );
    MIMES.add( new MimeType( "asd", "application/astound", true ) );
    MIMES.add( new MimeType( "asf", "video/x-ms-asf", true ) );
    MIMES.add( new MimeType( "asm", "text/x-asm", false ) );
    MIMES.add( new MimeType( "asn", "application/astound", true ) );
    MIMES.add( new MimeType( "asp", "text/asp", false ) );
    MIMES.add( new MimeType( "asx", "video/x-ms-asf", true ) );
    MIMES.add( new MimeType( "au", "audio/basic", true ) );
    MIMES.add( new MimeType( "au", "audio/basic", true ) );
    MIMES.add( new MimeType( "avi", "video/avi", true ) );
    MIMES.add( new MimeType( "avi", "video/msvideo", true ) );
    MIMES.add( new MimeType( "avi", "video/x-msvideo", true ) );
    MIMES.add( new MimeType( "avs", "video/avs-video", true ) );
    MIMES.add( new MimeType( "bat", "text/plain", false ) );
    MIMES.add( new MimeType( "bcpio", "application/x-bcpio", true ) );
    MIMES.add( new MimeType( "bin", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "bin", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "bin", "application/x-macbinary", true ) );
    MIMES.add( new MimeType( "bm", "image/bmp", true ) );
    MIMES.add( new MimeType( "bmp", "image/bmp", true ) );
    MIMES.add( new MimeType( "bmp", "image/x-ms-bmp", true ) );
    MIMES.add( new MimeType( "boo", "application/book", true ) );
    MIMES.add( new MimeType( "book", "application/book", true ) );
    MIMES.add( new MimeType( "boz", "application/x-bzip2", true ) );
    MIMES.add( new MimeType( "bsh", "application/x-bsh", true ) );
    MIMES.add( new MimeType( "bz", "application/x-bzip", true ) );
    MIMES.add( new MimeType( "bz2", "application/x-bzip2", true ) );
    MIMES.add( new MimeType( "c", "text/plain", false ) );
    MIMES.add( new MimeType( "c", "text/x-c", false ) );
    MIMES.add( new MimeType( "c++", "text/plain", false ) );
    MIMES.add( new MimeType( "cat", "application/vnd.ms-pki.seccat", true ) );
    MIMES.add( new MimeType( "cc", "text/plain", false ) );
    MIMES.add( new MimeType( "ccad", "application/clariscad", true ) );
    MIMES.add( new MimeType( "cco", "application/x-cocoa", true ) );
    MIMES.add( new MimeType( "cdf", "application/cdf", true ) );
    MIMES.add( new MimeType( "cdf", "application/x-cdf", true ) );
    MIMES.add( new MimeType( "cdf", "application/x-netcdf", true ) );
    MIMES.add( new MimeType( "cer", "application/x-x509-ca-cert", true ) );
    MIMES.add( new MimeType( "chat", "application/x-chat", true ) );
    MIMES.add( new MimeType( "class", "application/java", true ) );
    MIMES.add( new MimeType( "com", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "conf", "text/plain", false ) );
    MIMES.add( new MimeType( "cpio", "application/x-cpio", true ) );
    MIMES.add( new MimeType( "cpp", "text/x-c", false ) );
    MIMES.add( new MimeType( "cpt", "application/mac-compactpro", true ) );
    MIMES.add( new MimeType( "cpt", "application/x-compactpro", true ) );
    MIMES.add( new MimeType( "cpt", "application/x-cpt", true ) );
    MIMES.add( new MimeType( "crl", "application/pkcs-crl", true ) );
    MIMES.add( new MimeType( "crl", "application/pkix-crl", true ) );
    MIMES.add( new MimeType( "crt", "application/pkix-cert", true ) );
    MIMES.add( new MimeType( "crt", "application/x-x509-ca-cert", true ) );
    MIMES.add( new MimeType( "crt", "application/x-x509-user-cert", true ) );
    MIMES.add( new MimeType( "csh", "application/x-csh", false ) );
    MIMES.add( new MimeType( "csh", "text/x-script.csh", false ) );
    MIMES.add( new MimeType( "css", "text/css", false ) );
    MIMES.add( new MimeType( "css", "application/x-pointplus", true ) );
    MIMES.add( new MimeType( "cxx", "text/plain", false ) );
    MIMES.add( new MimeType( "dcr", "application/x-director", true ) );
    MIMES.add( new MimeType( "deepv", "application/x-deepv", true ) );
    MIMES.add( new MimeType( "def", "text/plain", false ) );
    MIMES.add( new MimeType( "der", "application/x-x509-ca-cert", true ) );
    MIMES.add( new MimeType( "dif", "video/x-dv", true ) );
    MIMES.add( new MimeType( "dir", "application/x-director", true ) );
    MIMES.add( new MimeType( "dir", "application/x-director", true ) );
    MIMES.add( new MimeType( "dl", "video/dl", true ) );
    MIMES.add( new MimeType( "dl", "video/x-dl", true ) );
    MIMES.add( new MimeType( "dll", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "doc", "application/msword", true ) );
    MIMES.add( new MimeType( "dot", "application/msword", true ) );
    MIMES.add( new MimeType( "dp", "application/commonground", true ) );
    MIMES.add( new MimeType( "drw", "application/drafting", true ) );
    MIMES.add( new MimeType( "dump", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "dv", "video/x-dv", true ) );
    MIMES.add( new MimeType( "dvi", "application/x-dvi", true ) );
    MIMES.add( new MimeType( "dwf", "application/x-dwf", true ) );
    MIMES.add( new MimeType( "dwf", "drawing/x-dwf", true ) );
    MIMES.add( new MimeType( "dwf", "model/vnd.dwf", true ) );
    MIMES.add( new MimeType( "dwg", "application/acad", true ) );
    MIMES.add( new MimeType( "dwg", "application/acad", true ) );
    MIMES.add( new MimeType( "dwg", "image/vnd.dwg", true ) );
    MIMES.add( new MimeType( "dwg", "image/x-dwg", true ) );
    MIMES.add( new MimeType( "dxf", "application/dxf", true ) );
    MIMES.add( new MimeType( "dxf", "application/dxf", true ) );
    MIMES.add( new MimeType( "dxf", "image/vnd.dwg", true ) );
    MIMES.add( new MimeType( "dxf", "image/x-dwg", true ) );
    MIMES.add( new MimeType( "dxr", "application/x-director", true ) );
    MIMES.add( new MimeType( "dxr", "application/x-director", true ) );
    MIMES.add( new MimeType( "el", "text/x-script.elisp", false ) );
    MIMES.add( new MimeType( "elc", "application/x-bytecode.elisp (compiled elisp)", true ) );
    MIMES.add( new MimeType( "elc", "application/x-elc", true ) );
    MIMES.add( new MimeType( "eml", "message/rfc822", true ) );
    MIMES.add( new MimeType( "env", "application/x-envoy", true ) );
    MIMES.add( new MimeType( "eps", "application/postscript", true ) );
    MIMES.add( new MimeType( "eps", "application/postscript", true ) );
    MIMES.add( new MimeType( "eps", "application/postscript", true ) );
    MIMES.add( new MimeType( "es", "application/x-esrehber", true ) );
    MIMES.add( new MimeType( "etx", "text/x-setext", false ) );
    MIMES.add( new MimeType( "evy", "application/envoy", true ) );
    MIMES.add( new MimeType( "evy", "application/x-envoy", true ) );
    MIMES.add( new MimeType( "exe", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "exe", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "f", "text/plain", false ) );
    MIMES.add( new MimeType( "f", "text/x-fortran", false ) );
    MIMES.add( new MimeType( "f77", "text/x-fortran", false ) );
    MIMES.add( new MimeType( "f90", "text/plain", false ) );
    MIMES.add( new MimeType( "f90", "text/x-fortran", false ) );
    MIMES.add( new MimeType( "fdf", "application/vnd.fdf", true ) );
    MIMES.add( new MimeType( "fif", "application/fractals", true ) );
    MIMES.add( new MimeType( "fif", "image/fif", true ) );
    MIMES.add( new MimeType( "fli", "video/fli", true ) );
    MIMES.add( new MimeType( "fli", "video/x-fli", true ) );
    MIMES.add( new MimeType( "flo", "image/florian", true ) );
    MIMES.add( new MimeType( "flx", "text/vnd.fmi.flexstor", false ) );
    MIMES.add( new MimeType( "fmf", "video/x-atomic3d-feature", true ) );
    MIMES.add( new MimeType( "for", "text/plain", false ) );
    MIMES.add( new MimeType( "for", "text/x-fortran", false ) );
    MIMES.add( new MimeType( "fpx", "image/vnd.fpx", true ) );
    MIMES.add( new MimeType( "fpx", "image/vnd.net-fpx", true ) );
    MIMES.add( new MimeType( "frl", "application/freeloader", true ) );
    MIMES.add( new MimeType( "funk", "audio/make", true ) );
    MIMES.add( new MimeType( "g", "text/plain", false ) );
    MIMES.add( new MimeType( "g3", "image/g3fax", true ) );
    MIMES.add( new MimeType( "gif", "image/gif", true ) );
    MIMES.add( new MimeType( "gl", "video/gl", true ) );
    MIMES.add( new MimeType( "gl", "video/x-gl", true ) );
    MIMES.add( new MimeType( "gsd", "audio/x-gsm", true ) );
    MIMES.add( new MimeType( "gsm", "audio/x-gsm", true ) );
    MIMES.add( new MimeType( "gsp", "application/x-gsp", true ) );
    MIMES.add( new MimeType( "gss", "application/x-gss", true ) );
    MIMES.add( new MimeType( "gtar", "application/x-gtar", true ) );
    MIMES.add( new MimeType( "gz", "application/x-compressed", true ) );
    MIMES.add( new MimeType( "gz", "application/x-gzip", true ) );
    MIMES.add( new MimeType( "gzip", "application/x-gzip", true ) );
    MIMES.add( new MimeType( "gzip", "multipart/x-gzip", true ) );
    MIMES.add( new MimeType( "h", "text/plain", false ) );
    MIMES.add( new MimeType( "h", "text/x-h", false ) );
    MIMES.add( new MimeType( "hdf", "application/x-hdf", true ) );
    MIMES.add( new MimeType( "help", "application/x-helpfile", true ) );
    MIMES.add( new MimeType( "hgl", "application/vnd.hp-hpgl", true ) );
    MIMES.add( new MimeType( "hh", "text/plain", false ) );
    MIMES.add( new MimeType( "hh", "text/x-h", false ) );
    MIMES.add( new MimeType( "hlb", "text/x-script", false ) );
    MIMES.add( new MimeType( "hlp", "application/hlp", true ) );
    MIMES.add( new MimeType( "hlp", "application/x-helpfile", true ) );
    MIMES.add( new MimeType( "hlp", "application/x-winhelp", true ) );
    MIMES.add( new MimeType( "hpg", "application/vnd.hp-hpgl", true ) );
    MIMES.add( new MimeType( "hpgl", "application/vnd.hp-hpgl", true ) );
    MIMES.add( new MimeType( "hqx", "application/binhex", true ) );
    MIMES.add( new MimeType( "hqx", "application/binhex4", true ) );
    MIMES.add( new MimeType( "hqx", "application/mac-binhex", true ) );
    MIMES.add( new MimeType( "hqx", "application/mac-binhex40", true ) );
    MIMES.add( new MimeType( "hqx", "application/x-binhex40", true ) );
    MIMES.add( new MimeType( "hqx", "application/x-mac-binhex40", true ) );
    MIMES.add( new MimeType( "hta", "application/hta", true ) );
    MIMES.add( new MimeType( "htc", "text/x-component", false ) );
    MIMES.add( new MimeType( "htm", "text/html", false ) );
    MIMES.add( HTML );
    MIMES.add( new MimeType( "htmls", "text/html", false ) );
    MIMES.add( new MimeType( "htt", "text/webviewhtml", false ) );
    MIMES.add( new MimeType( "htx", "text/html", false ) );
    MIMES.add( new MimeType( "ice", "x-conference/x-cooltalk", true ) );
    MIMES.add( new MimeType( "ico", "image/x-icon", true ) );
    MIMES.add( new MimeType( "idc", "text/plain", false ) );
    MIMES.add( new MimeType( "ief", "image/ief", true ) );
    MIMES.add( new MimeType( "iefs", "image/ief", true ) );
    MIMES.add( new MimeType( "iges", "application/iges", true ) );
    MIMES.add( new MimeType( "iges", "model/iges", true ) );
    MIMES.add( new MimeType( "igs", "application/iges", true ) );
    MIMES.add( new MimeType( "igs", "model/iges", true ) );
    MIMES.add( new MimeType( "ima", "application/x-ima", true ) );
    MIMES.add( new MimeType( "imap", "application/x-httpd-imap", true ) );
    MIMES.add( new MimeType( "inf", "application/inf", true ) );
    MIMES.add( new MimeType( "ins", "application/x-internett-signup", true ) );
    MIMES.add( new MimeType( "ip", "application/x-ip2", true ) );
    MIMES.add( new MimeType( "isu", "video/x-isvideo", true ) );
    MIMES.add( new MimeType( "it", "audio/it", true ) );
    MIMES.add( new MimeType( "iv", "application/x-inventor", true ) );
    MIMES.add( new MimeType( "ivr", "i-world/i-vrml", true ) );
    MIMES.add( new MimeType( "ivy", "application/x-livescreen", true ) );
    MIMES.add( new MimeType( "jam", "audio/x-jam", true ) );
    MIMES.add( new MimeType( "jar", "application/java-archive", true ) );
    MIMES.add( new MimeType( "jav", "text/plain", false ) );
    MIMES.add( new MimeType( "jav", "text/x-java-source", false ) );
    MIMES.add( new MimeType( "java", "text/plain", false ) );
    MIMES.add( new MimeType( "java", "text/x-java-source", false ) );
    MIMES.add( new MimeType( "jcm", "application/x-java-commerce", true ) );
    MIMES.add( new MimeType( "jfif", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jfif", "image/pjpeg", true ) );
    MIMES.add( new MimeType( "jfif-tbnl", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jpe", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jpe", "image/pjpeg", true ) );
    MIMES.add( new MimeType( "jpeg", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jpeg", "image/pjpeg", true ) );
    MIMES.add( new MimeType( "jpg", "image/jpg", true ) );
    MIMES.add( new MimeType( "jpg", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jpg", "image/jpeg", true ) );
    MIMES.add( new MimeType( "jpg", "image/pjpeg", true ) );
    MIMES.add( new MimeType( "jps", "image/x-jps", true ) );
    MIMES.add( new MimeType( "js", "application/javascript", false ) );
    MIMES.add( new MimeType( "js", "text/javascript", false ) );
    MIMES.add( new MimeType( "js", "application/ecmascript", false ) );
    MIMES.add( new MimeType( "js", "application/x-javascript", false ) );
    MIMES.add( new MimeType( "js", "text/ecmascript", false ) );
    MIMES.add( new MimeType( "jut", "image/jutvision", true ) );
    MIMES.add( new MimeType( "kar", "audio/midi", true ) );
    MIMES.add( new MimeType( "kar", "music/x-karaoke", true ) );
    MIMES.add( new MimeType( "ksh", "application/x-ksh", false ) );
    MIMES.add( new MimeType( "ksh", "text/plain", false ) );
    MIMES.add( new MimeType( "ksh", "text/x-script.ksh", false ) );
    MIMES.add( new MimeType( "la", "audio/nspaudio", true ) );
    MIMES.add( new MimeType( "la", "audio/x-nspaudio", true ) );
    MIMES.add( new MimeType( "lam", "audio/x-liveaudio", true ) );
    MIMES.add( new MimeType( "latex", "application/x-latex", true ) );
    MIMES.add( new MimeType( "lha", "application/lha", true ) );
    MIMES.add( new MimeType( "lha", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "lha", "application/x-lha", true ) );
    MIMES.add( new MimeType( "lhx", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "list", "text/plain", false ) );
    MIMES.add( new MimeType( "lma", "audio/nspaudio", true ) );
    MIMES.add( new MimeType( "lma", "audio/x-nspaudio", true ) );
    MIMES.add( new MimeType( "log", "text/plain", false ) );
    MIMES.add( new MimeType( "lsp", "application/x-lisp", true ) );
    MIMES.add( new MimeType( "lsp", "text/x-script.lisp", false ) );
    MIMES.add( new MimeType( "lst", "text/plain", false ) );
    MIMES.add( new MimeType( "lsx", "text/x-la-asf", false ) );
    MIMES.add( new MimeType( "ltx", "application/x-latex", true ) );
    MIMES.add( new MimeType( "lzh", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "lzh", "application/x-lzh", true ) );
    MIMES.add( new MimeType( "lzx", "application/lzx", true ) );
    MIMES.add( new MimeType( "lzx", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "lzx", "application/x-lzx", true ) );
    MIMES.add( new MimeType( "m", "text/plain", false ) );
    MIMES.add( new MimeType( "m", "text/x-m", false ) );
    MIMES.add( new MimeType( "m1v", "video/mpeg", true ) );
    MIMES.add( new MimeType( "m2a", "audio/mpeg", true ) );
    MIMES.add( new MimeType( "m2v", "video/mpeg", true ) );
    MIMES.add( new MimeType( "m3u", "audio/x-mpequrl", true ) );
    MIMES.add( new MimeType( "man", "application/x-troff-man", true ) );
    MIMES.add( new MimeType( "map", "application/x-navimap", true ) );
    MIMES.add( new MimeType( "mar", "text/plain", false ) );
    MIMES.add( new MimeType( "mbd", "application/mbedlet", true ) );
    MIMES.add( new MimeType( "mc$", "application/x-magic-cap-package-1.0", true ) );
    MIMES.add( new MimeType( "mcd", "application/mcad", true ) );
    MIMES.add( new MimeType( "mcd", "application/x-mathcad", true ) );
    MIMES.add( new MimeType( "mcf", "image/vasa", true ) );
    MIMES.add( new MimeType( "mcf", "text/mcf", false ) );
    MIMES.add( new MimeType( "mcp", "application/netmc", true ) );
    MIMES.add( new MimeType( "mdb", "application/msaccess", true ) );
    MIMES.add( new MimeType( "me", "application/x-troff-me", true ) );
    MIMES.add( new MimeType( "mht", "message/rfc822", true ) );
    MIMES.add( new MimeType( "mhtml", "message/rfc822", true ) );
    MIMES.add( new MimeType( "mid", "application/x-midi", true ) );
    MIMES.add( new MimeType( "mid", "audio/midi", true ) );
    MIMES.add( new MimeType( "mid", "audio/x-mid", true ) );
    MIMES.add( new MimeType( "mid", "audio/x-midi", true ) );
    MIMES.add( new MimeType( "mid", "music/crescendo", true ) );
    MIMES.add( new MimeType( "mid", "x-music/x-midi", true ) );
    MIMES.add( new MimeType( "midi", "application/x-midi", true ) );
    MIMES.add( new MimeType( "midi", "audio/midi", true ) );
    MIMES.add( new MimeType( "midi", "audio/x-mid", true ) );
    MIMES.add( new MimeType( "midi", "audio/x-midi", true ) );
    MIMES.add( new MimeType( "midi", "music/crescendo", true ) );
    MIMES.add( new MimeType( "midi", "x-music/x-midi", true ) );
    MIMES.add( new MimeType( "mif", "application/x-frame", true ) );
    MIMES.add( new MimeType( "mif", "application/x-mif", true ) );
    MIMES.add( new MimeType( "mime", "message/rfc822", true ) );
    MIMES.add( new MimeType( "mime", "www/mime", true ) );
    MIMES.add( new MimeType( "mjf", "audio/x-vnd.audioexplosion.mjuicemediafile", true ) );
    MIMES.add( new MimeType( "mjpg", "video/x-motion-jpeg", true ) );
    MIMES.add( new MimeType( "mm", "application/base64", true ) );
    MIMES.add( new MimeType( "mm", "application/x-meme", true ) );
    MIMES.add( new MimeType( "mme", "application/base64", true ) );
    MIMES.add( new MimeType( "mod", "audio/mod", true ) );
    MIMES.add( new MimeType( "mod", "audio/x-mod", true ) );
    MIMES.add( new MimeType( "moov", "video/quicktime", true ) );
    MIMES.add( new MimeType( "mov", "video/quicktime", true ) );
    MIMES.add( new MimeType( "movie", "video/x-sgi-movie", true ) );
    MIMES.add( new MimeType( "mp2", "audio/mpeg", true ) );
    MIMES.add( new MimeType( "mp2", "audio/x-mpeg", true ) );
    MIMES.add( new MimeType( "mp2", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mp2", "video/x-mpeg", true ) );
    MIMES.add( new MimeType( "mp2", "video/x-mpeq2a", true ) );
    MIMES.add( new MimeType( "mp3", "audio/mpeg", true ) );
    MIMES.add( new MimeType( "mp3", "audio/mpeg3", true ) );
    MIMES.add( new MimeType( "mp3", "audio/x-mpeg", true ) );
    MIMES.add( new MimeType( "mp3", "audio/x-mpeg-3", true ) );
    MIMES.add( new MimeType( "mp3", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mp3", "video/x-mpeg", true ) );
    MIMES.add( new MimeType( "mp4", "video/mp4", true ) );
    MIMES.add( new MimeType( "mp4", "video/mp4", true ) );
    MIMES.add( new MimeType( "mpa", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mpc", "application/x-project", true ) );
    MIMES.add( new MimeType( "mpe", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mpeg", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mpg", "audio/mpeg", true ) );
    MIMES.add( new MimeType( "mpg", "video/mpeg", true ) );
    MIMES.add( new MimeType( "mpga", "audio/mpeg", true ) );
    MIMES.add( new MimeType( "mpp", "application/msproject", true ) );
    MIMES.add( new MimeType( "mpp", "application/vnd.ms-project", true ) );
    MIMES.add( new MimeType( "mpt", "application/x-project", true ) );
    MIMES.add( new MimeType( "mpv", "application/x-project", true ) );
    MIMES.add( new MimeType( "mpx", "application/x-project", true ) );
    MIMES.add( new MimeType( "mrc", "application/marc", true ) );
    MIMES.add( new MimeType( "ms", "application/x-troff-ms", true ) );
    MIMES.add( new MimeType( "ms", "application/x-troff-ms", true ) );
    MIMES.add( new MimeType( "ms", "application/x-troff-ms", true ) );
    MIMES.add( new MimeType( "mv", "video/x-sgi-movie", true ) );
    MIMES.add( new MimeType( "my", "audio/make", true ) );
    MIMES.add( new MimeType( "mzz", "application/x-vnd.audioexplosion.mzz", true ) );
    MIMES.add( new MimeType( "nap", "image/naplps", true ) );
    MIMES.add( new MimeType( "naplps", "image/naplps", true ) );
    MIMES.add( new MimeType( "nc", "application/x-netcdf", true ) );
    MIMES.add( new MimeType( "nc", "application/x-netcdf", true ) );
    MIMES.add( new MimeType( "nc", "application/x-netcdf", true ) );
    MIMES.add( new MimeType( "ncm", "application/vnd.nokia.configuration-message", true ) );
    MIMES.add( new MimeType( "nif", "image/x-niff", true ) );
    MIMES.add( new MimeType( "niff", "image/x-niff", true ) );
    MIMES.add( new MimeType( "nix", "application/x-mix-transfer", true ) );
    MIMES.add( new MimeType( "nsc", "application/x-conference", true ) );
    MIMES.add( new MimeType( "nvd", "application/x-navidoc", true ) );
    MIMES.add( new MimeType( "nws", "message/rfc822", true ) );
    MIMES.add( new MimeType( "o", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "obj", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "oda", "application/oda", true ) );
    MIMES.add( new MimeType( "ogg", "video/ogg", true ) );
    MIMES.add( new MimeType( "omc", "application/x-omc", true ) );
    MIMES.add( new MimeType( "omcd", "application/x-omcdatamaker", true ) );
    MIMES.add( new MimeType( "omcr", "application/x-omcregerator", true ) );
    MIMES.add( new MimeType( "p", "text/x-pascal", false ) );
    MIMES.add( new MimeType( "p10", "application/pkcs10", true ) );
    MIMES.add( new MimeType( "p10", "application/x-pkcs10", true ) );
    MIMES.add( new MimeType( "p12", "application/pkcs-12", true ) );
    MIMES.add( new MimeType( "p12", "application/x-pkcs12", true ) );
    MIMES.add( new MimeType( "p7a", "application/x-pkcs7-signature", true ) );
    MIMES.add( new MimeType( "p7c", "application/pkcs7-mime", true ) );
    MIMES.add( new MimeType( "p7c", "application/x-pkcs7-mime", true ) );
    MIMES.add( new MimeType( "p7m", "application/pkcs7-mime", true ) );
    MIMES.add( new MimeType( "p7m", "application/x-pkcs7-mime", true ) );
    MIMES.add( new MimeType( "p7r", "application/x-pkcs7-certreqresp", true ) );
    MIMES.add( new MimeType( "p7s", "application/pkcs7-signature", true ) );
    MIMES.add( new MimeType( "part", "application/pro_eng", true ) );
    MIMES.add( new MimeType( "part", "application/pro_eng", true ) );
    MIMES.add( new MimeType( "pas", "text/pascal", false ) );
    MIMES.add( new MimeType( "pbm", "image/x-portable-bitmap", true ) );
    MIMES.add( new MimeType( "pbm", "image/x-portable-bitmap", true ) );
    MIMES.add( new MimeType( "pbm", "image/x-portable-bitmap", true ) );
    MIMES.add( new MimeType( "pcl", "application/vnd.hp-pcl", true ) );
    MIMES.add( new MimeType( "pcl", "application/x-pcl", true ) );
    MIMES.add( new MimeType( "pct", "image/x-pict", true ) );
    MIMES.add( new MimeType( "pcx", "image/x-pcx", true ) );
    MIMES.add( new MimeType( "pdb", "chemical/x-pdb", true ) );
    MIMES.add( PDF );
    MIMES.add( new MimeType( "pfunk", "audio/make", true ) );
    MIMES.add( new MimeType( "pfunk", "audio/make.my.funk", true ) );
    MIMES.add( new MimeType( "pfx", "application/x-pkcs12", true ) );
    MIMES.add( new MimeType( "pgm", "image/x-portable-greymap", true ) );
    MIMES.add( new MimeType( "pic", "image/pict", true ) );
    MIMES.add( new MimeType( "pict", "image/pict", true ) );
    MIMES.add( new MimeType( "pkg", "application/x-newton-compatible-pkg", true ) );
    MIMES.add( new MimeType( "pko", "application/vnd.ms-pki.pko", true ) );
    MIMES.add( new MimeType( "pl", "text/plain", false ) );
    MIMES.add( new MimeType( "pl", "text/x-script.perl", false ) );
    MIMES.add( new MimeType( "plx", "application/x-pixclscript", true ) );
    MIMES.add( new MimeType( "pm", "image/x-xpixmap", true ) );
    MIMES.add( new MimeType( "pm", "text/x-script.perl-module", false ) );
    MIMES.add( new MimeType( "pm4", "application/x-pagemaker", true ) );
    MIMES.add( new MimeType( "pm5", "application/x-pagemaker", true ) );
    MIMES.add( new MimeType( "png", "image/png", true ) );
    MIMES.add( new MimeType( "pnm", "application/x-portable-anymap", true ) );
    MIMES.add( new MimeType( "pnm", "image/x-portable-anymap", true ) );
    MIMES.add( new MimeType( "pot", "application/mspowerpoint", true ) );
    MIMES.add( new MimeType( "pot", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "pov", "model/x-pov", true ) );
    MIMES.add( new MimeType( "ppa", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "ppm", "image/x-portable-pixmap", true ) );
    MIMES.add( new MimeType( "pps", "application/mspowerpoint", true ) );
    MIMES.add( new MimeType( "pps", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "ppt", "application/mspowerpoint", true ) );
    MIMES.add( new MimeType( "ppt", "application/powerpoint", true ) );
    MIMES.add( new MimeType( "ppt", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "ppt", "application/x-mspowerpoint", true ) );
    MIMES.add( new MimeType( "ppz", "application/mspowerpoint", true ) );
    MIMES.add( new MimeType( "pre", "application/x-freelance", true ) );
    MIMES.add( new MimeType( "prt", "application/pro_eng", true ) );
    MIMES.add( new MimeType( "ps", "application/postscript", true ) );
    MIMES.add( new MimeType( "psd", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "pvu", "paleovu/x-pv", true ) );
    MIMES.add( new MimeType( "pwz", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "pwz", "application/vnd.ms-powerpoint", true ) );
    MIMES.add( new MimeType( "py", "text/x-python", false ) );
    MIMES.add( new MimeType( "py", "text/x-script.phyton", false ) );
    MIMES.add( new MimeType( "pyc", "application/x-bytecode.python", true ) );
    MIMES.add( new MimeType( "pyc", "application/x-python-code", true ) );
    MIMES.add( new MimeType( "pyo", "application/x-python-code", true ) );
    MIMES.add( new MimeType( "qcp", "audio/vnd.qcelp", true ) );
    MIMES.add( new MimeType( "qd3", "x-world/x-3dmf", true ) );
    MIMES.add( new MimeType( "qd3d", "x-world/x-3dmf", true ) );
    MIMES.add( new MimeType( "qif", "image/x-quicktime", true ) );
    MIMES.add( new MimeType( "qt", "video/quicktime", true ) );
    MIMES.add( new MimeType( "qtc", "video/x-qtc", true ) );
    MIMES.add( new MimeType( "qti", "image/x-quicktime", true ) );
    MIMES.add( new MimeType( "qtif", "image/x-quicktime", true ) );
    MIMES.add( new MimeType( "ra", "audio/x-pn-realaudio", true ) );
    MIMES.add( new MimeType( "ra", "audio/x-pn-realaudio-plugin", true ) );
    MIMES.add( new MimeType( "ra", "audio/x-realaudio", true ) );
    MIMES.add( new MimeType( "ram", "application/x-pn-realaudio", true ) );
    MIMES.add( new MimeType( "ram", "audio/x-pn-realaudio", true ) );
    MIMES.add( new MimeType( "ras", "application/x-cmu-raster", true ) );
    MIMES.add( new MimeType( "ras", "image/cmu-raster", true ) );
    MIMES.add( new MimeType( "ras", "image/x-cmu-raster", true ) );
    MIMES.add( new MimeType( "rast", "image/cmu-raster", true ) );
    MIMES.add( new MimeType( "rdf", "application/xml", true ) );
    MIMES.add( new MimeType( "rexx", "text/x-script.rexx", false ) );
    MIMES.add( new MimeType( "rf", "image/vnd.rn-realflash", true ) );
    MIMES.add( new MimeType( "rgb", "image/x-rgb", true ) );
    MIMES.add( new MimeType( "rm", "application/vnd.rn-realmedia", true ) );
    MIMES.add( new MimeType( "rm", "audio/x-pn-realaudio", true ) );
    MIMES.add( new MimeType( "rmi", "audio/mid", true ) );
    MIMES.add( new MimeType( "rmp", "audio/x-pn-realaudio", true ) );
    MIMES.add( new MimeType( "rmp", "audio/x-pn-realaudio-plugin", true ) );
    MIMES.add( new MimeType( "rng", "application/ringing-tones", true ) );
    MIMES.add( new MimeType( "rng", "application/vnd.nokia.ringing-tone", true ) );
    MIMES.add( new MimeType( "rnx", "application/vnd.rn-realplayer", true ) );
    MIMES.add( new MimeType( "roff", "application/x-troff", true ) );
    MIMES.add( new MimeType( "rp", "image/vnd.rn-realpix", true ) );
    MIMES.add( new MimeType( "rpm", "audio/x-pn-realaudio-plugin", true ) );
    MIMES.add( new MimeType( "rpm", "audio/x-pn-realaudio-plugin", true ) );
    MIMES.add( new MimeType( "rt", "text/richtext", false ) );
    MIMES.add( new MimeType( "rt", "text/vnd.rn-realtext", false ) );
    MIMES.add( new MimeType( "rtf", "application/rtf", false ) );
    MIMES.add( new MimeType( "rtf", "application/x-rtf", false ) );
    MIMES.add( new MimeType( "rtf", "text/richtext", false ) );
    MIMES.add( new MimeType( "rtx", "application/rtf", false ) );
    MIMES.add( new MimeType( "rtx", "text/richtext", false ) );
    MIMES.add( new MimeType( "rv", "video/vnd.rn-realvideo", true ) );
    MIMES.add( new MimeType( "s", "text/x-asm", false ) );
    MIMES.add( new MimeType( "s3m", "audio/s3m", true ) );
    MIMES.add( new MimeType( "saveme", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "sbk", "application/x-tbook", true ) );
    MIMES.add( new MimeType( "scm", "application/x-lotusscreencam", true ) );
    MIMES.add( new MimeType( "scm", "text/x-script.guile", false ) );
    MIMES.add( new MimeType( "scm", "text/x-script.scheme", false ) );
    MIMES.add( new MimeType( "scm", "video/x-scm", true ) );
    MIMES.add( new MimeType( "sdml", "text/plain", false ) );
    MIMES.add( new MimeType( "sdp", "application/sdp", true ) );
    MIMES.add( new MimeType( "sdp", "application/x-sdp", true ) );
    MIMES.add( new MimeType( "sdr", "application/sounder", true ) );
    MIMES.add( new MimeType( "sea", "application/sea", true ) );
    MIMES.add( new MimeType( "sea", "application/x-sea", true ) );
    MIMES.add( new MimeType( "set", "application/set", true ) );
    MIMES.add( new MimeType( "sgm", "text/sgml", false ) );
    MIMES.add( new MimeType( "sgm", "text/x-sgml", false ) );
    MIMES.add( new MimeType( "sgml", "text/sgml", false ) );
    MIMES.add( new MimeType( "sgml", "text/x-sgml", false ) );
    MIMES.add( new MimeType( "sh", "application/x-bsh", false ) );
    MIMES.add( new MimeType( "sh", "application/x-sh", false ) );
    MIMES.add( new MimeType( "sh", "application/x-shar", true ) );
    MIMES.add( new MimeType( "sh", "text/x-script.sh", false ) );
    MIMES.add( new MimeType( "shar", "application/x-bsh", true ) );
    MIMES.add( new MimeType( "shar", "application/x-shar", true ) );
    MIMES.add( new MimeType( "shtml", "text/html", false ) );
    MIMES.add( new MimeType( "shtml", "text/x-server-parsed-html", false ) );
    MIMES.add( new MimeType( "sid", "audio/x-psid", true ) );
    MIMES.add( new MimeType( "sit", "application/x-sit", true ) );
    MIMES.add( new MimeType( "sit", "application/x-stuffit", true ) );
    MIMES.add( new MimeType( "sit", "application/x-stuffit", true ) );
    MIMES.add( new MimeType( "skd", "application/x-koan", true ) );
    MIMES.add( new MimeType( "skm", "application/x-koan", true ) );
    MIMES.add( new MimeType( "skp", "application/x-koan", true ) );
    MIMES.add( new MimeType( "skt", "application/x-koan", true ) );
    MIMES.add( new MimeType( "sl", "application/x-seelogo", true ) );
    MIMES.add( new MimeType( "smi", "application/smil", true ) );
    MIMES.add( new MimeType( "smil", "application/smil", true ) );
    MIMES.add( new MimeType( "snd", "audio/basic", true ) );
    MIMES.add( new MimeType( "snd", "audio/x-adpcm", true ) );
    MIMES.add( new MimeType( "so", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "sol", "application/solids", true ) );
    MIMES.add( new MimeType( "spc", "application/x-pkcs7-certificates", true ) );
    MIMES.add( new MimeType( "spc", "text/x-speech", false ) );
    MIMES.add( new MimeType( "spl", "application/futuresplash", true ) );
    MIMES.add( new MimeType( "spr", "application/x-sprite", true ) );
    MIMES.add( new MimeType( "sprite", "application/x-sprite", true ) );
    MIMES.add( new MimeType( "src", "application/x-wais-source", true ) );
    MIMES.add( new MimeType( "ssi", "text/x-server-parsed-html", false ) );
    MIMES.add( new MimeType( "ssm", "application/streamingmedia", true ) );
    MIMES.add( new MimeType( "sst", "application/vnd.ms-pki.certstore", true ) );
    MIMES.add( new MimeType( "st", "application/STEP", true ) );
    MIMES.add( new MimeType( "step", "application/STEP", true ) );
    MIMES.add( new MimeType( "stl", "application/sla", true ) );
    MIMES.add( new MimeType( "stl", "application/vnd.ms-pki.stl", true ) );
    MIMES.add( new MimeType( "stl", "application/x-navistyle", true ) );
    MIMES.add( new MimeType( "stp", "application/STEP", true ) );
    MIMES.add( new MimeType( "stp", "application/step", true ) );
    MIMES.add( new MimeType( "sv4cpio", "application/x-sv4cpio", true ) );
    MIMES.add( new MimeType( "sv4crc", "application/x-sv4crc", true ) );
    MIMES.add( new MimeType( "sv4crc", "application/x-sv4crc", true ) );
    MIMES.add( new MimeType( "svf", "image/vnd.dwg", true ) );
    MIMES.add( new MimeType( "svf", "image/x-dwg", true ) );
    MIMES.add( new MimeType( "svr", "application/x-world", true ) );
    MIMES.add( new MimeType( "svr", "x-world/x-svr", true ) );
    MIMES.add( new MimeType( "swf", "application/x-shockwave-flash", true ) );
    MIMES.add( new MimeType( "t", "application/x-troff", true ) );
    MIMES.add( new MimeType( "talk", "text/x-speech", false ) );
    MIMES.add( new MimeType( "tar", "application/x-tar", true ) );
    MIMES.add( new MimeType( "tbk", "application/toolbook", true ) );
    MIMES.add( new MimeType( "tbk", "application/x-tbook", true ) );
    MIMES.add( new MimeType( "tcl", "application/x-tcl", false ) );
    MIMES.add( new MimeType( "tcl", "text/x-script.tcl", false ) );
    MIMES.add( new MimeType( "tcsh", "text/x-script.tcsh", false ) );
    MIMES.add( new MimeType( "tex", "application/x-tex", true ) );
    MIMES.add( new MimeType( "texi", "application/x-texinfo", true ) );
    MIMES.add( new MimeType( "texinfo", "application/x-texinfo", true ) );
    MIMES.add( new MimeType( "text", "application/plain", true ) );
    MIMES.add( TEXT );
    MIMES.add( new MimeType( "tgz", "application/gnutar", true ) );
    MIMES.add( new MimeType( "tgz", "application/x-compressed", true ) );
    MIMES.add( new MimeType( "tif", "image/tiff", true ) );
    MIMES.add( new MimeType( "tif", "image/x-tiff", true ) );
    MIMES.add( new MimeType( "tiff", "image/tiff", true ) );
    MIMES.add( new MimeType( "tiff", "image/x-tiff", true ) );
    MIMES.add( new MimeType( "tr", "application/x-troff", true ) );
    MIMES.add( new MimeType( "tsi", "audio/tsp-audio", true ) );
    MIMES.add( new MimeType( "tsp", "application/dsptype", true ) );
    MIMES.add( new MimeType( "tsp", "audio/tsplayer", true ) );
    MIMES.add( new MimeType( "tsv", "text/tab-separated-values", false ) );
    MIMES.add( new MimeType( "turbot", "image/florian", true ) );
    MIMES.add( new MimeType( "txt", "text/plain", false ) );
    MIMES.add( new MimeType( "uil", "text/x-uil", false ) );
    MIMES.add( new MimeType( "uni", "text/uri-list", false ) );
    MIMES.add( new MimeType( "unis", "text/uri-list", false ) );
    MIMES.add( new MimeType( "unv", "application/i-deas", true ) );
    MIMES.add( new MimeType( "uri", "text/uri-list", false ) );
    MIMES.add( new MimeType( "uris", "text/uri-list", false ) );
    MIMES.add( new MimeType( "ustar", "application/x-ustar", true ) );
    MIMES.add( new MimeType( "ustar", "multipart/x-ustar", true ) );
    MIMES.add( new MimeType( "uu", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "uu", "text/x-uuencode", false ) );
    MIMES.add( new MimeType( "uue", "text/x-uuencode", false ) );
    MIMES.add( new MimeType( "vcd", "application/x-cdlink", true ) );
    MIMES.add( new MimeType( "vcf", "text/x-vcard", false ) );
    MIMES.add( new MimeType( "vcs", "text/x-vcalendar", false ) );
    MIMES.add( new MimeType( "vda", "application/vda", true ) );
    MIMES.add( new MimeType( "vdo", "video/vdo", true ) );
    MIMES.add( new MimeType( "vew", "application/groupwise", true ) );
    MIMES.add( new MimeType( "viv", "video/vivo", true ) );
    MIMES.add( new MimeType( "viv", "video/vnd.vivo", true ) );
    MIMES.add( new MimeType( "vivo", "video/vivo", true ) );
    MIMES.add( new MimeType( "vivo", "video/vnd.vivo", true ) );
    MIMES.add( new MimeType( "vmd", "application/vocaltec-media-desc", true ) );
    MIMES.add( new MimeType( "vmf", "application/vocaltec-media-file", true ) );
    MIMES.add( new MimeType( "voc", "audio/voc", true ) );
    MIMES.add( new MimeType( "voc", "audio/x-voc", true ) );
    MIMES.add( new MimeType( "voc", "audio/x-voice", true ) );
    MIMES.add( new MimeType( "vos", "video/vosaic", true ) );
    MIMES.add( new MimeType( "vox", "audio/voxware", true ) );
    MIMES.add( new MimeType( "vqe", "audio/x-twinvq-plugin", true ) );
    MIMES.add( new MimeType( "vqf", "audio/x-twinvq", true ) );
    MIMES.add( new MimeType( "vql", "audio/x-twinvq-plugin", true ) );
    MIMES.add( new MimeType( "vrml", "application/x-vrml", true ) );
    MIMES.add( new MimeType( "vrml", "model/vrml", true ) );
    MIMES.add( new MimeType( "vrml", "x-world/x-vrml", true ) );
    MIMES.add( new MimeType( "vrt", "x-world/x-vrt", true ) );
    MIMES.add( new MimeType( "vrt", "x-world/x-vrt", true ) );
    MIMES.add( new MimeType( "vsd", "application/x-visio", true ) );
    MIMES.add( new MimeType( "vst", "application/x-visio", true ) );
    MIMES.add( new MimeType( "vsw", "application/x-visio", true ) );
    MIMES.add( new MimeType( "w60", "application/wordperfect6.0", true ) );
    MIMES.add( new MimeType( "w61", "application/wordperfect6.1", true ) );
    MIMES.add( new MimeType( "w6w", "application/msword", true ) );
    MIMES.add( new MimeType( "w6w", "application/msword", true ) );
    MIMES.add( new MimeType( "wav", "audio/wav", true ) );
    MIMES.add( new MimeType( "wav", "audio/x-wav", true ) );
    MIMES.add( new MimeType( "wav", "audio/x-wav", true ) );
    MIMES.add( new MimeType( "wb1", "application/x-qpro", true ) );
    MIMES.add( new MimeType( "wbmp", "image/vnd.wap.wbmp", true ) );
    MIMES.add( new MimeType( "web", "application/vnd.xara", true ) );
    MIMES.add( new MimeType( "webm", "video/webm", true ) );
    MIMES.add( new MimeType( "wiz", "application/msword", true ) );
    MIMES.add( new MimeType( "wiz", "application/msword", true ) );
    MIMES.add( new MimeType( "wk1", "application/x-123", true ) );
    MIMES.add( new MimeType( "wmf", "windows/metafile", true ) );
    MIMES.add( new MimeType( "wml", "text/vnd.wap.wml", false ) );
    MIMES.add( new MimeType( "wmlc", "application/vnd.wap.wmlc", true ) );
    MIMES.add( new MimeType( "wmls", "text/vnd.wap.wmlscript", false ) );
    MIMES.add( new MimeType( "wmlsc", "application/vnd.wap.wmlscriptc", true ) );
    MIMES.add( new MimeType( "word", "application/msword", true ) );
    MIMES.add( new MimeType( "wp", "application/wordperfect", true ) );
    MIMES.add( new MimeType( "wp5", "application/wordperfect", true ) );
    MIMES.add( new MimeType( "wp5", "application/wordperfect6.0", true ) );
    MIMES.add( new MimeType( "wp6", "application/wordperfect", true ) );
    MIMES.add( new MimeType( "wpd", "application/wordperfect", true ) );
    MIMES.add( new MimeType( "wpd", "application/x-wpwin", true ) );
    MIMES.add( new MimeType( "wq1", "application/x-lotus", true ) );
    MIMES.add( new MimeType( "wri", "application/mswrite", true ) );
    MIMES.add( new MimeType( "wri", "application/x-wri", true ) );
    MIMES.add( new MimeType( "wrl", "application/x-world", true ) );
    MIMES.add( new MimeType( "wrl", "model/vrml", true ) );
    MIMES.add( new MimeType( "wrl", "x-world/x-vrml", true ) );
    MIMES.add( new MimeType( "wrz", "model/vrml", true ) );
    MIMES.add( new MimeType( "wrz", "x-world/x-vrml", true ) );
    MIMES.add( new MimeType( "wsc", "text/scriplet", false ) );
    MIMES.add( new MimeType( "wsdl", "application/xml", true ) );
    MIMES.add( new MimeType( "wsrc", "application/x-wais-source", true ) );
    MIMES.add( new MimeType( "wtk", "application/x-wintalk", true ) );
    MIMES.add( new MimeType( "x-png", "image/png", true ) );
    MIMES.add( new MimeType( "xbm", "image/x-xbitmap", true ) );
    MIMES.add( new MimeType( "xbm", "image/x-xbm", true ) );
    MIMES.add( new MimeType( "xbm", "image/xbm", true ) );
    MIMES.add( new MimeType( "xdr", "video/x-amt-demorun", true ) );
    MIMES.add( new MimeType( "xgz", "xgl/drawing", true ) );
    MIMES.add( new MimeType( "xif", "image/vnd.xiff", true ) );
    MIMES.add( new MimeType( "xl", "application/excel", true ) );
    MIMES.add( new MimeType( "xla", "application/excel", true ) );
    MIMES.add( new MimeType( "xla", "application/msexcel", true ) );
    MIMES.add( new MimeType( "xla", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xla", "application/x-msexcel", true ) );
    MIMES.add( new MimeType( "xlb", "application/excel", true ) );
    MIMES.add( new MimeType( "xlb", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xlb", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlc", "application/excel", true ) );
    MIMES.add( new MimeType( "xlc", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xlc", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xld", "application/excel", true ) );
    MIMES.add( new MimeType( "xld", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlk", "application/excel", true ) );
    MIMES.add( new MimeType( "xlk", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xll", "application/excel", true ) );
    MIMES.add( new MimeType( "xll", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xll", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlm", "application/excel", true ) );
    MIMES.add( new MimeType( "xlm", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xlm", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xls", "application/excel", true ) );
    MIMES.add( new MimeType( "xls", "application/excel", true ) );
    MIMES.add( new MimeType( "xls", "application/msexcel", true ) );
    MIMES.add( new MimeType( "xls", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xls", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xls", "application/x-msexcel", true ) );
    MIMES.add( new MimeType( "xlt", "application/excel", true ) );
    MIMES.add( new MimeType( "xlt", "application/msexcel", true ) );
    MIMES.add( new MimeType( "xlt", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlv", "application/excel", true ) );
    MIMES.add( new MimeType( "xlv", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlw", "application/excel", true ) );
    MIMES.add( new MimeType( "xlw", "application/msexcel", true ) );
    MIMES.add( new MimeType( "xlw", "application/vnd.ms-excel", true ) );
    MIMES.add( new MimeType( "xlw", "application/x-excel", true ) );
    MIMES.add( new MimeType( "xlw", "application/x-msexcel", true ) );
    MIMES.add( new MimeType( "xm", "audio/xm", true ) );
    MIMES.add( new MimeType( "xml", "text/xml", false ) );
    MIMES.add( SOAP );
    MIMES.add( new MimeType( "xmz", "xgl/movie", true ) );
    MIMES.add( new MimeType( "xpdl", "application/xml", true ) );
    MIMES.add( new MimeType( "xpix", "application/x-vnd.ls-xpix", true ) );
    MIMES.add( new MimeType( "xpm", "image/x-xpixmap", true ) );
    MIMES.add( new MimeType( "xpm", "image/xpm", true ) );
    MIMES.add( new MimeType( "xsl", "application/xml", true ) );
    MIMES.add( new MimeType( "xsr", "video/x-amt-showrun", true ) );
    MIMES.add( new MimeType( "xwd", "image/x-xwd", true ) );
    MIMES.add( new MimeType( "xwd", "image/x-xwindowdump", true ) );
    MIMES.add( new MimeType( "xyz", "chemical/x-pdb", true ) );
    MIMES.add( new MimeType( "z", "application/x-compress", true ) );
    MIMES.add( new MimeType( "z", "application/x-compressed", true ) );
    MIMES.add( new MimeType( "zip", "application/x-compressed", true ) );
    MIMES.add( new MimeType( "zip", "application/zip", true ) );
    MIMES.add( new MimeType( "zip", "multipart/x-zip", true ) );
    MIMES.add( new MimeType( "zoo", "application/octet-stream", true ) );
    MIMES.add( new MimeType( "zsh", "text/x-script.zsh", false ) );
  }




  private MimeType( String extension, String type, boolean binary ) {
    this.extension = extension;
    this.type = type;
    this.binary = binary;
  }




  /**
   * @return the extension associated with this type
   */
  public String getExtension() {
    return extension;
  }




  /**
   * @return the MIME type string to place in the header
   */
  public String getType() {
    return type;
  }




  /**
   * @return true if the MIME type represents a binary type binary
   */
  public boolean isBinary() {
    return binary;
  }




  /**
   * Return a list of MIME types for the given file name (extension).
   * 
   * <p>There are often more than one MIME type for a file extension.</p>
   * 
   * <p>If the MIME type could not be determined, then the returned list will 
   * contain only the "unknown" MIME type.</p>
   * 
   * @param fileName The name of the file from which to retrieve the extension.
   * 
   * @return A list of MIME types which are applicable for this file extension.
   */
  public static List<MimeType> get( String fileName ) {
    synchronized( MIMES ) {

      List<MimeType> retval = new ArrayList<MimeType>();

      String key = fileName.toLowerCase();

      int sep = key.lastIndexOf( '.' );
      String ext = null;
      if ( sep > 0 ) {
        ext = key.substring( sep + 1 );
      } else {
        ext = key;
      }

      for ( MimeType mime : MIMES ) {
        if ( mime.extension.equals( ext ) ) {
          retval.add( mime );
        }
      }

      if ( retval.size() == 0 ) {
        retval.add( UNKNOWN );
      }

      return retval;
    }
  }




  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer b = new StringBuffer( "MIME: " );
    b.append( this.extension );
    b.append( " : " );
    b.append( this.type );
    b.append( " : " );
    if ( this.binary ) {
      b.append( "BINARY" );
    } else {
      b.append( "TEXT" );
    }

    return b.toString();
  }




  /**
   * Add the given extension to type mapping to this set of MIME types.
   * 
   * <p>If either the extension or the type is blank (null, empty or all 
   * whitespace) the operation is silently ignored.</p>
   * 
   * @param extension Not blank extension without the delimiting dot (e.g. "html")
   * @param type the MIME text to use (e.g. "text/html")
   * @param binary flag indicating if the stream is binary (true) or text (false)
   */
  public static void add( String extension, String type, boolean binary ) {
    synchronized( MIMES ) {
      if ( StringUtil.isNotBlank( extension ) && StringUtil.isNotBlank( type ) ) {
        MIMES.add( new MimeType( extension.trim().toLowerCase(), type.trim().toLowerCase(), binary ) );
      }
    }
  }




  /**
   * Remove ALL mappings of this extension to any existing MIME types.
   * 
   * <p>The purpose of this is to provide a way to ensure that a given 
   * extension returns only the required MIME types and not any of the existing 
   * standard types. It is quite common for a single extension to return 
   * multiple MIME types. An extension of "zip" can be and is often mapped to 
   * "application/x-compressed", "application/zip", and "multipart/x-zip". This 
   * method allows for the mapping of only one MIME type by removing all the 
   * existing mappings and then calling {@code add( String, String, boolean )} 
   * to add the desired type.</p>    
   * 
   * <p>If the extension is blank (null, empty or all whitespace) the operation 
   * is silently ignored.</p>
   * 
   * @param extension Not blank extension without the delimiting dot (e.g. "html")
   */
  public static void remove( String extension ) {
    synchronized( MIMES ) {
      if ( StringUtil.isNotBlank( extension ) ) {
        String ext = extension.trim().toLowerCase();

        ListIterator<MimeType> iter = MIMES.listIterator();
        while ( iter.hasNext() ) {
          if ( iter.next().getExtension().equals( ext ) ) {
            iter.remove();
          }
        }
      }
    }
  }




  /**
   * Tests if the MIME Types are equivalent.
   * 
   * <p>True if both are Mime Types and their type value is the same. This 
   * does <strong>not</strong> check for the value of the binary flag nor the 
   * file extension, just the resulting type. so a MIME Type retrieve for 
   * "html" will equal a MIME Type for "htm" and "htmls" as they all will 
   * return "text/html" as their type. 
   *   
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj ) {
    if ( obj != null ) {
      if ( obj instanceof MimeType ) {
        if ( this.getType().equals( ( (MimeType)obj ).getType() ) ) {
          return true;
        }
      }
    }
    return false;
  }

}
