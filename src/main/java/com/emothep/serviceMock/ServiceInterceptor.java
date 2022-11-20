package com.emothep.serviceMock;


import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

import com.emothep.servicemock;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.ISRuntimeException;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Session;
import com.wm.app.b2b.server.User;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSService;
import com.wm.util.JournalLogger;
import com.wm.util.ServerException;

/**
 *
 * This class provides the ability to extend the webMethods Integration Server version 6.1 using the InvokeChainProcessor processing chain extension mechanism. The extension adds the capability to
 * intercept and replace service calls at runtime. This is useful for mocking service calls in automated test cases.
 *
 * The class needs to be registered with the webMethods Integration Server by adding the following line in the WM_HOME/config/invokemanager.cnf file where WM_HOME is the install directory of the
 * webMethods Integration Server.
 *
 * <pre>
 * <code>
 *
 *  &lt;record javaclass=&quot;com.wm.data.ISMemDataImpl&quot;&gt;
 *    &lt;array name=&quot;processorArray&quot; type=&quot;value&quot; depth=&quot;1&quot;&gt;
 *      &lt;value&gt;com.wm.serviceMock.ServiceInterceptor&lt;/value&gt;
 *    &lt;/array&gt;
 *  &lt;/record&gt;
 *
 * </code>
 * </pre>
 *
 * The class needs to be loaded by the webMethods Integtration Server's instance classloader and not by any package classloader and, as such, should be placed in the integration server's classpath.
 * The best way to do that is to package it in a jar file and place the jar file in the WM_HOME/lib/jars.
 *
 * Any code can setup to intercept a subsequent service call by using the method {@link #setupInterception(String, String, MockObject)} before executing the code that makes the service call. The
 * ServiceInterceptor can :
 *
 * <br>
 * <br>
 * 1. Throw an Exception provided. <br>
 * 2. Return a fixtured pipeline that was provided. <br>
 * 3. Invoke a mock service instead. <br>
 * 4. Invoke a method from a java class that implements the {@link servicemock.MockDataFactory} interface. The class must have a no parameter public constructor to instantiate it. The
 * {@link servicemock.MockDataFactory#createData(BaseService, IData, ServiceStatus)} method is called to create a pipeline object as mock for the the service output. <br>
 * <br>
 *
 * Service interception can be removed for a particular service by using the method {@link #clearInterception(String, String)}
 *
 * The method {@link #clearAllInterception()} removes service interception for all services. There are a couple of methods {@link #enableInterception()} and {@link #disableInterception()} that do not
 * impact the list of service to be intercepted but can globally turn on/off service interception.
 *
 * Service Interception settings are non-persistent and do not survive server restarts. The service interception has three different scopes.
 *
 * <br>
 * <br>
 * 1. Session : The intcerption is local to the session where it is setup. <br>
 * 2. User : The interception is local to all the sessions for the user setting it up. <br>
 * 3. Server : Works globally for all sessions for all users. <br>
 * <br>
 *
 * @author Rupinder Singh
 * @see com.wm.app.b2b.server.invoke.InvokeChainProcessor
 * @since webMethods Integration Server 6.1
 */
public class ServiceInterceptor implements InvokeChainProcessor
{

    private static final String svcsKey = "__servicesToIntercept"; //$NON-NLS-1$
    private static final String globalKey = "__global"; //$NON-NLS-1$

    /*
     * global setting for enabling or disabling service interception
     */
    private boolean enabled = true;

    /*
     * the list of services that have been setup for interception
     */
    private Hashtable<String,Hashtable<String,MockObject>>  interceptedSvcsTable;
    private Hashtable<String,Hashtable<String,MockObject>>  interceptedSvcsParmsTable;

    /*
     * Constants for valid scope values.
     */

    /**
     * Constant indicating session scope
     */
    public static final String scopeSession = "session"; //$NON-NLS-1$

    /**
     * Constant indicating user scope
     */
    public static final String scopeUser = "user"; //$NON-NLS-1$

    /**
     * Constants indicating server scope
     */
    public static final String scopeServer = "server"; //$NON-NLS-1$

    private static ServiceInterceptor licenseInstance;

