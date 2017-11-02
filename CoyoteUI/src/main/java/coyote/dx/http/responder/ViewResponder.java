/*
 * Copyright (c) 2017 Stephan D. Cote' - All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the MIT License which accompanies this distribution, and is 
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import coyote.commons.StringUtil;
import coyote.commons.network.MimeType;
import coyote.commons.network.http.IHTTPSession;
import coyote.commons.network.http.IStatus;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.Status;
import coyote.commons.network.http.responder.DefaultResponder;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.loader.log.Log;


/**
 * 
 */
public abstract class ViewResponder extends DefaultResponder implements Responder {
  private static final String HTML_EXTENSION = ".html";
  private static final String TEMPLATE_DIRECTORY = "views/";
  protected Status status = Status.OK;
  protected String template = "";
  protected MimeType mimetype = MimeType.HTML;
  private SymbolTable symbolTable = new SymbolTable();




  @Override
  public Response delete(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response get(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response other(String method, Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response post(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response put(Resource resource, Map<String, String> urlParams, IHTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public IStatus getStatus() {
    return status;
  }




  @Override
  public String getText() {
    return Template.resolve(template, symbolTable);
  }




  @Override
  public String getMimeType() {
    return mimetype.getType();
  }




  /**
   * @param status the status to set
   */
  protected void setStatus(Status status) {
    this.status = status;
  }




  /**
   * @param type the mimetype to set
   */
  protected void setMimetype(MimeType type) {
    mimetype = type;
  }




  /**
   * Merge the given symbol table into our table.
   * @param symbols the table to merge into our table 
   */
  protected void mergeSymbols(SymbolTable symbols) {
    symbolTable.merge(symbols);
  }




  /**
   * Replace our symbol table with the given table
   * @param symbols the symbol table to use in this responder.
   */
  protected void setSymbols(SymbolTable symbols) {
    symbolTable = symbols;
  }




  protected SymbolTable getSymbolTable() {
    return symbolTable;
  }




  /**
   * @param name
   */
  protected void loadTemplate(String name) {
    String resource = TEMPLATE_DIRECTORY + name;
    String result = loadResource(resource);

    if (result == null) {
      resource = TEMPLATE_DIRECTORY + name + HTML_EXTENSION;
      result = loadResource(resource);
      if (result == null) {
        resource = TEMPLATE_DIRECTORY + name.toLowerCase();
        result = loadResource(resource);
      }
      if (result == null) {
        resource = TEMPLATE_DIRECTORY + name.toLowerCase() + HTML_EXTENSION;
        result = loadResource(resource);
      }
    }

    if (result != null) {
      template = result;
    }
  }




  private String loadResource(String name) {
    String resource = name;
    String retval = null;

    // From ClassLoader, all paths are "absolute" already - there's no context
    // from which they could be relative. Therefore you don't need a leading slash.
    ClassLoader cLoader = this.getClass().getClassLoader();
    InputStream inputStream = cLoader.getResourceAsStream(resource);
    if (inputStream != null) {
      final int bufferSize = 1024;
      final char[] buffer = new char[bufferSize];
      final StringBuilder out = new StringBuilder();
      try {
        Reader in = new InputStreamReader(inputStream, StringUtil.ISO8859_1);
        for (;;) {
          int rsz = in.read(buffer, 0, buffer.length);
          if (rsz < 0)
            break;
          out.append(buffer, 0, rsz);
        }
        retval = out.toString();
      } catch (IOException e) {
        Log.error("Could not load template from " + resource, e);
      }
    } else {
      Log.error("Could not find template in " + resource);
    }
    return retval;
  }

}