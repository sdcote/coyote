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
import coyote.dx.context.TransformContext;
import coyote.dx.http.CoyoteHttpManager;
import coyote.dx.vault.Vault;
import coyote.dx.vault.VaultProxy;
import coyote.loader.Context;
import coyote.loader.cfg.ConfigSanitizer;
import coyote.loader.component.ManagedComponent;
import coyote.vault.util.JsonMarshaller;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ComponentDetail {
    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    ManagedComponent component;
    SymbolTable symbolTable = null;

    String name = null;

    public ComponentDetail(ManagedComponent component) {
        this.component = component;
    }

    /**
     * Append a right-aligned and zero-padded numeric value to a `StringBuilder`.
     */
    static private void append(StringBuilder tgt, String pfx, int dgt, long val) {
        tgt.append(pfx);
        if (dgt > 1) {
            int pad = (dgt - 1);
            for (long xa = val; xa > 9 && pad > 0; xa /= 10) {
                pad--;
            }
            for (int xa = 0; xa < pad; xa++) {
                tgt.append('0');
            }
        }
        tgt.append(val);
    }

    /**
     * Formats the given number of milliseconds into hours, minutes and seconds
     * and if requested the remaining milliseconds.
     *
     * @param val the interval in milliseconds
     * @return the time interval in hh:mm:ss format.
     */
    private String formatElapsedMillis(long val, boolean millis) {
        StringBuilder buf = new StringBuilder(20);
        String sgn = "";

        if (val < 0) {
            sgn = "-";
            val = Math.abs(val);
        }

        append(buf, sgn, 0, (val / 3600000));
        append(buf, ":", 2, ((val % 3600000) / 60000));
        append(buf, ":", 2, ((val % 60000) / 1000));
        if (millis)
            append(buf, ".", 3, (val % 1000));

        return buf.toString();
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

        if (component != null) showComponentDetails(b, component);

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

        b.append("              <div class=\"tab-pane\" id=\"cntx_tab\">\n");
        b.append(contextTab(component.getContext()));
        b.append("              </div><!-- /.tab-pane -->\n");
        b.append("              <div class=\"tab-pane\" id=\"symb_tab\">\n");
        if (component.getContext() != null)
            b.append(symbolsTab(component.getContext().getSymbols()));
        else
            b.append(symbolsTab(null));
        b.append("              </div><!-- /.tab-pane -->\n");


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
        b.append("                <div class=\"tab-pane active\" id=\"info_tab\">\n");
        b.append("                  <div class=\"row\">\n");

        b.append("                  <div class=\"col-sm-4\">\n");
        b.append("                    <b>Name:</b> " + component.getName() + "<br>\n");
        b.append("                    <b>Instance:</b> " + component.getId() + "<br>\n");
        b.append("                    <b>Class:</b> " + CUI.trimClassName(component.getClass()) + "<br>\n");
        b.append("                  </div>\n");

        b.append("                  <div class=\"col-sm-4\">\n");
        b.append("                    <b>AppId:</b> " + component.getApplicationId() + "<br>\n");
        b.append("                    <b>SysId:</b> " + component.getSystemId() + "<br>\n");
        b.append("                    <b>CmpId:</b> " + component.getComponentId() + "<br>\n");
        b.append("                  </div>\n");

        b.append("                  <div class=\"col-sm-4\">\n");
        b.append("                    <b>Category:</b> " + component.getCategory() + "<br>\n");
        if (component.getStartTime() == 0)
            b.append("                    <b>Started:</b> Not started<br>\n");
        else
            b.append("                    <b>Started:</b> " + DATE_TIME_FORMAT.format(new Date(component.getStartTime())) + "<br>\n");

        b.append("                    <b>Active:</b> " + component.isActive() + "<br>\n");
        b.append("                  </div>\n");
        b.append("                </div> <!-- ./row -->\n");
        String description = component.getDescription();
        if (StringUtil.isNotEmpty(description)) {
            b.append("                <div class=\"row p-3 mb-2\">\n                  ");
            b.append(description);
            b.append("\n                </div> <!-- ./row -->\n");
        }

        if (component instanceof ScheduledBatchJob) {
            b.append("                <div class=\"row\">\n ");
            b.append(scheduledBatchJobDetails((ScheduledBatchJob) component));
            b.append("                </div> <!-- ./row -->\n");
            b.append("                <div class=\"row\">\n");
            b.append(engineCard(((ScheduledBatchJob) component).getEngine()));
            b.append("                </div> <!-- ./row -->\n");
        }
        b.append("              </div><!-- /.tab-pane -->\n");

        return b.toString();
    }

    private String scheduledBatchJobDetails(ScheduledBatchJob job) {
        StringBuffer b = new StringBuffer();
        b.append("                  <div class=\"col-sm-4\">\n" +
                "                    <b>Cron Entry:</b> " + job.getCronEntry().toString() + "<br>\n");
        if (job.getExecutionTime() == 0)
            b.append("                    <b>Execution Time:</b> Not executed<br>\n");
        else
            b.append("                    <b>Execution Time:</b> " + DATE_TIME_FORMAT.format(new Date(job.getExecutionTime())) + "<br>\n");

        if (job.getExpirationTime() == 0)
            b.append("                    <b>Expiration Time:</b> None (Does not expire)<br>\n");
        else
            b.append("                    <b>Expiration Time:</b> " + DATE_TIME_FORMAT.format(new Date(job.getExpirationTime())) + "<br>\n");

        b.append("                    <b>Execution Interval:</b> " + job.getExecutionInterval() + " ms<br>\n" +
                "                    <b>Execution Count:</b> " + job.getExecutionCount() + "<br>\n");
        if (job.getExecutionLimit() == 0)
            b.append("                    <b>Execution Limit:</b> none\n");
        else
            b.append("                    <b>Execution Limit:</b> " + job.getExecutionLimit() + "\n");

        b.append("                  </div>\n" +
                "                  <div class=\"col-sm-4\">\n");
        if (job.getNextJob() != null)
            b.append("                    <b>Linked:</b> true<br>\n");
        else
            b.append("                    <b>Linked:</b> false<br>\n");

        b.append("                    <b>Cancelled:</b> " + job.isCancelled() + "<br>\n" +
                "                    <b>Repeatable:</b> " + job.isRepeatable() + "<br>\n" +
                "                    <b>Restart:</b> " + job.isRestart() + "<br>\n" +
                "                    <b>Enabled:</b> " + job.isEnabled() + "<br>\n" +
                "                    <b>Licensed:</b> " + job.isLicensed() + "\n" +
                "                  </div>\n" +
                "                  <div class=\"col-sm-4\">\n" +
                "                    <b>Suspended:</b> " + job.isSuspended() + "<br>\n" +
                "                    <b>Shutdown:</b> " + job.isShutdown() + "<br>\n" +
                "                    <b>Active:</b> " + job.isActive() + "<br>\n" +
                "                    <b>Hyper:</b> " + job.isHyper() + "\n" +
                "                  </div>\n");
        return b.toString();
    }


    private String engineCard(TransformEngine engine) {
        StringBuffer b = new StringBuffer();

        b.append("             <div class=\"p-3 mb-3 w-100\">\n" +
                "              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header d-flex p-0\">\n" +
                "                  <h3 class=\"card-title p-3\"><i class=\"fa fa-cog\"></i> Transform Engine</h3>\n" +
                "                  <ul class=\"nav nav-pills ml-auto p-2\">\n" +
                "                    <li class=\"nav-item\"><a class=\"nav-link active\" href=\"#einfo_tab\" data-toggle=\"tab\">Info</a></li>\n" +
                "                    <li class=\"nav-item\"><a class=\"nav-link\" href=\"#ecntx_tab\" data-toggle=\"tab\">Context</a></li>\n" +
                "                    <li class=\"nav-item\"><a class=\"nav-link\" href=\"#esymb_tab\" data-toggle=\"tab\">Symbol</a></li>\n" +
                "                    <li class=\"nav-item\"><a class=\"nav-link\" href=\"#ecmpt_tab\" data-toggle=\"tab\">Components</a></li>\n" +
                "                  </ul>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                  <div class=\"tab-content\">\n" +
                "                    <div class=\"tab-pane active\" id=\"einfo_tab\">\n" +
                "                  <div class=\"row\">\n" +
                "                    <div class=\"col-sm-4\">\n" +
                "                      <b>Name:</b> " + engine.getName() + "<br>\n" +
                "                      <b>Work:</b> " + engine.getWorkDirectory() + "<br>\n" +
                "                    </div>\n" +
                "                    <div class=\"col-sm-4\">\n" +
                "                      <b>ID:</b> " + engine.getInstanceId() + "<br>\n" +
                "                      <b>Job:</b> " + engine.getJobDirectory() + "<br>\n" +
                "                    </div>\n" +
                "                    <div class=\"col-sm-4\">\n" +
                "                      <b>Runcount:</b> " + engine.getInstanceRunCount() + "<br>\n" +
                "                    </div>\n" +
                "                  </div> <!-- ./row -->\n" +
                "              </div><!-- /.tab-pane -->\n");

        b.append("              <div class=\"tab-pane\" id=\"ecntx_tab\">\n");
        b.append(transformContextTab(engine.getContext()));
        b.append("              </div><!-- /.tab-pane -->\n");
        b.append("              <div class=\"tab-pane\" id=\"esymb_tab\">\n");
        b.append(symbolsTab(engine.getSymbolTable()));
        b.append("              </div><!-- /.tab-pane -->\n");
        b.append("              <div class=\"tab-pane\" id=\"ecmpt_tab\">\n");
        b.append(componentsTab(engine));
        b.append("              </div> <!-- /.tab-pane -->\n" +
                "            </div><!-- /.tab-content -->\n" +
                "          </div> <!-- /.card Body -->" +
                "        </div><!-- ./card (engine) -->\n" +
                "      </div> <!-- card content padding-->\n");
        return b.toString();
    }

    private String componentsTab(TransformEngine engine) {
        StringBuffer b = new StringBuffer();


        FrameReader preloader = engine.getPreloader();
        if (preloader != null) {
            showPreloader(preloader, b);
        }


        List<TransformTask> pretasks = engine.getPreprocessTasks();
        if (pretasks.size() > 0) {
            showPreprocessingTasks(pretasks, b);
        }


        FrameReader reader = engine.getReader();
        if (reader != null) {
            showReader(reader, b);
        }


        List<FrameFilter> filters = engine.getFilters();
        if (filters.size() > 0) {
            showFilters(filters, b);
        }


        List<FrameValidator> validators = engine.getValidators();
        if (validators.size() > 0) {
            showValidators(validators, b);
        }


        List<FrameTransform> transformers = engine.getTransformers();
        if (transformers.size() > 0) {
            showTransformers(transformers, b);
        }


        List<FrameAggregator> aggregators = engine.getAggregators();
        if (aggregators.size() > 0) {
            showAggregators(aggregators, b);
        }


        FrameMapper mapper = engine.getMapper();
        if (mapper != null) {
            showMapper(mapper, b);
        }

        List<FrameWriter> writers = engine.getWriters();
        if (writers.size() > 0) {
            showWriters(writers, b);
        }


        List<TransformTask> postasks = engine.getPostprocessTasks();
        if (postasks.size() > 0) {
            showPostprocessingTasks(postasks, b);
        }


        List<ContextListener> listeners = engine.getListeners();
        if (listeners.size() > 0) {
            showListeners(listeners, b);
        }

        return b.toString();
    }


    private String contextTab(Context context) {
        StringBuffer b = new StringBuffer();

        String startTime = (context.getStartTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getStartTime())) : "Not Started";
        String endTime = (context.getEndTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getEndTime())) : "Not Ended";
        String contextStatus = StringUtil.isNotEmpty(context.getStatus()) ? context.getStatus() : "Unknown";
        //context.getListeners(); // TODO?
        b.append(
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
                "                </div> <!-- card -->\n"
        );
        return b.toString();
    }


    private String transformContextTab(TransformContext context) {
        StringBuffer b = new StringBuffer();

        if (context != null) {
            String elapsed = formatElapsedMillis(context.getElapsed(), true);

            String startTime = (context.getStartTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getStartTime())) : "Not Started";
            String endTime = (context.getEndTime() != 0) ? DATE_TIME_FORMAT.format(new Date(context.getEndTime())) : "Not Ended";
            String contextState = StringUtil.isNotEmpty(context.getState()) ? context.getState() : "Unknown";

            //context.getListeners(); // TODO?

            b.append(
                    "                <div class=\"row mb-2\">\n" +
                            "                  <div class=\"col-sm-4\">\n" +
                            "                    <b>State:</b> " + contextState + "<br>\n" +
                            "                    <b>Errored:</b>  " + context.isInError() + "<br>\n" +
                            "                    <b>Open Count:</b>  " + context.getOpenCount() + "\n" +
                            "                  </div>\n" +
                            "                  <div class=\"col-sm-4\">\n" +
                            "                    <b>Start Time:</b> " + startTime + "<br>\n" +
                            "                    <b>End Time:</b> " + endTime + "<br>\n" +
                            "                    <b>Elapsed:</b> " + elapsed + "\n" +
                            "                  </div>\n" +
                            "                  <div class=\"col-sm-4\">\n" +
                            "                    <b>Row:</b> " + context.getRow() + "<br>\n" +
                            "                  </div>\n" +
                            "                </div> <!-- row -->\n");
            String contextMessage = context.getErrorMessage();
            if (StringUtil.isNotEmpty(contextMessage)) {
                b.append("                <div class=\"row p-3 mb-2\">\n" +
                        "                  Error: \n" + contextMessage +
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
                        "                          <td>" + formatObject(context.get(key)) + "</td>\n" +
                        "                        </tr>\n");
            }

            b.append("                      </tbody>\n" +
                    "                    </table>\n" +
                    "                  </div><!-- /.card-body -->\n" +
                    "                </div> <!-- card -->\n"
            );
        } else {
            b.append("No transform context available. Engine may not have been run yet.");
        }
        return b.toString();
    }


    /**
     * Format the given object into a human-readable string.
     *
     * <p>Designed primarily for String arrays, but it might evolve into supporting other types.</p>
     *
     * @param o the object to format
     * @return a human-readable version of the object.
     */
    private String formatObject(Object o) {
        String retval = o.toString();
        if (o instanceof Object[]) {
            StringBuffer b = new StringBuffer(retval);
            b.append(" = ");
            b.append(Arrays.toString((Object[]) o));
            retval = b.toString();
        }
        return retval;
    }


    private String symbolsTab(SymbolTable symtab) {
        StringBuffer b = new StringBuffer();
        if (symtab != null) {
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
                    "                </div> <!-- card -->\n");
        } else {
            b.append("No symbols found. Engine may not have been run yet.");
        }
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


    private void showPreloader(FrameReader preloader, StringBuffer b) {
        String group = "preloadergrp";

        b.append(" <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-caret-square-up\"></i> Preloader</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        showCollapsibleComponent(preloader, group, "plodr1", b);
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (preloader) -->");
    }

    private void showPreprocessingTasks(List<TransformTask> pretasks, StringBuffer b) {
        String group = "pretaskgrp";

        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                 <div class=\"card-header\">\n" +
                "                   <h3 class=\"card-title\"><i class=\"fa fa-tasks\"></i> Pre-Processing Tasks</h3>\n" +
                "                 </div>\n" +
                "                 <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (TransformTask task : pretasks) {
            String token = "prept" + idx++;
            showCollapsibleComponent(task, group, token, b);
            idx++;
        }
        b.append("                   </div>\n" +
                "                 </div><!-- ./card-body -->\n" +
                "               </div><!-- ./card (pretasks) -->");

    }

    private void showReader(FrameReader reader, StringBuffer b) {
        String group = "readergrp";
        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                 <div class=\"card-header\">\n" +
                "                   <h3 class=\"card-title\"><i class=\"fa fa-upload\"></i> Reader</h3>\n" +
                "                 </div>\n" +
                "                 <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        showCollapsibleComponent(reader, group, "rc1", b);
        b.append("                   </div>\n" +
                "                 </div><!-- ./card-body -->\n" +
                "               </div><!-- ./card (reader) -->\n");
    }

    private void showCollapsibleComponent(ConfigurableComponent component, String group, String name, StringBuffer b) {
        String cardTheme;
        if (component.isEnabled()) {
            cardTheme = "card-success";
        } else {
            cardTheme = "card-danger";
        }

        // TODO: Where should this go?
        String condition = "";
        if (component instanceof ConditionalComponent) {
            condition = ((ConditionalComponent) component).getCondition();
        }

        b.append("                  <div class=\"card " + cardTheme + " card-outline\">\n" +
                "                    <a class=\"d-block w-100\" data-toggle=\"collapse\" href=\"#" + name + "\">\n" +
                "                      <div class=\"card-header\">\n" +
                "                        <h4 class=\"card-title w-100\">");
        b.append(CUI.trimClassName(component.getClass()));
        b.append("</h4>\n" +
                "                      </div>\n" +
                "                    </a>\n" +
                "                    <div id=\"" + name + "\" class=\"collapse\" data-parent=\"#" + group + "\">\n" +
                "                      <div class=\"card-body\">\n" +
                "                        <pre>\n");
        b.append(JSONMarshaler.toFormattedString(ConfigSanitizer.sanitize(component.getConfiguration())));
        b.append("                        </pre>\n" +
                "                      </div>\n" +
                "                    </div>\n" +
                "                  </div>\n");
    }

    private void showFilters(List<FrameFilter> filters, StringBuffer b) {
        String group = "filtergrp";
        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                 <div class=\"card-header\">\n" +
                "                   <h3 class=\"card-title\"><i class=\"fa fa-filter\"></i> Filters</h3>\n" +
                "                 </div>\n" +
                "                 <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (FrameFilter filter : filters) {
            String token = "fltr" + idx++;
            showCollapsibleComponent(filter, group, token, b);
        }
        b.append("                   </div>\n" +
                "                 </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (filters) -->\n");
    }

    private void showValidators(List<FrameValidator> validators, StringBuffer b) {
        String group = "validatorgrp";

        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-check\"></i> Validators</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (FrameValidator validator : validators) {
            String token = "vldtr" + idx++;
            showCollapsibleComponent(validator, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (validators) -->\n");
    }

    private void showTransformers(List<FrameTransform> transformers, StringBuffer b) {
        String group = "transformergrp";

        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-cogs\"></i> Transformers</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (FrameTransform transformer : transformers) {
            String token = "ppt" + idx++;
            showCollapsibleComponent(transformer, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (transformers) -->\n");
    }

    private void showAggregators(List<FrameAggregator> aggregators, StringBuffer b) {
        String group = "aggregatorgrp";

        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-object-group\"></i> Aggregators</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (FrameAggregator aggregator : aggregators) {
            String token = "agrtr" + idx++;
            showCollapsibleComponent(aggregator, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (aggregators) -->\n");
    }

    private void showMapper(FrameMapper mapper, StringBuffer b) {
        String group = "mappergrp";
        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-random\"></i> Mapper</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        showCollapsibleComponent(mapper, group, "mpr1", b);
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (mapper) -->\n");
    }

    private void showWriters(List<FrameWriter> writers, StringBuffer b) {
        String group = "writergrp";
        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-download\"></i> Writers</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (FrameWriter writer : writers) {
            String token = "wrtr" + idx++;
            showCollapsibleComponent(writer, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (writers) -->\n");
    }

    private void showPostprocessingTasks(List<TransformTask> postasks, StringBuffer b) {
        String group = "posttaskgrp";
        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-tasks\"></i> Post-Processing Tasks</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (TransformTask task : postasks) {
            String token = "postt" + idx++;
            showCollapsibleComponent(task, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (posttasks) -->\n");
    }

    private void showListeners(List<ContextListener> listeners, StringBuffer b) {
        String group = "listenergrp";

        b.append("              <div class=\"card card-primary card-outline\">\n" +
                "                <div class=\"card-header\">\n" +
                "                  <h3 class=\"card-title\"><i class=\"fa fa-bolt\"></i> Listeners</h3>\n" +
                "                </div>\n" +
                "                <div class=\"card-body p-3\">\n" +
                "                   <div class=\"col-12\" id=\"" + group + "\">\n");
        int idx = 0;
        for (ContextListener listener : listeners) {
            String token = "lstnr" + idx++;
            showCollapsibleComponent(listener, group, token, b);
        }
        b.append("                   </div>\n" +
                "                </div><!-- ./card-body -->\n" +
                "              </div><!-- ./card (listeners) -->\n");
    }


}
