package cn.goldencis.gbclient;

import cn.goldencis.gbclient.client.Transport;
import cn.goldencis.gbclient.core.InitParameter;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.TooManyListenersException;

public class Client implements IClient {

    private Transport transport;
    private InitParameter initParameter;

    public Client(InitParameter initParameter) throws InvalidArgumentException, SipException, TooManyListenersException, ParseException {
        this.initParameter = initParameter;
        transport = new Transport(initParameter);
    }

    public Response firstRegister() throws ParseException,
            InvalidArgumentException, SipException {
        Request request = transport.createFirstRegisterHeader();
        return transport.sendRequest(request);
    }

    public Response secondRegister(Response response) throws Exception {
        Request request = transport.createSecondRegisterHeader(response);
        return transport.sendRequest(request);
    }

    @Override
    public Response queryDeviceInfo(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>DeviceInfo</CmdType>\r\n" +
                "<SN>17431</SN>\r\n" +
                "<DeviceID>" + deviceUserName + "</DeviceID>\r\n" +
                "</Query>";
        Request request = transport.createQueryRequest(deviceUserName, deviceIp, sdpData);
        return transport.sendRequest(request);
    }

    @Override
    public Response queryDeviceList(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>Catalog</CmdType>\r\n" +
                "<SN>248</SN>\r\n" +
                "<DeviceID>" + initParameter.getServerUsername() + "</DeviceID>\r\n" +
                "</Query>";

        Request request = transport.createQueryRequest(deviceUserName, deviceIp, sdpData);
        return transport.sendRequest(request);
    }

    @Override
    public Response queryDeviceStatus(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>DeviceStatus</CmdType>\r\n" +
                "<SN>17431</SN>\r\n" +
                "<DeviceID>" + initParameter.getServerUsername() + "</DeviceID>\r\n" +
                "</Query>";
        Request request = transport.createQueryRequest(deviceUserName, deviceIp, sdpData);
        return transport.sendRequest(request);
    }

}
