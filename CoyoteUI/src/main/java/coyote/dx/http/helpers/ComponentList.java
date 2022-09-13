package coyote.dx.http.helpers;

import coyote.commons.CronEntry;
import coyote.commons.DateUtil;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;
import coyote.dx.ScheduledBatchJob;
import coyote.loader.component.ManagedComponent;
import coyote.loader.log.Log;

import java.util.ArrayList;
import java.util.List;

public class ComponentList {

    List<ManagedComponent> components = new ArrayList<>();
    SymbolTable symbolTable = null;

    public ComponentList(List<ManagedComponent> components) {
        this.components = components;
    }


    public ComponentList Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }

    public String build() {
        StringBuffer b = new StringBuffer();
        b.append("      <!-- Default box -->\n" +
                "      <div class=\"card\">\n" +
                "        <div class=\"card-header\">\n" +
                "          <h3 class=\"card-title\">Projects</h3>\n" +
                "          <div class=\"card-tools\">\n" +
                "            <button type=\"button\" class=\"btn btn-tool\" data-card-widget=\"collapse\" title=\"Collapse\">\n" +
                "              <i class=\"fas fa-minus\"></i>\n" +
                "            </button>\n" +
//                "            <button type=\"button\" class=\"btn btn-tool\" data-card-widget=\"remove\" title=\"Remove\">\n" +
//                "              <i class=\"fas fa-times\"></i>\n" +
//                "            </button>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "        <div class=\"card-body p-0\">\n" +
                "          <table class=\"table table-striped projects\">\n" +
                "              <thead>\n" +
                "                  <tr>\n" +
                "                      <th style=\"width: 17%\">\n" +
                "                          ID\n" +
                "                      </th>\n" +
                "                      <th style=\"width: 17%\">\n" +
                "                          Name\n" +
                "                      </th>\n" +
                "                      <th style=\"width: 17%\">\n" +
                "                          Class\n" +
                "                      </th>\n" +
                "                      <th>\n" +
                "                          Started\n" +
                "                      </th>\n" +
                "                      <th style=\"width: 17%\">\n" +
                "                          Status\n" +
                "                      </th>\n" +
                "                      <th style=\"width: 17%\">\n" +
                "                          Schedule\n" +
                "                      </th>\n" +
                "                  </tr>\n" +
                "              </thead>\n" +
                "              <tbody>\n");

        for (ManagedComponent component : components) {
            b.append("                <tr>\n");
            b.append("                  <td>\n");
            b.append(component.getId());
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                  <td>\n");
            b.append(component.getName());
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                  <td>\n");
            b.append(component.getClass().getSimpleName());
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                  <td>\n");
            long started =component.getStartTime();
            if( started>0) b.append( DateUtil.formatSignificantElapsedTime((System.currentTimeMillis() - started) / 1000 ));
            else b.append("TBD");
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                  <td>\n");
            if( component.isActive()) b.append("Active");
            else  b.append("Inactive");
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                  <td>\n");
            if( component instanceof ScheduledBatchJob) b.append(((ScheduledBatchJob)component).getCronEntry().toString());
            else  b.append("N/A");
            b.append("\n");
            b.append("                  </td>\n");
            b.append("                </tr>\n");
        }

        b.append("              </tbody>\n" +
                "          </table>\n" +
                "        </div>\n" +
                "        <!-- /.card-body -->\n" +
                "      </div>\n" +
                "      <!-- /.card -->\n");
        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(), symbolTable);
    }

}