    /**
     * The default constructor that just calls the init method in the class.
     */
    private ServiceInterceptor()
    {
        init();
    }

    public static ServiceInterceptor getInstance(){

        if (licenseInstance == null) {
            synchronized (ServiceInterceptor.class) {
                if(licenseInstance == null){
                    licenseInstance = new ServiceInterceptor();
                }
            }
        }
        return licenseInstance;


    }
    /**
     * Initializes the list of services to be intercepted by creating a new list.
     */
    private void init()
    {
        interceptedSvcsTable = new Hashtable<>();
        interceptedSvcsParmsTable  = new Hashtable<>();

    }

    /**
     * Globally enable service interception. This is only needed to enable interception after it has been disabled by calling {@link #disableInterception()}. By default, interception is enabled. This
     * uses the existing list of services to be intercepted.
     */
    public synchronized void enableInterception()
    {
        enabled = true;
    }

    /**
     * Globally disable service interception. Once disabled, service interception would have to be explicitly enabled using {@link #enableInterception()}. This does not clear the list of services to be
     * intercepted.
     *
     */
    public synchronized void disableInterception()
    {
        enabled = false;
    }

    /**
     * Setup interception for a service. The interception is handled by using the interceptionObject using the following strategy : <li>if the object is an instance of an IData object then the service
     * returns with the object as the output from the service. The actual service is never called. <li>if the object is an instance of an Exception object then a wrapper ServiceException is thrown as if
     * the service failed. <li>if the object is an instance of String then the it is assumed to be the fully-qualified name of an alternate service to be invoked and that service is invoked instead of
     * the service being intercepted.
     *
     * @param scope
     *          the scope in which the interception is to be setup.
     * @param serviceName
     *          the fully-qualified name of the service to setup interception for.
     * @param interceptorObject
     *          the MockObject that is used for interception.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public synchronized  void setupInterception(String scope, String serviceName, MockObject interceptorObject)
    {
        if (scopeSession.equalsIgnoreCase(scope))
        {
            Session session = InvokeState.getCurrentSession();
            Hashtable servicesToIntercept = (Hashtable) session.get(svcsKey);
            if (servicesToIntercept == null)
            {
                servicesToIntercept = new Hashtable();
                session.put(svcsKey, servicesToIntercept);
            }

            servicesToIntercept.put(serviceName, interceptorObject);
            return;
        }

        String key = globalKey;
        if (scopeUser.equalsIgnoreCase(scope))
        {
            key = InvokeState.getCurrentUser().getName();
        }

        Hashtable servicesToIntercept = (Hashtable) interceptedSvcsTable.get(key);
        if (servicesToIntercept == null)
        {
            servicesToIntercept = new Hashtable();
            interceptedSvcsTable.put(key, servicesToIntercept);
        }
        servicesToIntercept.put(serviceName, interceptorObject);
    }

    /**
     * Clear interception for a service.
     *
     * @param scope
     *          the scope in which the interception was setup.
     * @param serviceName
     *          the fully-qualified name of the service.
     */
    @SuppressWarnings({ "rawtypes" })
    public synchronized void clearInterception(String scope, String serviceName)
    {
        if (scopeSession.equalsIgnoreCase(scope))
        {
            Session session = InvokeState.getCurrentSession();
            Hashtable servicesToIntercept = (Hashtable) session.get(svcsKey);
            if (servicesToIntercept != null)
            {
                servicesToIntercept.remove(serviceName);
            }
        }
        else
        {
            String key = globalKey;

            if (scopeUser.equalsIgnoreCase(scope))
            {
                key = InvokeState.getCurrentUser().getName();
            }

            Hashtable servicesToIntercept = (Hashtable) interceptedSvcsTable.get(key);
            if (servicesToIntercept != null)
            {
                servicesToIntercept.remove(serviceName);
            }

            Hashtable interceptServicesParms = (Hashtable) interceptedSvcsParmsTable.get(key);
            if (interceptServicesParms != null)
            {
                interceptServicesParms.remove(serviceName);
            }
        }
    }

