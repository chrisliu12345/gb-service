package cn.goldencis.gbclient;


import cn.goldencis.gbclient.core.InitParameter;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;


public class ClientMain {

    public static void main(String[] args) {
        String localIp = "192.168.3.92";
        String serverIp = "192.168.3.60";
        String deviceIp = "192.168.3.162";

        int localPort = 5060;
        int serverPort = 5060;
        String serverUsername = "34020000001000000001";
        String localUsername = "34020000001310000001";
        String deviceUsername = "34020000001120000002";
        String realm = "34020000";//34020000
        String password = "admin123";

        InitParameter initParameter = new InitParameter(localIp, serverIp, localPort, serverPort, localUsername, serverUsername, realm, password);
        try {
            Client client = new Client(initParameter);
            Response fr = client.firstRegister();
            Response sr = client.secondRegister(fr);
            Response deviceInfo = client.queryDeviceInfo(deviceUsername, serverIp);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                        client.keepAlive();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (SipException e) {
                        e.printStackTrace();
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }


}
