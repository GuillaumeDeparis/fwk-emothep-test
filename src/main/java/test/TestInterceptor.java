package test;

import com.softwareag.is.log.Log;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;

import java.util.Iterator;

public class TestInterceptor {
    public static final void serviceInterceptor(IData pipeline) throws ServiceException {
        InvokeManager.getDefault().registerProcessor(new InvokeChainProcessor() {
            public void process(Iterator chain, BaseService baseService, IData pipeline, ServiceStatus status) throws ServerException {
                String serviceName = baseService.getNSName().getFullName();
                if ( serviceName.equals("Zz_GDS.pub:test")) {

                    Log.debug("Interception du test avant debut");
                }
                if (chain.hasNext()) {
                    ((InvokeChainProcessor) chain.next()).process(chain, baseService, pipeline, status);
                }
            }
        });
    }
}