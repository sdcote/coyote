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
import coyote.commons.network.http.HTTPSession;
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
  private Status status = Status.OK;
  private String template = "";
  private MimeType mimetype = MimeType.HTML;
  private SymbolTable symbolTable = new SymbolTable();
  private boolean preProcessing = false;




  @Override
  public Response delete(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response get(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }


  /**
   * Resolve the template with our current symbol table.
   *
   * @return the resolved template.
   */
  @Override
  public String getText() {
    return resolve(template);
  }




  /**
   * Loads the resource from the classpath as a string.
   *
   * <p>This assumes ISO 8859-1 (latin 1) encoding.
   *
   * @param name the exact name of the resource to load including any path
   *        required to find the resource (i.e. fully qualified name).
   *
   * @return a string representing the data found at the given location or
   *         null if the data could not be loaded.
   */
  private String loadResource(final String name) {
    final String resource = name;
    String retval = null;

    final ClassLoader cLoader = this.getClass().getClassLoader();
    final InputStream inputStream = cLoader.getResourceAsStream(resource);
    if (inputStream != null) {
      final int bufferSize = 1024;
      final char[] buffer = new char[bufferSize];
      final StringBuilder out = new StringBuilder();
      try {
        final Reader in = new InputStreamReader(inputStream, StringUtil.ISO8859_1);
        int ncr = 0;
        while (ncr > -1) {
          ncr = in.read(buffer, 0, buffer.length);
          if (ncr > 0) {
            out.append(buffer, 0, ncr);
          }
        }
        retval = out.toString();
      } catch (final IOException e) {
        Log.warn("Could not load resource from " + resource, e);
      }
    }
    return retval;
  }




  /**
   * Loads a fragment of content from the classpath resolving any found data as
   * a template with the current state of the symbol table.
   *
   * <p>The name must be fully qualified. For example, the section {@code
   * alerts.txt} will be loaded from the root while {@code sections/alerts.txt}
   * will be loaded from the {@code sections} namespace/directory.
   *
   * @param name the exact name of the section to load
   *
   * @return the resolved section or an empty string if the section was not
   *         found. This method call will not return null.
   */
  protected String loadFragment(final String name) {
    String section = loadResource(name);
    if (section != null) {
      section = resolve(section);
    } else {
      section = "";
    }
    return section;
  }




  /**
   * @param template
   * @return
   */
  private String resolve(String template) {
    String retval;
    if (this.isPreProcessing()) {
      retval = Template.preProcess(template, symbolTable);
    } else {
      retval = Template.resolve(template, symbolTable);
    }
    return retval;
  }




  /**
   * Load a template from the {@code templates} namespace/directory of the 
   * classpath.
   *
   * <p>This loads the HTML template with the given name from the class path. Standard practice is to use same name as
   * the responder class bu a single responder may load different templates based on its state.</p>
   *
   * @param name the name of the template to load.
   */
  protected void loadTemplate(final String name) {
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
    } else{
      Log.warn("Could not find template");
    }
  }




  /**
   * Merge the given symbol table into our table.
   *
   * @param symbols the table to merge into our table
   */
  protected void mergeSymbols(final SymbolTable symbols) {
    symbolTable.merge(symbols);
  }




  @Override
  public Response other(final String method, final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response post(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  @Override
  public Response put(final Resource resource, final Map<String, String> urlParams, final HTTPSession session) {
    return Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
  }




  /**
   * @param status the status to set
   */
  protected void setStatus(final Status status) {
    this.status = status;
  }




  /**
   * Replace our symbol table with the given table
   * @param symbols the symbol table to use in this responder.
   */
  protected void setSymbols(final SymbolTable symbols) {
    if (symbols != null) {
      symbolTable = symbols;
    } else {
      symbolTable = new SymbolTable();
    }
  }




  /**
   * @return the template string or an empty string {@code ""} if the template 
   *         is not set. This method will not return null.
   */
  protected String getTemplate() {
    return template;
  }




  /**
   * @param template the template to set
   */
  protected void setTemplate(final String template) {
    if (template != null) {
      this.template = template;
    } else {
      this.template = "";
    }
  }




  /**
   * @return the mimetype
   */
  protected MimeType getRawMimeType() {
    return mimetype;
  }




  /**
   * @param type the mimetype to set
   */
  protected void setRawMimetype(final MimeType type) {
    mimetype = type;
  }




  @Override
  public String getMimeType() {
    return mimetype.getType();
  }




  @Override
  public Status getStatus() {
    return status;
  }




  protected SymbolTable getSymbols() {
    return symbolTable;
  }




  /**
   * @return true if template resolution is only pre-processing; leaving 
   *         unresolved variables in the template, false indicates full 
   *         resolution where unresolved variables are resolved to an empty 
   *         string.
   */
  public boolean isPreProcessing() {
    return preProcessing;
  }




  /**
   * Set the resolution of templates to pre-processing mode.
   * 
   * <p>In pre-processing mode, the template leaves any unresolved tokens in 
   * the template text so as to allow subsequent processing and potential 
   * resolution with an updated or different symbol table. Otherwise, any 
   * unresolved tokens in the template text are resolved to an empty string.
   * 
   * <p>This is useful in testing as it is easy to see where unresolved values
   * occur.
   * 
   * @param preProcessing true sets resolution to preprocessing mode, false 
   *        fully resolves symbols.
   */
  public void setPreProcessing(boolean preProcessing) {
    this.preProcessing = preProcessing;
  }

}