    /**
     * Clears the list of services to be intercepted from all the scopes.
     */
    public synchronized void clearAllInterception()
    {
        InvokeState.getCurrentSession().remove(svcsKey);
        interceptedSvcsTable.clear();
        interceptedSvcsParmsTable.clear();
    }

    /**
     * The method that provides the implementation of the InvokeChainProcessor interface. If the service is setup for interception it checks the type of the interceptor object and takes action
     * accordingly. In these cases, the actual service never gets called. If the service is not setup for interception it continues to process like it should. If it is mocked then the service call is
     * replaced by a call to alternate service, a method call a pipeline merge from fixtured data or an exception. The only exception to it is when a service is mocked by an alternate service and the
     * alternate service calls the mocked service instead. In such a case, the mocked service replaced by the call to the alternate service but when the alternate service calls the mocked service it is
     * allowed to go without being mocked.
     */
    @SuppressWarnings("rawtypes")
    public void process(Iterator chain, BaseService baseService, IData pipeline, ServiceStatus status) throws ServerException
    {
        if (enabled)
        {
            String serviceName = baseService.getNSName().getFullName();
            MockObject interceptorObject = getInterceptorObject(serviceName);
            if (interceptorObject != null)
            {
                switch (interceptorObject.getType())
                {
                    case MockObject.data:
                        logServerMessage(MessageFormat.format(ServiceMockMessages.pipelineMockedLogMessage, serviceName)); //1.5x
                        //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.pipelineMockedLogMessage, new Object[] { serviceName }));
                        // Simulate the service call by using the IData object as return values
                        IData mockPipeline = interceptorObject.getIData();
                        IDataUtil.merge(mockPipeline, pipeline);
                        status.setReturnValue(pipeline);
                        return;

                    case MockObject.error:
                        Exception exception = interceptorObject.getException();
                        logServerMessage(MessageFormat.format(ServiceMockMessages.exceptionMockedLogMessage, serviceName)); //1.5x
                        //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.exceptionMockedLogMessage, new Object[] { serviceName }));
                        if (exception instanceof ISRuntimeException)
                        {
                            // Simulate a transient error
                            throw (ISRuntimeException) exception;
                        }
                        else
                        {
                            // Simulate a service error
                            throw new ServerException(exception);
                        }

                    case MockObject.service:
                        if (isRecursive(serviceName))
                        {
                            logServerMessage(MessageFormat.format(ServiceMockMessages.recursiveMock, serviceName)); //1.5x
                            logServerMessage(MessageFormat.format(ServiceMockMessages.recursiveMock, new Object[] { serviceName }));
                            break;
                        }

                        // Call an alternate service to get service results
                        String mockService = interceptorObject.getServiceName();
                        if (mockService != null)
                        {
                            logServerMessage(MessageFormat.format(ServiceMockMessages.serviceMockedLogMessage, serviceName, mockService)); //1.5x
                            //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.serviceMockedLogMessage, new Object[] { serviceName, mockService }));
                            IData serviceParms = interceptorObject.getIData();
                            if (serviceParms != null)
                            {
                                IDataUtil.merge(serviceParms, pipeline);
                            }
                            // Delegate to the next processor in the chain as this takes care of
                            // other server activities too.
                            baseService = Namespace.getService(NSName.create(mockService));
                        }

                        break;

                    case MockObject.object:
                        if (isRecursive(serviceName))
                        {
                            logServerMessage(MessageFormat.format(ServiceMockMessages.recursiveMock, serviceName)); //1.5x
                            //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.recursiveMock, new Object[] { serviceName }));
                            break;
                        }

                        try
                        {
                            MockDataFactory dataFactory = interceptorObject.getDataFactory();
                            logServerMessage(MessageFormat.format(ServiceMockMessages.factoryMockedLogMessage, serviceName, dataFactory.getClass().getName())); //1.5x
                            //1.4x logServerMessage(MessageFormat.format(ServiceMockMessages.factoryMockedLogMessage, new Object[] { serviceName, dataFactory.getClass().getName() }));
                            mockPipeline = dataFactory.createData(baseService, pipeline, status);
                            IDataUtil.merge(mockPipeline, pipeline);
                            status.setReturnValue(pipeline);
                            return;
                        }
                        catch (Exception e)
                        {
                            throw new ServerException(e);
                        }

                    default:
                        throw new ServerException(ServiceMockMessages.unrecognizedMockObject);
                }
            }
        }
        // execute the service normally
        if (chain.hasNext())
        {
            ((InvokeChainProcessor) chain.next()).process(chain, baseService, pipeline, status);
        }
    }

