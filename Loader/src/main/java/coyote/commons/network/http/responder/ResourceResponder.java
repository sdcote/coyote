/*
 * Copyright (c) 2003 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */

package coyote.commons.network.http.responder;

import java.net.URL;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.HTTPD;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.loader.log.Log;


/**
 * Serves resources from the class path and not the file system.
 *
 * <p>All data must be a resource in the class path. This responder does not
 * serve anything which is not in the packaged application. This is arguably
 * more secure than serving from the file system in that the user does not have
 * the opportunity to introduce sensitive data or links to other resources.
 *
 * <p>The root of the class path uses is "content" by default but can be
 * changed
 *
 * addRoute( "/(.)+", ResourceResponder.class, "content" );
 *
 */
public class ResourceResponder extends DefaultResponder {

  private static final String DEFAULT_ROOT = "content";
  private boolean redirectOnIndexedDir = false;

  // The class loader object associated with this Class
  ClassLoader cLoader = this.getClass().getClassLoader();




  @Override
  public Response get(final Resource resource, final Map<String, String> urlParams, final IHTTPSession session) {

    final String baseUri = resource.getUri(); // the regex matcher URL

    String coreRequest = HTTPDRouter.normalizeUri(session.getUri());

    // find the portion of the URI which differs from the base
    for (int index = 0; index < Math.min(baseUri.length(), coreRequest.length()); index++) {
      if (baseUri.charAt(index) != coreRequest.charAt(index)) {
        coreRequest = HTTPDRouter.normalizeUri(coreRequest.substring(index));
        break;
      }
    }

    // Retrieve the base directory in the classpath for our search
    String parentdirectory = resource.initParameter(0, String.class);
    try {
      if (StringUtil.isBlank(parentdirectory)) {
        parentdirectory = DEFAULT_ROOT;
      }
    } catch (final Exception e) {
      Log.append(HTTPD.EVENT, "ResourceResponder initialization error: Parent Directory: " + e.getMessage() + " - defaulting to '" + DEFAULT_ROOT + "'");
    }

    // Check if we should send a 301 redirect when the request is for a
    // directory and we found an index file in that location which can be
    // served instead
    if (resource.getInitParameterLength() > 1) {
      try {
        redirectOnIndexedDir = resource.initParameter(1, Boolean.class);
      } catch (final Exception e) {
        Log.append(HTTPD.EVENT, "ResourceResponder initialization error: Redirect On Indexed Directory: " + e.getMessage() + " - defaulting to true");
      }
    }

    // make sure we are configured with a properly formatted parent directory
    if (!parentdirectory.endsWith("/")) {
      parentdirectory = parentdirectory.concat("/");
    }
    if (parentdirectory.startsWith("/")) {
      parentdirectory = parentdirectory.substring(1);
    }

    // add our configured parent directory to the real request. This is the
    // actual local resource for which we are looking:
    String localPath = parentdirectory + coreRequest;

    // A blank request or one ending with a path delimiter indicates a request
    // for our root or some other directory; see if there is an index file in
    // the requested directory and send a (301) redirect if so.
    if (StringUtil.isBlank(coreRequest) || coreRequest.endsWith("/")) {
      localPath = getDirectoryIndexRequest(localPath);

      // If we did not get a new local path, it means there is no index file in
      // the directory
      if (StringUtil.isBlank(localPath)) {
        if (StringUtil.isBlank(coreRequest)) {
          Log.append(HTTPD.EVENT, "There does not appear to be an index file in the content root (" + parentdirectory + ") of the classpath.");
        }
        Log.append(HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "'");
        return new Error404Responder().get(resource, urlParams, session);
      } else {
        if (redirectOnIndexedDir) {
          // We need to send a 301, indicating the new (proper) URL to use
          final String redirectlocation = localPath.replace(parentdirectory.substring(0, parentdirectory.length() - 1), ""); // YUCK!!!
          final Response redirect = Response.createFixedLengthResponse(Status.REDIRECT, MimeType.TEXT.getType(), null);
          redirect.addHeader("Location", redirectlocation);
          return redirect;
        } else {
          // hide the fact that we are serving the index page and just serve
          // the index page
          try {
            return Response.createChunkedResponse(Status.OK, HTTPD.getMimeTypeForFile(localPath), cLoader.getResourceAsStream(localPath));
          } catch (final Exception ioe) {
            return Response.createFixedLengthResponse(Status.REQUEST_TIMEOUT, MimeType.TEXT.getType(), null);
          }
        }
      }
    } else {
      // See if the requested resource exists
      final URL rsc = cLoader.getResource(localPath);

      // if we have no URL, the class loader could not find the resource
      if (rsc == null) {
        Log.append(HTTPD.EVENT, "404 NOT FOUND - '" + coreRequest + "' LOCAL: " + localPath);
        return new Error404Responder().get(resource, urlParams, session);
      } else {
        // Success - Found the resource -
        try {
          return Response.createChunkedResponse(Status.OK, HTTPD.getMimeTypeForFile(localPath), cLoader.getResourceAsStream(localPath));
        } catch (final Exception ioe) {
          return Response.createFixedLengthResponse(Status.REQUEST_TIMEOUT, MimeType.TEXT.getType(), null);
        }
      }
    }
  }




  @Override
  public String getMimeType() {
    return MimeType.TEXT.getType(); // this should never be called
  }




  @Override
  public IStatus getStatus() {
    return Status.INTERNAL_ERROR; // this should never be called
  }




  @Override
  public String getText() {
    return Status.INTERNAL_ERROR.getDescription(); // this should never be called
  }




  /**
   * Treat the given request path as a directory and try different options to pull an index request.
   *
   * @param path
   *
   * @return the new request which will return one of the index files
   */
  private String getDirectoryIndexRequest(String path) {
    if (StringUtil.isBlank(path)) {
      path = "/";
    }
    if (!path.endsWith("/")) {
      path = path.concat("/");
    }

    // look for the standard index file name
    String retval = path.concat("index.html");
    if (cLoader.getResource(retval) != null) {
      return retval; // found it
    } else {
      retval = path.concat("index.htm");
      if (cLoader.getResource(retval) != null) {
        return retval; // found DOS index file
      } else {
        return null; // did not find either
      }
    }
  }

}