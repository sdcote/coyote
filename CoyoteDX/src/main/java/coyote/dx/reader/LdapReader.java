package coyote.dx.reader;

import coyote.dataframe.DataFrame;
import coyote.dx.context.TransactionContext;
import coyote.dx.context.TransformContext;
import coyote.loader.cfg.Config;
import coyote.loader.cfg.ConfigurationException;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.util.Properties;


public class LdapReader extends AbstractFrameReader {

    private DirContext connection;


    /**
     * @param cfg the configuration to set
     * @throws ConfigurationException
     */
    @Override
    public void setConfiguration(Config cfg) throws ConfigurationException {
        super.setConfiguration(cfg);

    }


    /**
     * @param context The transformation context in which this component should be opened.
     */
    @Override
    public void open(TransformContext context) {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:10389");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin, ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");
        try {
            connection = new InitialDirContext(env);
            System.out.println("Hello World!" + connection);
        } catch (AuthenticationException ex) {
            System.out.println(ex.getMessage());
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param context the context containing data related to the current transaction.
     * @return the next frame read or null if there are no more frames to process.
     */
    @Override
    public DataFrame read(TransactionContext context) {
        return null;
    }


    /**
     * @return
     */
    @Override
    public boolean eof() {
        return false;
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
        }
    }


    public void getAllUsers() throws NamingException {
        String searchFilter = "(objectClass=inetOrgPerson)";
        String[] reqAtt = {"cn", "sn"};
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(reqAtt);

        NamingEnumeration users = connection.search("ou=users,ou=system", searchFilter, controls);

        SearchResult result = null;
        while (users.hasMore()) {
            result = (SearchResult) users.next();
            Attributes attr = result.getAttributes();
            String name = attr.get("cn").get(0).toString();
            System.out.println(attr.get("cn"));
            System.out.println(attr.get("sn"));
        }
    }

}
