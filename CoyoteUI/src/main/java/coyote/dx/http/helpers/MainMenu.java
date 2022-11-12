/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.helpers;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.loader.log.Log;

import java.util.List;


/**
 * This is a helper class for generating the main menu consistently between responders.
 */
public class MainMenu {
    public static final String HOME = "Home";
    public static final String COMPONENTS = "Components";
    public static final String EVENTS = "Events";
    private final HTTPSession session;
    private final String currentPage = HOME;
    SymbolTable symbolTable = null;

    public MainMenu(HTTPSession session) {
        this.session = session;
    }

    public MainMenu Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }


    public MainMenu CurrentPage(String location) {
        return this;
    }


    public String build() {
        StringBuffer b = new StringBuffer();
        String username = session.getUserName();
        List<String> usergroups = session.getUserGroups();

        if (usergroups.size() == 0) {
            Log.info("No groups");
        }

        b.append(" <aside class=\"main-sidebar sidebar-dark-primary elevation-4\">\n" +
                "    <!-- Brand Logo -->\n" +
                "    <a href=\"/\" class=\"brand-link\">\n" +
                "      <img src=\"/dist/img/Logo.png\" alt=\"CDX Logo\" class=\"brand-image img-circle elevation-3\" style=\"opacity: .8\">\n" +
                "      <span class=\"brand-text font-weight-light\">[#$NodeID#]</span>\n" +
                "    </a>\n" +
                "\n" +
                "    <!-- Sidebar -->\n" +
                "    <div class=\"sidebar\">\n" +
                "      <!-- Sidebar user panel -->\n" +
                "      <div class=\"user-panel mt-3 pb-3 mb-3 d-flex\">\n" +
                "        <div class=\"image\">\n" +
                "          <img src=\"/dist/img/user.png\" class=\"img-circle elevation-2\" alt=\"User Image\">\n" +
                "        </div>\n" +
                "        <div class=\"info\">\n");
        if (StringUtil.isBlank(username)) {
            b.append("          <a href=\"/login\" class=\"d-block\">Anonymous</a>\n");
        } else {
            b.append("          <a href=\"/logout\" class=\"d-block\">" + username + "</a>\n");
        }
        b.append("        </div>\n" +
                "      </div>\n" +
                "\n" +
                "      <!-- Sidebar Menu -->\n" +
                "      <nav class=\"mt-2\">\n" +
                "        <ul class=\"nav nav-pills nav-sidebar flex-column\" data-widget=\"treeview\" role=\"menu\" data-accordion=\"false\">\n" +
                "          <!-- Add icons to the links using the .nav-icon class with font-awesome or any other icon font library -->\n" +
                "\n" +
                "\n" +
                "          <li class=\"nav-item\">\n" +
                "            <a href=\"/components\" class=\"nav-link\">\n" +
                "              <i class=\"nav-icon fa fa-cog text-info\"></i>\n" +
                "              <p>Components</p>\n" +
                "            </a>\n" +
                "          </li>\n" +
                "          <li class=\"nav-item\">\n" +
                "            <a href=\"/events\" class=\"nav-link\">\n" +
                "              <i class=\"nav-icon far fa-bell text-info\"></i>\n" +
                "              <p>Events</p>\n" +
                "            </a>\n" +
                "          </li>\n" +
                "        </ul>\n" +
                "      </nav>\n" +
                "      <!-- /.sidebar-menu -->\n" +
                "    </div>\n" +
                "    <!-- /.sidebar -->\n" +
                "  </aside>");

        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(), symbolTable);
    }

}
