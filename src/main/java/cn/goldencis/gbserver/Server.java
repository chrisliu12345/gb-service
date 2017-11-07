package com.git.wuqf.sip.demos.server;

import com.git.wuqf.sip.demos.security.DigestClientAuthenticationMethod;
import com.git.wuqf.sip.demos.processor.MessageProcessor;
import com.git.wuqf.sip.demos.util.XmlUtils;
import org.xml.sax.SAXException;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.UUID;

public class Server implements IServer, SipListener {


    private MessageProcessor messageProcessor;


    private String localIp = "192.168.3.93";
    private String remoteIp = "192.168.3.161";
    private int localPort = 5060;
    private String destPort = "5060";
    private String localUsername = "34020000002000000001";
    private String remoteUsername = "34020000001110000001";
    private String cameraUsername = "34020000001110000001";
    private String realm = "34020000";
    private String password = "admin123";
    private long callSeqNumber = 1;
    boolean isSend = false;
    private SipStack sipStack;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    DigestClientAuthenticationMethod digest = new DigestClientAuthenticationMethod();

    private XmlUtils xmlUtils = new XmlUtils();

    private String callId;

    /**
     * Here we initialize the SIP stack.
     */
    public Server()
            throws PeerUnavailableException, TransportNotSupportedException,
            InvalidArgumentException, ObjectInUseException,
            TooManyListenersException, ParserConfigurationException {

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "ClientMain");
        properties.setProperty("javax.sip.IP_ADDRESS", localIp);

        //DEBUGGING: Information will go to files
        //textclient.log and textclientdebug.log
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "textclient.txt");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "textclientdebug.log");

        sipStack = sipFactory.createSipStack(properties);
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        ListeningPoint tcp = sipStack.createListeningPoint(5060, "tcp");
        ListeningPoint udp = sipStack.createListeningPoint(5060, "udp");

        sipProvider = sipStack.createSipProvider(tcp);
        sipProvider.addSipListener(this);
        sipProvider = sipStack.createSipProvider(udp);
        sipProvider.addSipListener(this);
    }

    /**
     * This method is called by the SIP stack when a response arrives.
     */
    public void processResponse(ResponseEvent evt) {
        Response response = evt.getResponse();
        int status = response.getStatusCode();
        System.out.println("status is " + status);
    }

    /**
     * This method is called by the SIP stack when a new request arrives.
     */
    public void processRequest(RequestEvent evt) {
        Request req = evt.getRequest();

        try {
            createCallIdHeader(req);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String method = req.getMethod();
        if (method.equals(Request.REGISTER)) { //bad request type.
            try {
                if (req.getHeader("Authorization") == null || req.getHeader("Authorization").equals("")) {
                    onFirstRegister(req);
                } else {
                    onSecondRegister(req);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            }
        } else if (method.equals(Request.MESSAGE)) {
            try {
                onHeartBeat(req);

                Thread.sleep(1000);
                onQueryCamera();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method is called by the SIP stack when there's no answer
     * to a message. Note that this is treated differently from an error
     * message.
     */
    public void processTimeout(TimeoutEvent evt) {
        messageProcessor
                .processError("Previous message not sent: " + "timeout");
    }

    /**
     * This method is called by the SIP stack when there's an asynchronous
     * message transmission error.
     */
    public void processIOException(IOExceptionEvent evt) {
        messageProcessor.processError("Previous message not sent: "
                + "I/O Exception");
    }

    /**
     * This method is called by the SIP stack when a dialog (session) ends.
     */
    public void processDialogTerminated(DialogTerminatedEvent evt) {
    }

    /**
     * This method is called by the SIP stack when a transaction ends.
     */
    public void processTransactionTerminated(TransactionTerminatedEvent evt) {
    }


    @Override
    public void onFirstRegister(Request request) throws ParseException, InvalidArgumentException, SipException {

        Response response = messageFactory.createResponse(401, request);

        WWWAuthenticateHeader authenticateHeader = headerFactory.createWWWAuthenticateHeader("Digest");
        authenticateHeader.setRealm(realm);
        String uuid = UUID.randomUUID().toString();
        authenticateHeader.setNonce(uuid);
        response.addHeader(authenticateHeader);
        sipProvider.sendResponse(response);
    }

    @Override
    public void onSecondRegister(Request request) throws ParseException, InvalidArgumentException, SipException {
        Response response = messageFactory.createResponse(200, request);
        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
        response.addHeader(expiresHeader);
        sipProvider.sendResponse(response);
    }

    @Override
    public void onHeartBeat(Request request) throws ParseException, InvalidArgumentException, SipException, IOException, SAXException, ParserConfigurationException {

        Response response = messageFactory.createResponse(200, request);
//        byte[] content = (byte[]) request.getContent();
//        String sc = new String(content);
//        String cmdType = xmlUtils.getCmdType(sc);
//        if(cmdType!=null&&cmdType.equals("Keepalive")){
        sipProvider.sendResponse(response);
//        }

    }

    @Override
    public void onQueryCamera() throws ParseException, InvalidArgumentException, SipException {
        Address fromNameAddress = addressFactory.createAddress("sip:" + localUsername + "@34020000");
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                "14020000");

        SipURI toAddress = addressFactory.createSipURI(remoteUsername, realm);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(remoteUsername, remoteIp);


        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(remoteUsername,
                localPort, "udp", "branch1");
        viaHeaders.add(viaHeader);
        viaHeader.setHost(localIp);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        callIdHeader.setCallId(callId);

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);


        Request request = messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        SipURI remoteUri = addressFactory.createSipURI(remoteUsername, remoteIp);
        Address remoteAddress = addressFactory.createAddress(remoteUri);
        RouteHeader routeHeader = headerFactory.createRouteHeader(remoteAddress);
        request.addHeader(routeHeader);
        Address contactAddress = addressFactory.createAddress("<sip:" + remoteUsername + "@192.168.3.93:5060>");
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        String sdpData = "<?xml version=\"1.0\"?>\r\n" +
                "<Query>\r\n" +
                "<CmdType>DeviceInfo</CmdType>\r\n" +
                "<SN>17430</SN>\r\n" +
                "<DeviceID>34020000001110000001</DeviceID>\r\n" +
                "</Query>";
        byte[] contents = sdpData.getBytes();

        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(contents, contentTypeHeader);

        sipProvider.sendRequest(request);
    }

    private CallIdHeader createCallIdHeader(Request request) throws ParseException {
        callId = ((CallIdHeader) request.getHeader(CallIdHeader.NAME)).getCallId();
        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        if (callId.trim().length() > 0)
            callIdHeader.setCallId(callId);
        return callIdHeader;
    }
}
