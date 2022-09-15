package coyote.dx.http.helpers;

import coyote.commons.DateUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dx.ScheduledBatchJob;
import coyote.dx.Service;
import coyote.i13n.AppEvent;
import coyote.loader.component.ManagedComponent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EventList {
    private static final List<AppEvent> EMPTY_EVENTS = new ArrayList<>();
    private final HTTPSession session;
    private final TimeZone tzUTC = TimeZone.getTimeZone("UTC");
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    private final List<AppEvent> events = new ArrayList<>();
    private SymbolTable symbolTable = null;
    private Service service = null;

    public EventList(HTTPSession session) {
        this.session = session;
    }


    public EventList Service(Service service) {
        this.service = service;
        return this;
    }


    public EventList Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }

    public String build() {
        df.setTimeZone(tzUTC);

        List<AppEvent> events = null;
        if (service != null) {
            events = service.getStats().getEvents();
        } else {
            events = EMPTY_EVENTS;
        }
        StringBuffer b = new StringBuffer();
        b.append("      <div class=\"container-fluid\">\n" +
                "        <div class=\"row\">\n" +
                "          <div class=\"col-12\">\n" +
                "           \n" +
                "            <div class=\"card\">\n" +
                "              <div class=\"card-header\">\n" +
                "                <h3 class=\"card-title\">Active events for all components</h3>\n" +
                "              </div>\n" +
                "              <!-- /.card-header -->\n" +
                "              <div class=\"card-body\">\n" +
                "                <table id=\"eventtable\" class=\"table table-bordered table-striped\">\n" +
                "                  <thead>\n" +
                "                  <tr>\n" +
                "                    <th>Seq</th>\n" +
                "                    <th>Timestamp</th>\n" +
                "                    <th>Application</th>\n" +
                "                    <th>System</th>\n" +
                "                    <th>Component</th>\n" +
                "                    <th>Component</th>\n" +
                "                    <th>Severity</th>\n" +
                "                    <th>Major</th>\n" +
                "                    <th>Minor</th>\n" +
                "                    <th>Message</th>\n" +
                "                  </tr>\n" +
                "                  </thead>\n" +
                "                  <tbody>\n");

        for (AppEvent event : events) {
            b.append("                  <tr>\n");

            b.append("                    <td>");
            b.append(event.getSequence());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.getSequence());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(df.format(new Date(event.getTime())));
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.get_appId());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.get_sysId());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.get_cmpId());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.getSeverity());
                b.append(" - ");
                b.append(event.getSeverityString());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.getMajorCode());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.getMinorCode());
            b.append("</td>\n");

            b.append("                    <td>");
            b.append(event.getMessage());
            b.append("</td>\n");

            b.append("                  </tr>\n");
        }

        b.append("                  </tbody>\n" +
                "                  <tfoot>\n" +
                "                  <tr>\n" +
                "                    <th>Seq</th>\n" +
                "                    <th>Timestamp</th>\n" +
                "                    <th>Application</th>\n" +
                "                    <th>System</th>\n" +
                "                    <th>Component</th>\n" +
                "                    <th>Component</th>\n" +
                "                    <th>Severity</th>\n" +
                "                    <th>Major</th>\n" +
                "                    <th>Minor</th>\n" +
                "                    <th>Message</th>\n" +
                "                  </tr>\n" +
                "                  </tfoot>\n" +
                "                </table>\n" +
                "              </div>\n" +
                "              <!-- /.card-body -->\n" +
                "            </div>\n");
        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(), symbolTable);
    }

}
