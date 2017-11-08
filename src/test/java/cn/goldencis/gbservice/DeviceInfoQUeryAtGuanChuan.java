package cn.goldencis.gbservice;

import cn.goldencis.gbservice.core.InitParameter;

import javax.sip.message.Response;

public class DeviceInfoQUeryAtGuanChuan {
    public static void main(String[] args) {
        String localIp = "192.168.1.244";
        String serverIp = "//192.168.1.251";
        String deviceIp = "//192.168.1.248";

        int localPort = 5060;
        int serverPort = 5060;
        String serverUsername = "34020000001320000001";//
        String localUsername = "34020000001320000002";//
        String deviceUsername = "34020000001320000003";
        String realm = "34020000";
        String password = "admin123";

        InitParameter initParameter = new InitParameter(localIp, serverIp, localPort, serverPort, localUsername, serverUsername, realm, password);
        try {
            Client client = new Client(initParameter);
            Response fr = client.firstRegister();
            Response sr = client.secondRegister(fr);
            Thread.sleep(10000);
            Response deviceInfo = client.queryDeviceInfo(deviceUsername, deviceIp);
        } catch (Throwable e) {
            System.out.println("Problem initializing the SIP stack.");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
