package coyote.dx.http.helpers;

import coyote.commons.network.http.HTTPSession;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dx.Service;
import coyote.i13n.AppEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * This is a helper class for generating the top navigational bar consistently between responders.
 */
public class NavBar {
    public static final String HOME = "Home";
    private final HTTPSession session;
    private final String currentPage = HOME;
    SymbolTable symbolTable = null;
    private Service service = null;


    public NavBar(HTTPSession session) {
        this.session = session;
    }


    public NavBar Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }


    public NavBar CurrentPage(String location) {
        return this;
    }


    public NavBar Service(Service service) {
        this.service = service;
        return this;

    }


    public String build() {
        long newEventCount = 0;
        List<AppEvent> events = null;
        if (service != null) {
            events = service.getStats().getEvents();
            for(AppEvent event:events){
                if( !event.isCleared()){
                    newEventCount++;
                }
            }

        }


        StringBuffer b = new StringBuffer();
        b.append(" <nav class=\"main-header navbar navbar-expand navbar-white navbar-light\">\n" +
                "    <!-- Left navbar links -->\n" +
                "    <ul class=\"navbar-nav\">\n" +
                "      <li class=\"nav-item\">\n" +
                "        <a class=\"nav-link\" data-widget=\"pushmenu\" href=\"#\" role=\"button\"><i class=\"fas fa-bars\"></i></a>\n" +
                "      </li>\n" +
                "      <li class=\"nav-item d-none d-sm-inline-block\">\n" +
                "        <a href=\"/\" class=\"nav-link\">Home</a>\n" +
                "      </li>\n" +
                "    </ul>\n" +
                "\n" +
                "    <!-- Right navbar links -->\n" +
                "    <ul class=\"navbar-nav ml-auto\">\n" +
                "\n" +
                "      <!-- Notifications Dropdown Menu -->\n" +
                "      <li class=\"nav-item dropdown\">\n" +
                "        <a class=\"nav-link\" data-toggle=\"dropdown\" href=\"#\">\n" +
                "          <i class=\"far fa-bell\"></i>\n");
        if (newEventCount > 0) {
            b.append("          <span class=\"badge badge-warning navbar-badge\">");
            b.append(newEventCount);
            b.append("</span>\n");
            b.append("        </a>\n" +
                    "        <div class=\"dropdown-menu dropdown-menu-lg dropdown-menu-right\">\n" +
                    "          <span class=\"dropdown-item dropdown-header\">");
            b.append(newEventCount);
            if(newEventCount==1) b.append(" Event</span>\n");
            else b.append(" Events</span>\n");
            b.append("          <div class=\"dropdown-divider\"></div>\n" +
                    "          <a href=\"/events\" class=\"dropdown-item dropdown-footer\">See All Events</a>\n" +
                    "        </div>\n");
            // more sections can go here...like 1 critical and 4 major...totalling each of the 5 severities.
        } else {
            b.append("        </a>\n");
        }

        b.append("      </li>\n" +
                "    </ul>\n" +
                "  </nav>\n" +
                " ");

        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(), symbolTable);
    }

}
