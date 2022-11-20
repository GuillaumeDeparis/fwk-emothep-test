package com.emothep.test;

import com.wm.app.b2b.client.Query;
import com.wm.app.b2b.client.ServiceException;
import com.wm.app.b2b.client.ns.ContextWrapper;
import com.wm.app.b2b.util.ServerIf;
import com.wm.driver.comm.b2b.WmConnection;
import com.wm.driver.comm.b2b.WmConnectionFactory;
import com.wm.driver.comm.b2b.http.HTTPConnection;
import com.wm.driver.comm.b2b.http.HTTPConnectionParms;
import com.wm.driver.comm.b2b.http.SessionHTTPLink;
import com.wm.util.comm.Connection;

import java.lang.reflect.Field;
import java.util.Date;

public class EmothepWmConnection {
    private String host = "localhost";
    private String port = "5555";
    private String user = "Administrator";
    private String password = "manage";
    private HTTPConnectionParms connParms;
    private WmConnection gContext;
    private Query gQuery;
    private ContextWrapper gContextW;
    private boolean connected = false;
    private transient Date lastAccessed;
    private boolean connectionInProgress;

    public EmothepWmConnection(String host, String port, String user, String password){
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public void connect() throws ServiceException {
        connectionInProgress = true;
        if (connected){
            connectionInProgress = false;
            return;
        }
        try {
            connParms = new HTTPConnectionParms("localhost:5555");
            connParms.setUserCredentials("Administrator", "manage");
            connParms.setUseSession(true);

            gContext = WmConnectionFactory.create(connParms);
            gContext.setLinkReady();

            gQuery = new Query(gContext);
            gQuery.setContext(gContext);

            gContextW = new ContextWrapper(gContext);
        }
        catch(Exception e){
            gContext = null;
            gContextW = null;
            gQuery = null;
            connected = false;
            connectionInProgress = false;
            throw e;
        }
        connected = true;
        connectionInProgress = false;
    }

    public void disconnect(){
        connected = false;
        if (gContext != null){
            gContext.registerMessageHandler(ServerIf.KEY_EVENT_HTTP_WAITFOREVENT,null);
            gContext.disconnect();
        }
        gContextW = null;
        gQuery = null;
        connected = false;
        connectionInProgress = false;
    }

    public void associateConnectionModels(WmConnection wmconnetion, Query query, boolean isconnected) {
        gContext = wmconnetion;
        gQuery = query;
        gContextW = new ContextWrapper(wmconnetion);
        connected = isconnected;
        updateLastAccessed();
    }

    public void updateLastAccessed() {
        lastAccessed = new Date();
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public String getHostname() {
        if (host == null)
            return ""; //$NON-NLS-1$
        return host;
    }

    public String getSessionId(){
        String sessionId = null;
        try {
            Connection connection_object = (Connection) getFieldValueForced(HTTPConnection.class, "conn", gContext); //$NON-NLS-1$
            SessionHTTPLink sessionhttplink_object = (SessionHTTPLink) getFieldValueForced(Connection.class, "link", connection_object); //$NON-NLS-1$
            sessionId = (String) getFieldValueForced(SessionHTTPLink.class, "sessionId", sessionhttplink_object); //$NON-NLS-1$
            return sessionId;

        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        return sessionId;
    }

    private Object getFieldValueForced(Class<?> clazz, String fieldName, Object instance) {
        Field declaredField = null;
        try {
            declaredField = clazz.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(instance);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return declaredField;
    }

    String getPassword() {
        return password;
    }


    /**
     * Set the port number
     *
     * @param port
     *            the port number
     */
    public void setPort(String port) {
        if (this.port != port) {
            disconnect();
            this.port = port;
        }
    }

    public String getPort() {
        if (port == null)
            return null; //$NON-NLS-1$
        return port;
    }


    /**
     * Get the name of the server
     *
     * @return the server name
     */

    public String getServer() {
        return host;
    }


    /**
     * Get the name of the user to connect to the server.
     *
     * @return the user name
     */
    public String getUsername() {
        return user;
    }


    public boolean isConnected() {
        return connected;
    }


    public WmConnection getWmConnection() {
        return gContext;
    }

    public void getWmConnection(WmConnection gContext) {
        this.gContext = gContext;
    }

    public Query getQuery() {
        return gQuery;
    }

    public ContextWrapper getContextWrapper() {
        return gContextW;
    }
}
