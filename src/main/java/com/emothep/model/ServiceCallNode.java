package com.emothep.model;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import com.wm.app.b2b.server.BaseService;

public class ServiceCallNode {

    /** Service Name */
    private String sN;

    /** package Name */
    private String pN;

    /** Service Type */
    private String sT;

    /** Response Time */
    private double rT;

    /** Children total elapsed time */
    private double cRT;

    /** Thread level CPU time */
    private double tCPU;

    /** Service executed time in UTC */
    private long eT;

    /** Service call node children */
    private List<ServiceCallNode> children;

    /** Service execution start time */
    private transient long serviceStartTime;

    /** Thread level CPU start time */
    private transient long tCPUStartTime;

    /** Parent reference to create hierarchy */
    private transient ServiceCallNode parentServiceNode = null;

    /**
     * Constructor which will be called while creating service call node for top
     * level service.
     *
     * @param currentBaseService
     */
    public ServiceCallNode(BaseService currentBaseService) {
        this.pN = currentBaseService.getPackageName();
        this.sN = currentBaseService.getNSName().getFullName();
        this.sT = currentBaseService.getServiceType().getType();
        this.children = new ArrayList<ServiceCallNode>(1);
        this.serviceStartTime = System.nanoTime();
        this.tCPUStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        this.setTimeOfExecution(System.currentTimeMillis());
    }

    /**
     * Constructor which will be called while creating child service.
     *
     * @param currentBaseService
     * @param parentServiceNode
     */
    public ServiceCallNode(BaseService currentBaseService, ServiceCallNode parentServiceNode) {
        this(currentBaseService);

        this.parentServiceNode = parentServiceNode;
        if (this.parentServiceNode != null) {
            this.parentServiceNode.addChild(this);
        }
    }

    /**
     * This method can be used to add child service node to this node.
     *
     * @param childServiceNode
     */
    public void addChild(ServiceCallNode childServiceNode) {
        this.children.add(childServiceNode);
    }

    /**
     * This method is invoked when the service [parent or child] execution
     * completes. On invoke, this will compute the time taken for the service
     * execution.
     *
     * @return rT - response time
     */
    public double endService() {
        rT = System.nanoTime() - serviceStartTime;
        tCPU = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - tCPUStartTime;
        return rT;
    }

    public String getPackageName() {
        return this.pN;
    }

    public String getServiceName() {
        return this.sN;
    }

    public String getServiceType() {
        return this.sT;
    }

    public double getThreadLevelCPUTime() {
        return this.tCPU;
    }

    public void setChildrenResponseTime(double childrenRT) {
        this.cRT = childrenRT;
    }

    public double getChildrenResponseTime() {
        return this.cRT;
    }

    public ServiceCallNode getParentNode() {
        return this.parentServiceNode;
    }

    public long getTimeOfExecution() {
        return eT;
    }

    public void setTimeOfExecution(long eT) {
        this.eT = eT;
    }
}