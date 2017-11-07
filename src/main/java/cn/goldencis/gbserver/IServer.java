package com.git.wuqf.sip.demos.server;

import org.xml.sax.SAXException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;

public interface IServer {
    void onFirstRegister(Request request) throws ParseException, InvalidArgumentException, SipException;

    void onSecondRegister(Request request) throws ParseException, InvalidArgumentException, SipException;

    void onHeartBeat(Request request) throws ParseException, InvalidArgumentException, SipException, IOException, SAXException, ParserConfigurationException;

    void onQueryCamera() throws ParseException, InvalidArgumentException, SipException;
}
