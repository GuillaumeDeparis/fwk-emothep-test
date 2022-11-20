package com.emothep.test;

import com.emothep.serviceMock.ServiceInvokeProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmTestCase {
    private static EmTestCase INSTANCE;
    private boolean isRunning = false;

    private ServiceInvokeProcessor processor;
    private Map<String, Object> includedPkgsMap;

    public static EmTestCase getInstance() {
        if (INSTANCE == null) {
            synchronized (EmTestCase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new EmTestCase();
                }
            }
        }

        return INSTANCE;
    }

    private EmTestCase(){
        includedPkgsMap = new HashMap<>();
    }

    public void startProfiler() {
        if (!isRunning) {
            processor = new ServiceInvokeProcessor("pub.flow:debugLog",
                    includedPkgsMap);

            InvokeManager manager = InvokeManager.getDefault();

            manager.registerProcessor(processor);

            isRunning = true;
        }
    }

    public void stopProfiler() {
        if (isRunning) {
            InvokeManager manager = InvokeManager.getDefault();
            manager.unregisterProcessor(processor);

            isRunning = false;
        }
    }

    public boolean isRunning(){
        return isRunning;
    }

}
