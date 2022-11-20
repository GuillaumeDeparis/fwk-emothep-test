package com.emothep.serviceMock;


import java.io.Serializable;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;

/**
 * The interface that is needed for mocking service calls with a class methods. If the mock object
 * encapsulates a MockDataFactry object then at runtime the service call is mocked out and the
 * {@link #createData(BaseService, IData, ServiceStatus)} method is called to simulate the output of
 * the service.
 *
 * @author Rupinder Singh
 */
public interface MockDataFactory extends Serializable
{
    /**
     * Method that is called to create the pipeline output for the mocked service. The
     * {@link ServiceInterceptor} object registered with the webMethods Integration Server is responsible
     * for calling this method to simulate the service output when the service is mocked by an implementation
     * of this object.
     * In addition to the parameters passed, the methods can also use the
     * {@link com.wm.app.b2b.server.InvokeState com.wm.app.b2b.server.InvokeState}
     * class to get additional state information.
     *
     * @param baseService the service details
     * @param pipeline the current pipeline at service call time
     * @param status the service status
     * @return the IData that should simulate the service call
     */
    public IData createData(BaseService baseService, IData pipeline, ServiceStatus status) throws Exception;
}
