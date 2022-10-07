/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.helpers;

import coyote.commons.StringUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dataframe.marshal.JSONMarshaler;
import coyote.dx.*;
import coyote.dx.context.ContextListener;
import coyote.dx.http.CoyoteHttpManager;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultProxy;
import coyote.loader.Context;
import coyote.loader.cfg.ConfigSanitizer;
import coyote.loader.component.ManagedComponent;
import coyote.vault.util.JsonMarshaller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ComponentDetail {
    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    ManagedComponent component = null;
    SymbolTable symbolTable = null;

    String name = null;

    public ComponentDetail(ManagedComponent component) {
        this.component = component;
    }


    public ComponentDetail Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }


    public ComponentDetail name(String name) {
        this.name = name;
        return this;
    }


    public String build() {
        StringBuffer b = new StringBuffer();
        b.append("<div class=\"container-fluid\">\n");

        if (component != null) {
            showComponentDetails(b, component);
            if (component instanceof ScheduledBatchJob) {
                showJobDetails(b, (ScheduledBatchJob) component);
            }
        }

        b.append("      </div><!-- /.container-fluid -->\n");

        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(), symbolTable);
    }


    private void showComponentDetails(StringBuffer b, ManagedComponent component) {
        b.append("        <div class=\"card\">\n" +
                "          <div class=\"card-header d-flex p-0\">\n" +
                "            <h3 class=\"card-title p-3\"><i class=\"fa fa-cog\"></i> " + component.getName() + "</h3>\n" +
                "            <ul class=\"nav nav-pills ml-auto p-2\">\n" +
                "              <li class=\"nav-item\"><a class=\"nav-link active\" href=\"#info_tab\" data-toggle=\"tab\">Info</a></li>\n" +
                "              <li class=\"nav-item\"><a class=\"nav-link\" href=\"#cntx_tab\" data-toggle=\"tab\">Context</a></li>\n" +
                "              <li class=\"nav-item\"><a class=\"nav-link\" href=\"#symb_tab\" data-toggle=\"tab\">Symbols</a></li>\n" +
                "              <li class=\"nav-item\"><a class=\"nav-link\" href=\"#conf_tab\" data-toggle=\"tab\">Config</a></li>\n" +
                "              <li>\n" +
                "                <div class=\"card-tools\">\n" +
                "                  <button type=\"button\" class=\"btn btn-tool\" data-card-widget=\"collapse\" title=\"Collapse\"><i class=\"fas fa-minus\"></i></button>\n" +
                "                </div>\n" +
                "              </li>\n" +
                "            </ul>\n" +
                "          </div>\n" +
                "          <div class=\"card-body p-3\">\n" +
                "            <div class=\"tab-content\">\n");
        b.append(infoTab(component));
        b.append(contextTab(component));
        b.append(symbolsTab(component));
        b.append(configTab(component));

        b.append("            </div><!-- /.tab-content -->\n" +
                "          </div> <!-- /.card Body -->\n" +
                "        </div> <!-- /.card --> \n");

        // What about these?
        component.getProfile(); // JSON String
        component.getTemplate(); // JSON String

    }

    private String infoTab(ManagedComponent component) {
        StringBuffer b = new StringBuffer();
        b.append("              <div class=\"tab-pane active\" id=\"info_tab\">\n");
        b.append("                <div class=\"row\">\n");

        b.append("                <div class=\"col-sm-4\">\n");
        b.append("                  <b>Name:</b> " + component.getName() + "<br>\n");
        b.append("                  <b>Instance:</b> " + component.getId() + "<br>\n");
        b.append("                  <b>Class:</b> " + component.getClass() + "<br>\n");
        b.append("                </div>\n");

        b.append("                <div class=\"col-sm-4\">\n");
        b.append("                  <b>AppId:</b> " + component.getApplicationId() + "<br>\n");
        b.append("                  <b>SysId:</b> " + component.getSystemId() + "<br>\n");
        b.append("                  <b>CmpId:</b> " + component.getComponentId() + "<br>\n");
        b.append("                </div>\n");

        b.append("                <div class=\"col-sm-4\">\n");
        b.append("                  <b>Category:</b> " + component.getCategory() + "<br>\n");
        if (component.getStartTime() == 0)
            b.append("                  <b>Started:</b> Not started<br>\n");
        else
            b.append("                  <b>Started:</b> " + DATE_TIME_FORMAT.format(new Date(component.getStartTime())) + "<br>\n");

        b.append("                  <b>Active:</b> " + component.isActive() + "<br>\n");
        b.append("                </div>\n");
        b.append("                </div> <!-- ./row -->\n");
        String description = component.getDescription();
        if (StringUtil.isNotEmpty(description)) {
            b.append("                <div class=\"row p-3 mb-2\">\n" +
                    "                  Message: \n" +
                    "                </div> <!-- ./row -->\n");
        }
        b.append("              </div><!-- /.tab-pane -->\n");

        return b.toString();
    }

    private String contextTab(ManagedComponent component) {
        StringBuffer b = new StringBuffer();
        Context context = component.getContext();

        context.getMessage();

        String startTime = (context.getStartTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getStartTime())) : "Not Started";
        String endTime = (context.getEndTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getEndTime())) : "Not Ended";
        String contextStatus = StringUtil.isNotEmpty(context.getStatus()) ? context.getStatus() : "Unknown";

        context.getListeners();

        b.append("              <div class=\"tab-pane\" id=\"cntx_tab\">\n" +
                "                <div class=\"row mb-2\">\n" +
                "                  <div class=\"col-sm-6\">\n" +
                "                    <b>Status</b> " + contextStatus + "<br>\n" +
                "                    <b>Errored</b>  " + context.isInError() + "\n" +
                "                  </div>\n" +
                "                  <div class=\"col-sm-6\">\n" +
                "                    <b>Start Time</b> " + startTime + "<br>\n" +
                "                    <b>End Time</b> " + endTime + "\n" +
                "                  </div>\n" +
                "                </div> <!-- row -->\n");
        String contextMessage = context.getMessage();
        if (StringUtil.isNotEmpty(contextMessage)) {
            b.append("                <div class=\"row p-3 mb-2\">\n" +
                    "                  Message: \n" + contextMessage +

                    "                </div> <!-- row -->\n");
        }
        b.append("                <div class=\"card\">\n" +
                "                  <div class=\"card-body table-responsive p-0\">\n" +
                "                    <table class=\"table table-hover text-nowrap\">\n" +
                "                      <thead>\n" +
                "                        <tr>\n" +
                "                          <th>Key</th>\n" +
                "                          <th>Value</th>\n" +
                "                        </tr>\n" +
                "                      </thead>\n" +
                "                      <tbody>\n");

        for (String key : context.getKeys()) {
            b.append("                        <tr>\n" +
                    "                          <td>" + key + "</td>\n" +
                    "                          <td>" + context.get(key) + "</td>\n" +
                    "                        </tr>\n");
        }

        b.append("                      </tbody>\n" +
                "                    </table>\n" +
                "                  </div><!-- /.card-body -->\n" +
                "                </div> <!-- card -->\n" +
                "              </div><!-- /.tab-pane -->\n");
        return b.toString();
    }


    private String symbolsTab(ManagedComponent component) {
        StringBuffer b = new StringBuffer();

        SymbolTable symtab = component.getContext().getSymbols();

        b.append("              <div class=\"tab-pane\" id=\"symb_tab\">\n" +
                "                <div class=\"card\">\n" +
                "                  <div class=\"card-body table-responsive p-0\">\n" +
                "                    <table class=\"table table-hover text-nowrap\">\n" +
                "                      <thead>\n" +
                "                        <tr>\n" +
                "                          <th>Key</th>\n" +
                "                          <th>Value</th>\n" +
                "                        </tr>\n" +
                "                      </thead>\n" +
                "                      <tbody>\n");

        for (Object key : symtab.keySet()) {
            String symbol = key.toString();
            if (CoyoteHttpManager.symbolIsProtected(symbol)) {
                b.append("                        <tr>\n" +
                        "                          <td>" + symbol + "</td>\n" +
                        "                          <td>[PROTECTED]</td>\n" +
                        "                        </tr>\n");
            } else {
                b.append("                        <tr>\n" +
                        "                          <td>" + symbol + "</td>\n" +
                        "                          <td>" + symtab.getString(symbol) + "</td>\n" +
                        "                        </tr>\n");
            }
        }
        b.append("                      </tbody>\n" +
                "                    </table>\n" +
                "                  </div><!-- /.card-body -->\n" +
                "                </div> <!-- card -->\n" +
                "              </div><!-- /.tab-pane -->\n");
        return b.toString();
    }


    private String configTab(ManagedComponent component) {
        StringBuffer b = new StringBuffer();
        b.append("              <div class=\"tab-pane\" id=\"conf_tab\">\n" +
                "                <div class=\"card\">\n" +
                "                  <div class=\"card-body table-responsive p-3\">\n" +
                "                  <pre>\n");
        b.append(JSONMarshaler.toFormattedString(ConfigSanitizer.sanitize(component.getConfiguration())));
        b.append("                  </pre>\n" +
                "                  </div><!-- /.card-body -->\n" +
                "                </div> <!-- card -->\n" +
                "              </div><!-- /.tab-pane -->\n");
        return b.toString();
    }


    private void showJobDetails(StringBuffer b, ScheduledBatchJob job) {
        job.getInstanceRunCount();
        job.getCronEntry();
        job.getCategory();
        showVaultDetails(b);
        showEngineDetails(b, job.getEngine());
    }


    private void showVaultDetails(StringBuffer b) {
        Object obj = Template.get(Vault.LOOKUP_TAG, null);
        if (obj != null && obj instanceof VaultProxy) {
            VaultProxy vault = (VaultProxy) obj;
            // Vault fa fa-lock
        }
    }


    private void showEngineDetails(StringBuffer b, TransformEngine engine) {
        // Preprocessing fa fa-tasks
        // Preloader fa fa-level-up
        // Reader fa fa-upload
        // Filter fa fa-filter
        // Validator fa fa-check-square-o
        // Transform fa fa-cogs
        // Mapper fa fa-random
        // Aggregator fa fa-object-group
        // Writers fa fa-download
        // Listeners fa fa-bolt
        // Post Processing fa fa-tasks
        engine.getSymbolTable();
        engine.getInstanceRunCount();
        engine.getName();
        engine.getContext();
        engine.getSymbolTable();
        engine.getInstanceId();
        engine.getMapper();
        engine.getJobDirectory();
        engine.getWorkDirectory();
        engine.getLoader();
        engine.getLogManager();
        List<TransformTask> pretasks = engine.getPreprocessTasks();
        FrameReader preloader = engine.getPreloader();
        FrameReader reader = engine.getReader();
        if (reader instanceof ConditionalComponent) {
            String condition = ((ConditionalComponent) reader).getCondition();
        }
        List<FrameFilter> filters = engine.getFilters();
        List<FrameValidator> validators = engine.getValidators();
        List<FrameTransform> transformers = engine.getTransformers();
        List<FrameAggregator> aggregators = engine.getAggregators();
        List<FrameWriter> writers = engine.getWriters();
        List<ContextListener> listeners = engine.getListeners();
        List<TransformTask> postasks = engine.getPostprocessTasks();
    }


}
