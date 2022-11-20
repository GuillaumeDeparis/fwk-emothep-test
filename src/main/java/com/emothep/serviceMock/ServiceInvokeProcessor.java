package com.emothep.serviceMock;

import java.util.Iterator;
import java.util.Map;

import com.emothep.model.ServiceCallNode;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;

/**
 * Class Name : ServiceInvokeProcessor
 *
 * Description : The subscriber class whose object is added to the invoke chain
 * to get the notification from Integration Server when the service execution
 * gets triggered and before the actual service invocation. When called it
 * stores the execution statistics of the service.
 */
public class ServiceInvokeProcessor implements InvokeChainProcessor {

    private String serviceTopicName;


    private Map<String, Object> includedPkgsMap;

    private ThreadLocal<ServiceCallNode> threadLocal;

    /**
     * Constructor.
     *
     * @param serviceTopicName
     * @param includedPkgsMap
     */
    public ServiceInvokeProcessor(String serviceTopicName,
                                  Map<String, Object> includedPkgsMap) {
        this.threadLocal = new ThreadLocal<ServiceCallNode>();
        this.serviceTopicName = serviceTopicName;
        this.includedPkgsMap = includedPkgsMap;
    }

    /**
     * Setter method to set the selected packages which needs to be monitored.
     * Those services which are part of the selected packages will be considered
     * for collecting the statistics. Whereas the services which are part of
     * other packages will be ignored.
     *
     * @param includedPkgsMap
     */
    public void setIncludedPkgMap(Map<String, Object> includedPkgsMap) {
        this.includedPkgsMap = includedPkgsMap;
    }

    @Override
    public void process(@SuppressWarnings("rawtypes") Iterator chain, BaseService baseService, IData pipeline,
                        ServiceStatus status) throws ServerException {
        double timeTaken;

        // PRE-PROCESSING
        if (status.isTopService()) {
            threadLocal.set(new ServiceCallNode(baseService));
        } else {
            threadLocal.set(new ServiceCallNode(baseService, threadLocal.get()));
        }

        // EXECUTION :: continuing the chain
        try {
            // required by InvokeChainProcessor definition
            if (chain.hasNext()) {
                ((InvokeChainProcessor) chain.next()).process(chain, baseService, pipeline, status);
            }
        } finally {
            // What do you want to do here
        }

        // POST-PROCESSING
        ServiceCallNode currentNode = threadLocal.get();
        timeTaken = currentNode.endService();
        ServiceCallNode parent = currentNode.getParentNode();
        if (parent != null) {
            parent.setChildrenResponseTime(timeTaken);
            threadLocal.set(parent);
        } else {
            if (includedPkgsMap.containsKey(currentNode.getPackageName())) {
                System.out.println("Je passe dans le package");
            }
        }
    }
}
