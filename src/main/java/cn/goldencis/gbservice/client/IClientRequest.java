package cn.goldencis.gbservice.client;

import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;


public interface IClientRequest {
    Response sendRequest(Request request) throws SipException;


}
