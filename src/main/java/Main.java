import com.emothep.serviceMock.ServiceInvokeProcessor;
import com.emothep.test.EmTestCase;
import com.emothep.test.EmothepWmConnection;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.driver.comm.b2b.WmConnectionFactory;
import test.TestInterceptor;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args){
        //EmothepWmConnection c = new EmothepWmConnection("localhost","5555", "Administrator", "5555" );
        try {
            EmothepWmConnection c = new EmothepWmConnection("localhost","5555", "Administrator", "5555" );
            c.connect();
            c.getWmConnection();

        } catch (com.wm.app.b2b.client.ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
