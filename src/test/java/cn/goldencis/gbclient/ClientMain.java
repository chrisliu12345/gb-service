package cn.goldencis.gbclient;


import cn.goldencis.gbclient.core.InitParameter;

import javax.sip.message.Response;


public class ClientMain {

    public static void main(String[] args) {
        String localIp = "192.168.3.95";
        String serverIp = "192.168.3.100";
        String deviceIp = "192.168.3.162";

        int localPort = 5060;
        int serverPort = 5060;
        String serverUsername = "34020000002000000001";
        String localUsername = "34020000001310000001";
        String deviceUsername = "34020000001110000001";
        String realm = "34020000";
        String password = "admin123";

        InitParameter initParameter = new InitParameter(localIp, serverIp, localPort, serverPort, localUsername, serverUsername, realm, password);
        try {
            Client client = new Client(initParameter);
            Response fr = client.firstRegister();
            Response sr = client.secondRegister(fr);
            client.queryDeviceInfo(deviceUsername,serverIp);

        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }


}
