package com.emothep;

import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import java.io.IOException;
import com.emothep.serviceMock.MockObject;
import com.emothep.serviceMock.MockDataFactory;
import com.wm.data.IDataFactory;
import com.wm.data.IDataCursor;
import com.emothep.serviceMock.ServiceMockMessages;
import com.wm.data.IDataUtil;
import com.wm.app.b2b.server.ServiceException;
import com.emothep.serviceMock.ServiceInterceptor;
import com.wm.data.IData;

public final class servicemock
{
    static final servicemock _instance;

    static servicemock _newInstance() {
        return new servicemock();
    }

    static servicemock _cast(final Object o) {
        return (servicemock)o;
    }

    public static final void clearAllMocks(final IData pipeline) throws ServiceException {
        ServiceInterceptor.getInstance().clearAllInterception();
    }

    public static final void clearMock(final IData pipeline) throws ServiceException {
        final IDataCursor pipelineCursor = pipeline.getCursor();
        final String serviceName = IDataUtil.getString(pipelineCursor, "service");
        if (serviceName == null || serviceName.length() == 0) {
            throw new ServiceException(ServiceMockMessages.missingServiceParameter);
        }
        String scope = null;
        if (pipelineCursor.first("scope")) {
            scope = IDataUtil.getString(pipelineCursor);
        }
        if (!isScopeValid(scope)) {
            scope = "session";
        }
        ServiceInterceptor.getInstance().clearInterception(scope, serviceName);
        pipelineCursor.destroy();
    }

    public static final void getMockedServices(final IData pipeline) throws ServiceException {
        final IDataCursor cursor = pipeline.getCursor();
        final Object[] mockedServices = ServiceInterceptor.getInstance().getInterceptedServices();
        if (mockedServices != null) {
            IDataUtil.put(cursor, "mockedServices", (Object)IDataFactory.create(new Object[][] { { "session", mockedServices[2] }, { "user", mockedServices[1] }, { "server", mockedServices[0] } }));
        }
        cursor.destroy();
    }

    public static final void suspendMocks(final IData pipeline) throws ServiceException {
        ServiceInterceptor.getInstance().disableInterception();
    }

    public static final void resumeMocks(final IData pipeline) throws ServiceException {
        ServiceInterceptor.getInstance().enableInterception();
    }

    public static final void loadMock(final IData pipeline) throws ServiceException {
        final IDataCursor pipelineCursor = pipeline.getCursor();
        final String serviceName = IDataUtil.getString(pipelineCursor, "service");
        if (serviceName == null || serviceName.length() == 0) {
            throw new ServiceException(ServiceMockMessages.missingServiceParameter);
        }
        final Object mockBaseObject = IDataUtil.get(pipelineCursor, "mockObject");
        if (mockBaseObject == null) {
            throw new ServiceException(ServiceMockMessages.missingMockObjectParameter);
        }
        if (!(mockBaseObject instanceof IData) && !(mockBaseObject instanceof Exception) && !(mockBaseObject instanceof String) && !(mockBaseObject instanceof MockDataFactory) && !(mockBaseObject instanceof MockObject)) {
            throw new ServiceException(ServiceMockMessages.invalidDatatypeForMockObject);
        }
        String scope = null;
        if (pipelineCursor.first("scope")) {
            scope = IDataUtil.getString(pipelineCursor);
        }
        if (!isScopeValid(scope)) {
            scope = "session";
        }
        MockObject mockObject = null;
        if (mockBaseObject instanceof MockObject) {
            mockObject = (MockObject)mockBaseObject;
        }
        else if (mockBaseObject instanceof String) {
            mockObject = new MockObject((String)mockBaseObject, IDataUtil.getIData(pipelineCursor, "parms"));
        }
        else if (mockBaseObject instanceof Exception) {
            mockObject = new MockObject((Exception)mockBaseObject);
        }
        else if (mockBaseObject instanceof IData) {
            mockObject = new MockObject((IData)mockBaseObject);
        }
        else if (mockBaseObject instanceof MockDataFactory) {
            try {
                mockObject = new MockObject((MockDataFactory)mockBaseObject);
            }
            catch (IOException ioe) {
                throw new ServiceException((Throwable)ioe);
            }
        }
        ServiceInterceptor.getInstance().setupInterception(scope, serviceName, mockObject);
        pipelineCursor.destroy();
    }

    public static final void startup(final IData pipeline) throws ServiceException {
        InvokeManager.getDefault().registerProcessor((InvokeChainProcessor)ServiceInterceptor.getInstance());
    }

    public static final void shutdown(final IData pipeline) throws ServiceException {
        InvokeManager.getDefault().unregisterProcessor((InvokeChainProcessor)ServiceInterceptor.getInstance());
    }

    private static boolean isScopeValid(final String scope) {
        return scope != null && (scope.equals("session") || scope.equals("user") || scope.equals("server"));
    }

    static {
        _instance = new servicemock();
    }
}