    /**
     *
     * @param serviceName
     *          the service for which the interceptorObject is required
     * @return the object or null if not setup
     */
    @SuppressWarnings("rawtypes")
    private MockObject getInterceptorObject(String serviceName)
    {
        Session session = InvokeState.getCurrentSession();
        MockObject interceptorObject = null;

        if (session != null && session.containsKey(svcsKey))
        {
            Hashtable servicesTable = (Hashtable) session.get(svcsKey);
            interceptorObject = (MockObject) servicesTable.get(serviceName);
        }

        if (interceptorObject == null)
        {
            User user = InvokeState.getCurrentUser();

            if (user != null)
            {
                Hashtable servicesTable = (Hashtable) interceptedSvcsTable.get(user.getName());
                if (servicesTable != null)
                {
                    interceptorObject = (MockObject) servicesTable.get(serviceName);
                }
            }

            if (interceptorObject == null)
            {
                Hashtable servicesTable = (Hashtable) interceptedSvcsTable.get(globalKey);
                if (servicesTable != null)
                {
                    interceptorObject = (MockObject) servicesTable.get(serviceName);
                }
            }
        }
        return interceptorObject;
    }

    /**
     *
     * @return the array of services that are setup for interception
     */
    @SuppressWarnings("rawtypes")
    public Object[] getInterceptedServices()
    {
        Session session = InvokeState.getCurrentSession();
        String[] sessionServices = null;
        String[] userServices = null;
        String[] globalServices = null;

        if (session != null && session.containsKey(svcsKey))
        {
            Hashtable servicesTable = (Hashtable) session.get(svcsKey);
            Object[] sessionServicesObj = servicesTable.keySet().toArray();
            sessionServices = new String[sessionServicesObj.length];
            for (int i = 0; i < sessionServicesObj.length; i++)
            {
                sessionServices[i] = (String) sessionServicesObj[i];
            }
        }

        User user = InvokeState.getCurrentUser();

        if (user != null)
        {
            Hashtable servicesTable = (Hashtable) interceptedSvcsTable.get(user.getName());
            if (servicesTable != null)
            {
                Object[] userServicesObj = servicesTable.keySet().toArray();
                userServices = new String[userServicesObj.length];
                for (int i = 0; i < userServicesObj.length; i++)
                {
                    userServices[i] = (String) userServicesObj[i];
                }
            }
        }

        Hashtable servicesTable = (Hashtable) interceptedSvcsTable.get(globalKey);
        if (servicesTable != null)
        {
            Object[] globalServicesObj = servicesTable.keySet().toArray();
            globalServices = new String[globalServicesObj.length];
            for (int i = 0; i < globalServicesObj.length; i++)
            {
                globalServices[i] = (String) globalServicesObj[i];
            }
        }

        return new Object[] { globalServices, userServices, sessionServices };
    }

    /*
     * Checks the callStack to make sure the service is not calling itself directly or indirectly
     */
    @SuppressWarnings("rawtypes")
    private boolean isRecursive(String serviceName)
    {
        InvokeState state = InvokeState.getCurrentState();
        Stack callStack = state.getCallStack();
        int index = -1;
        for (int i = 0; i < callStack.size(); i++)
        {
            NSService svcEntry = (NSService) callStack.elementAt(i);
            if (svcEntry.getNSName().getFullName().equals(serviceName))
            {
                index = i;
                break;
            }
        }

        if (index >= 0 && index < callStack.size() - 1)
        {
            return true;
        }
        return false;
    }

    private void logServerMessage(String message)
    {
        try
        {
            JournalLogger.log(JournalLogger.DEBUG, JournalLogger.FAC_FLOW_SVC, JournalLogger.DEBUG, ServiceMockMessages.logMessagePrefix, message);
        }
        catch (Exception e)
        {
            // Ignore message if there is a logging message
        }
    }
}
