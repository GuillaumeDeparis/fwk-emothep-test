package com.emothep.serviceMock;

import java.util.ResourceBundle;

public class ServiceMockMessages
{
    private static final String bundleName = "com.emothep.serviceMock.serviceMockMessages"; //$NON-NLS-1$

    private static final ResourceBundle resourceBundle;

    public static String exceptionMockedLogMessage;
    public static String missingMockObjectParameter;
    public static String invalidDatatypeForMockObject;
    public static String factoryMockedLogMessage;
    public static String foundClass;
    public static String loadedClass;
    public static String pipelineMockedLogMessage;
    public static String recursiveMock;
    public static String serviceMockedLogMessage;
    public static String missingServiceParameter;
    public static String unrecognizedMockObject;
    public static String logMessagePrefix;

    public static String javaAssistLibNotFound;
    public static String toolsJarLibNotFound;

    static
    {
        resourceBundle = ResourceBundle.getBundle(bundleName);
        exceptionMockedLogMessage = resourceBundle.getString("com.wm.ps.serviceMock.exceptionMockedLogMessage");
        missingMockObjectParameter = resourceBundle.getString("com.wm.ps.serviceMock.missingMockObjectParameter");
        invalidDatatypeForMockObject = resourceBundle.getString("com.wm.ps.serviceMock.invalidDatatypeForMockObject");
        factoryMockedLogMessage = resourceBundle.getString("com.wm.ps.serviceMock.factoryMockedLogMessage");
        foundClass = resourceBundle.getString("com.wm.ps.serviceMock.foundClass");
        loadedClass = resourceBundle.getString("com.wm.ps.serviceMock.loadedClass");
        pipelineMockedLogMessage = resourceBundle.getString("com.wm.ps.serviceMock.pipelineMockedLogMessage");
        recursiveMock = resourceBundle.getString("com.wm.ps.serviceMock.recursiveMock");
        serviceMockedLogMessage = resourceBundle.getString("com.wm.ps.serviceMock.serviceMockedLogMessage");
        missingServiceParameter = resourceBundle.getString("com.wm.ps.serviceMock.missingServiceParameter");
        unrecognizedMockObject = resourceBundle.getString("com.wm.ps.serviceMock.unrecognizedMockObject");
        logMessagePrefix = resourceBundle.getString("com.wm.ps.serviceMock.logMessagePrefix");

        javaAssistLibNotFound = resourceBundle.getString("javaAssistLibNotFound");
        toolsJarLibNotFound = resourceBundle.getString("toolsJarLibNotFound");

    }

    private ServiceMockMessages()
    {
    }
}
