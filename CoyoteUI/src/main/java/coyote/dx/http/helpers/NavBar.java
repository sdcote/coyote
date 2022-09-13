package coyote.dx.http.helpers;

import coyote.commons.network.http.HTTPSession;
import coyote.commons.template.SymbolTable;
import coyote.commons.template.Template;


/**
 * This is a helper class for generating the top navigational bar consistently between responders.
 */
public class NavBar {
    private final HTTPSession session;
    private String currentPage = HOME;
    SymbolTable symbolTable = null;

    public static final String HOME = "Home";

    public NavBar(HTTPSession session){
        this.session = session;
    }

    public NavBar Symbols(SymbolTable symbols) {
        symbolTable = symbols;
        return this;
    }
    public NavBar CurrentPage(String location){
        return this;
    }

    public String build(){
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
                "          <i class=\"far fa-bell\"></i>\n" +
                "          <span class=\"badge badge-warning navbar-badge\">15</span>\n" +
                "        </a>\n" +
                "        <div class=\"dropdown-menu dropdown-menu-lg dropdown-menu-right\">\n" +
                "          <span class=\"dropdown-item dropdown-header\">15 Notifications</span>\n" +
                "          <div class=\"dropdown-divider\"></div>\n" +
                "          <a href=\"#\" class=\"dropdown-item\">\n" +
                "            <i class=\"fas fa-envelope mr-2\"></i> 4 new messages\n" +
                "            <span class=\"float-right text-muted text-sm\">3 mins</span>\n" +
                "          </a>\n" +
                "          <div class=\"dropdown-divider\"></div>\n" +
                "          <a href=\"#\" class=\"dropdown-item\">\n" +
                "            <i class=\"fas fa-users mr-2\"></i> 8 friend requests\n" +
                "            <span class=\"float-right text-muted text-sm\">12 hours</span>\n" +
                "          </a>\n" +
                "          <div class=\"dropdown-divider\"></div>\n" +
                "          <a href=\"#\" class=\"dropdown-item\">\n" +
                "            <i class=\"fas fa-file mr-2\"></i> 3 new reports\n" +
                "            <span class=\"float-right text-muted text-sm\">2 days</span>\n" +
                "          </a>\n" +
                "          <div class=\"dropdown-divider\"></div>\n" +
                "          <a href=\"#\" class=\"dropdown-item dropdown-footer\">See All Notifications</a>\n" +
                "        </div>\n" +
                "      </li>\n" +
                "    </ul>\n" +
                "  </nav>\n" +
                " ");

        // Pre-process the text so unresolved tokens are easily identified and resolved.
        return Template.preProcess(b.toString(),symbolTable);
    }

}
