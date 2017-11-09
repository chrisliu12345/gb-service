package cn.goldencis.gbclient;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

public interface IClient {

    Response queryDeviceInfo(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException;

    Response queryDeviceList(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException;

    Response queryDeviceStatus(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException;

    Response keepAlive(String sn) throws ParseException, SipException, InvalidArgumentException;

    Response queryDeviceInfoAsServer(String deviceUserName, String deviceIp) throws ParseException, SipException, InvalidArgumentException;
}
