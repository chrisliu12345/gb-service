package cn.goldencis.gbclient;


import cn.goldencis.gbclient.core.InitParameter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.TooManyListenersException;


public class DeviceInfoQueryAtHome {

    String localIp = "192.168.3.92";
    String serverIp = "192.168.3.100";
    String NVRIp = "192.168.3.161";
    String cameraIp = "192.168.3.248";

    int localPort = 5060;
    int serverPort = 5060;
    String serverUsername = "34020000002000000001";
    String realm = "34020000";
    String password = "admin123";
    private Client client;

    @Test
    public void testQueryNVRDirectlyAtHome() {
        try {
            String localUsername = "34020000001110000001";
            String NVRUsername = "34020000001120000001";
            InitParameter initParameter = new InitParameter(localIp, serverIp, localPort, serverPort, localUsername, serverUsername, realm, password);
            client = new Client(initParameter);
            Response fr = client.firstRegister();
            Response sr = client.secondRegister(fr);
            Response NVRInfo = client.queryDeviceInfo(NVRUsername, NVRIp);
            Thread.sleep(3000);
        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Test
    public void testQueryHaikangCameraDirectlyAtHome() throws Exception {
        String localUsername = "34020000001120000001";
        String cameraUserName = "34020000001110000001";
        InitParameter initParameter = new InitParameter(localIp, serverIp, localPort, serverPort, localUsername, serverUsername, realm, password);
        client = new Client(initParameter);
        Response fr = client.firstRegister();
        Response sr = client.secondRegister(fr);
        try {
            Response cameraInfo = client.queryDeviceInfo(cameraUserName, cameraIp);
            Thread.sleep(3000);
        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
