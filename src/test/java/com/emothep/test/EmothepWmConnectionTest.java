package com.emothep.test;

import com.wm.app.b2b.client.ServiceException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmothepWmConnectionTest {

    @Test
    void connect()  {
        EmothepWmConnection c = new EmothepWmConnection("localhost","5555", "Administrator", "5555" );
        try {
            c.connect();
            c.getSessionId();
            c.disconnect();
        }
        catch(ServiceException e){
            System.out.println(e.getMessage());
        }

    }
}