/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.helpers;

import coyote.commons.DateUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dx.*;
import coyote.dx.context.ContextListener;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultProxy;
import coyote.loader.Context;
import coyote.loader.Loader;
import coyote.loader.component.ManagedComponent;

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
        b.append("      <div class=\"container-fluid\">\n");

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
                "          <div class=\"card-header\">\n" +
                "            <h3 class=\"card-title\"><i class=\"fa fa-info-circle\"></i> Summary</h3>\n" +
                "            <div class=\"card-tools\">\n" +
                "              <button type=\"button\" class=\"btn btn-tool\" data-card-widget=\"collapse\" title=\"Collapse\">\n" +
                "                <i class=\"fas fa-minus\"></i>\n" +
                "              </button>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "          <div class=\"card-body p-0\">\n" +
                "            <div class=\"p-3 mb3\">\n" +
                "              <div class=\"row\">\n");

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


        component.getConfiguration();

        component.getContext();
        SymbolTable symtab = component.getContext().getSymbols();
        component.getContext().getStatus();
        component.getProfile();
        component.getTemplate();

        ((ManagedComponent)component).getDescription();


        // Summary fa fa-info-circle
        // Context fa fa-list / fa fa-list-alt / fa fa-th-list

        b.append("              </div> <!-- Row -->\n" +
                "            </div> <!-- Card Content padding-->\n" +
                "          </div> <!-- Card Body -->\n" +
                "        </div> <!-- Card -->\n");
    }


    private void showJobDetails(StringBuffer b, ScheduledBatchJob job) {
        job.getInstanceRunCount();
        job.getCronEntry();
        job.getCategory();
        showVaultDetails(b);
        showEngineDetails(b, job.getEngine());
        // Vault fa fa-lock

    }


    private void showVaultDetails(StringBuffer b) {
        Object obj = Template.get(Vault.LOOKUP_TAG, null);
        if (obj != null && obj instanceof VaultProxy) {
            VaultProxy vault = (VaultProxy) obj;
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
        engine.getReader();
        List<FrameFilter> filters = engine.getFilters();
        List<FrameValidator> validators = engine.getValidators();
        List<FrameTransform> transformers = engine.getTransformers();
        List<FrameAggregator> aggregators = engine.getAggregators();
        List<FrameWriter> writers = engine.getWriters();
        List<ContextListener> listeners = engine.getListeners();
        List<TransformTask> postasks = engine.getPostprocessTasks();
    }


}
