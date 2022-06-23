package coyote.dx.reader;

import coyote.commons.StringUtil;
import coyote.dataframe.DataField;
import coyote.dataframe.DataFrame;
import coyote.dx.CDX;
import coyote.dx.ConfigTag;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;
import coyote.loader.log.Log;
import coyote.loader.log.LogMsg;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Reads entries from an LDAP service.
 *
 * <p>This is initially used in finding groups and configuration elements in  directory services. It has also been used
 * to discover the latest official printers and copiers in some environments. THere are more use cases, but these are
 * what drove the initial design decisions for this reader.</p>
 *
 * <p>A sample configuration follows:
 * <pre>
 *     "Reader" : {
 * 			"class" : "LdapReader",
 * 			"source" : "ldapserver.example.com",
 * 			"username": "juser",
 * 			"password": "53c4Et",
 * 			"name": "OU=Security Groups,DC=corp,DC=example,DC=com",
 * 			"filter": "(objectClass=group)",
 * 			"fields": [
 * 				"cn",
 * 				"description",
 * 				"displayName",
 * 				"info",
 * 				"name"
 * 			]
 *     },
 * </pre></p>
 *
 * <p>The name attribute is the name of the context or object to locate. </p>
 *
 * <p>The filter attribute is used to return only matching classes of entries.</p>
 *
 * <p>The files section list all the attribute names to include from the retrieved entry. These will be the fields in
 * the data frame that is passed through the transformation engine.</p>
 *
 * <p>Ever entry (data frame) will have at least a Name and FullName field regardless of the attribute names are in the
 * field section. This aides in development and debugging.</p>
 */
public class LdapReader extends AbstractFrameReader {

    private static final String RDN_TAG = "RDN";
    private static final String VALUE_TAG = "Value";
    private static final String DEFAULT_FILTER = "(objectClass=*)";

    // List of attributes to include in the dataframe
    private final List<String> attributeNames = new ArrayList<>();

    private final int pageSize = 100;
    private final List<DataFrame> frames = new ArrayList<>();
    String providerUrl = null;
    NamingEnumeration results = null;
    private LdapContext connection;
    private String searchFilter = null;
    private int index = 0;


    /**
     * @param cfg the configuration to set
     * @throws ConfigurationException if there is a problem with the configuration
     */
    @Override
    public void setConfiguration(Config cfg) throws ConfigurationException {
        super.setConfiguration(cfg);

        // Get the hostname(and port) of the LDAP server the source of our data
        String source = getString(ConfigTag.SOURCE);
        providerUrl = "ldap://" + source;

        // Filter the types of entries returned.
        searchFilter = getString(ConfigTag.FILTER);
        if (StringUtil.isBlank(searchFilter)) {
            searchFilter = DEFAULT_FILTER;
        }

        // Get the names of the attributes to retrieve from each entry
        Config fields = cfg.getSection(ConfigTag.FIELDS);
        if (fields != null) {
            for (DataField fld : fields.getFields()) {
                attributeNames.add(fld.getStringValue());
            }
        }

    }


    /**
     * @param context The transformation context in which this component should be opened.
     */
    @Override
    public void open(TransformContext context) {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        env.put(Context.SECURITY_PRINCIPAL, getString(ConfigTag.USERNAME));
        env.put(Context.SECURITY_CREDENTIALS, getString(ConfigTag.PASSWORD));

        try {
            connection = new InitialLdapContext(env, null);
            connection.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)});
            Log.debug("Connection successful: " + providerUrl);
        } catch (IOException | NamingException e) {
            context.setError(LogMsg.createMsg(CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), e.getClass().toString() + ": " + e.getMessage()).toString());
        }

        if (connection == null) {
            context.setError(LogMsg.createMsg(CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), "Could not connect to directory service at '" + providerUrl + "'").toString());
        } else {
            performSearch();
        }
    }


    /**
     * The performs the search and generates the results
     *
     * <p>This can take a long time for long results (e.g. all the groups in an organization).</p>
     */
    private void performSearch() {
        String name = getString(ConfigTag.NAME);
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setReturningAttributes(attributeNames.toArray(new String[0]));

        byte[] cookie = null;
        try {
            results = connection.search(name, searchFilter, searchControls);

            do {
                // Perform the search
                results = connection.search(name, searchFilter, searchControls);

                // Iterate over a batch of search results
                while (results != null && results.hasMore()) {
                    // Display an entry
                    SearchResult entry = (SearchResult) results.next();
                    if (entry != null) {
                        DataFrame frame = new DataFrame();
                        frame.add("Name", entry.getName());
                        frame.add("FullName", entry.getNameInNamespace());
                        Attributes attr = entry.getAttributes();
                        for (String attrName : attributeNames) {
                            Attribute at = attr.get(attrName);
                            if (at != null) {
                                if (at.size() > 0) frame.add(attrName, at.get(0).toString());
                                else frame.add(attrName, "");
                            } else {
                                frame.add(attrName, "");
                            }
                        }
                        frames.add(frame);
                    }
                }
                // Examine the paged results control response
                Control[] controls = connection.getResponseControls();
                if (controls != null) {
                    for (Control control : controls) {
                        if (control instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
                            cookie = prrc.getCookie();
                        }
                    }
                }
                // Re-activate paged results
                connection.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
            } while (cookie != null);
            Log.debug("Found " + frames.size() + " directory entries");
        } catch (NamingException | IOException e) {
            context.setError(LogMsg.createMsg(CDX.MSG, "Reader.could_not_read_from_source", getClass().getName(), e.getClass().toString() + ": " + e.getMessage()).toString());
        }
    }


    /**
     * @param context the context containing data related to the current transaction.
     * @return the next frame read or null if there are no more frames to process.
     */
    @Override
    public DataFrame read(TransactionContext context) {
        DataFrame retval = null;
        if (index < frames.size()) {
            retval = frames.get(index++);
        }
        if (eof()) {
            context.setLastFrame(true);
        }
        return retval;
    }


    /**
     * @return true if there are no more frames to read, false otherwise
     */
    @Override
    public boolean eof() {
        return index >= frames.size();
    }


    /**
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (NamingException e) {
                throw new IOException(e);
            }
            connection = null;
        }
    }

}
