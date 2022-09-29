/*
 * Copyright (c) 2022 Stephan D. Cote' - All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the MIT License which accompanies this distribution, and is
 * available at http://creativecommons.org/licenses/MIT/
 */
package coyote.dx.http.responder;

import coyote.commons.StringUtil;
import coyote.commons.network.http.HTTPSession;
import coyote.commons.network.http.Response;
import coyote.commons.network.http.SessionProfile;
import coyote.commons.network.http.SessionProfileManager;
import coyote.commons.network.http.responder.Resource;
import coyote.commons.network.http.responder.Responder;
import coyote.dx.ScheduledBatchJob;
import coyote.dx.Service;
import coyote.dx.http.helpers.ComponentDetail;
import coyote.dx.http.helpers.ComponentList;
import coyote.dx.http.helpers.MainMenu;
import coyote.dx.http.helpers.NavBar;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;

import java.util.List;
import java.util.Map;


/**
 *
 */
public class Components extends ViewResponder implements Responder {


    @Override
    public Response get(Resource resource, Map<String, String> urlParams, HTTPSession session) {
        SessionProfile profile = SessionProfileManager.retrieveOrCreateProfile(session);
        Service service = resource.initParameter(0, Service.class);
        Response retval = null;
        setPreProcessing(true);

        loadTemplate(this.getClass().getSimpleName());

        mergeSymbols(service.getContext().getSymbols()); // operational context
        mergeSymbols(service.getSymbols()); // loader

        getSymbols().put("Footer", loadFragment("fragments/Footer.html"));
        getSymbols().put("NavBar", new NavBar(session).Service(service).Symbols(getSymbols()).CurrentPage(MainMenu.COMPONENTS).build());
        getSymbols().put("MainMenu", new MainMenu(session).Symbols(getSymbols()).CurrentPage(MainMenu.COMPONENTS).build());

        // Get the component name from the URL parameters, specified when we were registered with the router
        String name = urlParams.get("name");
        populateBreadcrumb(name);

        if (StringUtil.isNotBlank(name)) {
            ManagedComponent component = findComponent(name,service.getComponents());
            if( component != null ) {
                getSymbols().put("Contents", new ComponentDetail(component).Symbols(getSymbols()).name(name).build());
                retval = Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
            } else {
                retval = new NotFound().get(resource,urlParams,session);
            }
        } else {
            getSymbols().put("Contents", new ComponentList(service.getComponents()).Symbols(getSymbols()).build());
            retval = Response.createFixedLengthResponse(getStatus(), getMimeType(), getText());
        }
        return retval;
    }

    private void populateBreadcrumb(String name) {
        if (StringUtil.isNotBlank(name)) {
            StringBuffer b = new StringBuffer("              <li class=\"breadcrumb-item\"><a href=\"\\components\\");
            b.append(name);
            b.append("\">");
            b.append(name);
            b.append("</a></li>\n");
            getSymbols().put("Breadcrumb", b.toString());
        } else {
            getSymbols().put("Breadcrumb", "");
        }
    }


    /**
     * Find the component that matches the name set in this builder.
     */
    private ManagedComponent findComponent(String name,   List<ManagedComponent> components ) {
        ManagedComponent retval = null;
        if (name != null && components != null && components.size() > 0) {
            for (ManagedComponent cmpt : components) {
                if (name.equals(cmpt.getName())) {
                    retval = cmpt;
                    break;
                }
            }
        }
        return retval;
    }

}
