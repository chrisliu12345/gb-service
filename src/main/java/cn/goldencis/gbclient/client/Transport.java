package cn.goldencis.gbclient.client;

import cn.goldencis.gbclient.core.InitParameter;
import cn.goldencis.gbclient.serturity.DigestClientAuthenticationMethod;
import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;

public class Transport implements SipListener, IClientRequest {


    private long callSeqNumber = 0;
    boolean receiveStatus = false;
    private SipStack sipStack;

    private String callId;

    private SipFactory sipFactory;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    private DigestClientAuthenticationMethod digest;

    private InitParameter bean;

    private ResponseEvent responseEvent;


    /**
     * Here we initialize the SIP stack.
     */
    public Transport(InitParameter bean)
            throws SipException,
            InvalidArgumentException,
            TooManyListenersException, ParseException {


        this.bean = bean;
        digest = new DigestClientAuthenticationMethod();
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "ClientMain");
        properties.setProperty("javax.sip.IP_ADDRESS", bean.getLocalIp());

        sipStack = sipFactory.createSipStack(properties);
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        ListeningPoint tcp = sipStack.createListeningPoint(bean.getLocalPort(), "tcp");
        ListeningPoint udp = sipStack.createListeningPoint(bean.getLocalPort(), "udp");

        sipProvider = sipStack.createSipProvider(tcp);
        sipProvider.addSipListener(this);
        sipProvider = sipStack.createSipProvider(udp);
        sipProvider.addSipListener(this);
    }

    @Override
    public Response sendRequest(Request request) throws SipException {
        callSeqNumber++;
        receiveStatus = false;
        sipProvider.sendRequest(request);
        for (int i = 0; i < 100; i++) {
            if (receiveStatus) {
                return responseEvent.getResponse();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * This method is called by the SIP stack when a response arrives.
     */
    public void processResponse(ResponseEvent evt) {
        receiveStatus = true;
        Response response = evt.getResponse();
        int status = response.getStatusCode();
        System.out.println("status is " + status);
        CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        long cs = cSeqHeader.getSeqNumber();
        responseEvent = evt;
    }

    /**
     * This method is called by the SIP stack when a new sendRequest arrives.
     */
    public void processRequest(RequestEvent evt) {

        Request req = evt.getRequest();
        String method = req.getMethod();
        if (!method.equals("MESSAGE")) { //bad sendRequest type.
            System.out.println("Bad sendRequest type: " + method);
            return;
        }

        FromHeader from = (FromHeader) req.getHeader("From");
        System.out.println(from.getAddress().toString() + new String(req.getRawContent()));
        Response response = null;
        try { //Reply with OK
            response = messageFactory.createResponse(200, req);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag("888"); //This is mandatory as per the spec.
            ServerTransaction st = sipProvider.getNewServerTransaction(req);
            st.sendResponse(response);
            String body = new String((byte[]) (req.getContent()));
            String Sn = body.split("SN")[1].split(">")[1].split("<")[0];

            if (body.contains("Catalog")) {
                createCatalogResponseResquest(Sn);
            } else if (body.contains("DeviceStatus")) {
                createDeviceStatusResponseResquest(Sn);
            } else if (body.contains("DeviceControl")) {
                createDeviceControlResponseResquest(Sn);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Can't send OK reply.");
        }
    }

    /**
     * This method is called by the SIP stack when there's no answer
     * to a message. Note that this is treated differently from an error
     * message.
     */
    public void processTimeout(TimeoutEvent evt) {
        System.out.println("Previous message not sent: " + "timeout");
        receiveStatus = true;
    }

    /**
     * This method is called by the SIP stack when there's an asynchronous
     * message transmission error.
     */
    public void processIOException(IOExceptionEvent evt) {
        System.out.println("Previous message not sent: "
                + "I/O Exception");
        receiveStatus = true;
    }

    /**
     * This method is called by the SIP stack when a dialog (session) ends.
     */
    public void processDialogTerminated(DialogTerminatedEvent evt) {
        System.out.println("processDialogTerminated");
    }

    /**
     * This method is called by the SIP stack when a transaction ends.
     */
    public void processTransactionTerminated(TransactionTerminatedEvent evt) {
        System.out.println("processTransactionTerminated");

    }

    private void processResponseAuthorization(Response response, URI uriReq, Request requestauth) throws Exception {

        WWWAuthenticateHeader wwwAuthenticateHeader = (WWWAuthenticateHeader) response.getHeader(SIPHeaderNames.WWW_AUTHENTICATE);
        if (wwwAuthenticateHeader != null) {
            String schema = wwwAuthenticateHeader.getScheme();
            String nonce = wwwAuthenticateHeader.getNonce();

            AuthorizationHeader authorizationHeader = headerFactory.createAuthorizationHeader(schema);
            authorizationHeader.setRealm(bean.getRealm());
            authorizationHeader.setNonce(nonce);
            authorizationHeader.setAlgorithm("MD5");
            authorizationHeader.setUsername(bean.getLocalUsername());

            authorizationHeader.setURI(uriReq);

            digest.initialize(bean.getRealm(), bean.getLocalUsername(), uriReq.toString(), nonce, bean.getPassword(), ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod(), null, "MD5");
            String r = digest.generateResponse();
            authorizationHeader.setResponse(r);

            System.out.println("Proxy Response antes de modificarlo : " + authorizationHeader.getResponse());
            requestauth.addHeader(authorizationHeader);
        } else {
            System.out.println("no auth info created here because  wwwAuthenticateHeader is null .");
        }
    }

    private CallIdHeader createCallIdHeader() throws ParseException {
        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        if (callId.trim().length() > 0)
            callIdHeader.setCallId(callId);
        return callIdHeader;
    }


    public Request createQueryRequest(String deviceUserName, String deviceIp, String queryContent) throws ParseException, InvalidArgumentException, SipException {
        Request request = createQueryRequestHeader(deviceUserName, deviceIp);
        byte[] contents = queryContent.getBytes();
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(contents, contentTypeHeader);
        return request;
    }

    private Request createQueryRequestHeader(String deviceUserName, String deviceIp) throws ParseException, InvalidArgumentException {

        Address fromNameAddress = addressFactory.createAddress("sip:" + bean.getLocalUsername() + "@" + bean.getRealm());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                bean.getRealm());

        SipURI toAddress = addressFactory.createSipURI(deviceUserName, bean.getRealm());
        Address toNameAddress = addressFactory.createAddress(toAddress);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(deviceUserName, bean.getRealm());

        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(bean.getLocalIp(),
                bean.getLocalPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = createCallIdHeader();

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
                Request.MESSAGE);
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);


        Request request = messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        Address routeAddress = addressFactory.createAddress("<sip:" + deviceUserName + "@" + deviceIp + ":5060>");
        RouteHeader routeHeader = headerFactory.createRouteHeader(routeAddress);
        request.addHeader(routeHeader);
        return request;
    }

    public Request createFirstRegisterHeader() throws ParseException, InvalidArgumentException {
        Address fromNameAddress = addressFactory.createAddress("sip:" + bean.getLocalUsername() + "@" + bean.getRealm());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                bean.getRealm());

        SipURI toAddress = addressFactory.createSipURI(bean.getLocalUsername(), bean.getRealm());
        Address toNameAddress = addressFactory.createAddress(toAddress);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(bean.getServerUsername(), bean.getServerIp());


        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(bean.getLocalIp(),
                bean.getLocalPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = sipProvider.getNewCallId();
        callId = callIdHeader.getCallId();

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(callSeqNumber,
                Request.REGISTER);


        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);


        Request request = messageFactory.createRequest(requestURI,
                Request.REGISTER, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
        request.setExpires(expiresHeader);

        ContactHeader contactHeader = headerFactory.createContactHeader();
        SipURI contactUri = addressFactory.createSipURI(bean.getLocalUsername(), bean.getLocalIp() + ":" + String.valueOf(bean.getLocalPort()));
        Address contactAddress = addressFactory.createAddress(contactUri);
        contactHeader.setAddress(contactAddress);
        request.addHeader(contactHeader);
        return request;
    }

    public Request createSecondRegisterHeader(Response response) throws Exception {
        SipURI fromUri = addressFactory.createSipURI(bean.getLocalUsername(), bean.getRealm());
        Address fromNameAddress = addressFactory.createAddress(fromUri);
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                bean.getRealm());


        SipURI toUri = addressFactory.createSipURI(bean.getLocalUsername(), bean.getRealm());
        Address toAddress = addressFactory.createAddress(toUri);
        ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

        SipURI requestURI = addressFactory.createSipURI(bean.getServerUsername(), bean.getServerIp());


        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(bean.getLocalIp(),
                bean.getLocalPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = createCallIdHeader();
        CSeqHeader cSeqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        cSeqHeader.setSeqNumber(callSeqNumber);
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);
        callSeqNumber++;

        Request request = messageFactory.createRequest(requestURI,
                Request.REGISTER, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
        request.setExpires(expiresHeader);

        Address contactAddress = addressFactory.createAddress("<sip:" + bean.getLocalUsername() + "@" + bean.getLocalIp() + ":" + bean.getLocalPort() + ">");
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
        request.addHeader(contactHeader);


        processResponseAuthorization(response, fromUri, request);
        return request;
    }

    public Request createRequestWithBody(String queryContent) throws ParseException, InvalidArgumentException, SipException {
        Request request = createNotifyRequestHeader();
        byte[] contents = queryContent.getBytes();
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("Application", "MANSCDP+xml");
        request.setContent(contents, contentTypeHeader);
        return request;
    }

    private Request createNotifyRequestHeader() throws ParseException, InvalidArgumentException {

        Address fromNameAddress = addressFactory.createAddress("sip:" + bean.getLocalUsername() + "@" + bean.getRealm());
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                bean.getRealm());

        SipURI toAddress = addressFactory.createSipURI(bean.getServerUsername(), bean.getRealm());
        Address toNameAddress = addressFactory.createAddress(toAddress);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        SipURI requestURI = addressFactory.createSipURI(bean.getServerUsername(), bean.getServerIp());
        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader(bean.getLocalIp(),
                bean.getLocalPort(), "udp", "branch1");
        viaHeaders.add(viaHeader);

        CallIdHeader callIdHeader = createCallIdHeader();

        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(callSeqNumber,
                Request.MESSAGE);
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);


        Request request = messageFactory.createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);

        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
        request.setExpires(expiresHeader);

        return request;
    }

    public Response createCatalogResponseResquest(String sn) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<Response>\r\n" +
                "<CmdType>Catalog</CmdType>\r\n" +
                "<SN>" + sn + "</SN>\r\n" +
                "<DeviceID>" + bean.getLocalUsername() + "</DeviceID>\r\n" +
                "<SumNum>1</SumNum>\r\n" +
                "<DeviceList Num=\"1\">\r\n" +
                "<Item>\r\n" +
                "<DeviceID>34020000001320000004</DeviceID>\r\n" +
                "<Name>Camera 01</Name>\r\n" +
                "<Manufacturer>Hikvision</Manufacturer>\r\n" +
                "<Model>IP Camera</Model>\r\n" +
                "<Owner>Owner</Owner>\r\n" +
                "<CivilCode>CivilCode</CivilCode>\r\n" +
                "<Address>Address</Address>\r\n" +
                "<Parental>0</Parental>\r\n" +
                "<ParentID>" + bean.getLocalUsername() + "</ParentID>\r\n" +
                "<SafetyWay>0</SafetyWay>\r\n" +
                "<RegisterWay>1</RegisterWay>\r\n" +
                "<Secrecy>0</Secrecy>\r\n" +
                "<Status>ON</Status>\r\n" +
                "</Item>\r\n" +
                "</DeviceList>\r\n" +
                "</Response>";
        Request request = createRequestWithBody(sdpData);
        return sendRequest(request);
    }

    public Response createDeviceStatusResponseResquest(String sn) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<Response>\r\n" +
                "<CmdType>Catalog</CmdType>\r\n" +
                "<SN>" + sn + "</SN>\r\n" +
                "<DeviceID>" + bean.getLocalUsername() + "</DeviceID>\r\n" +
                "<SumNum>1</SumNum>\r\n" +
                "<DeviceList Num=\"1\">\r\n" +
                "<Item>\r\n" +
                "<DeviceID>34020000001320000004</DeviceID>\r\n" +
                "<Name>Camera 01</Name>\r\n" +
                "<Manufacturer>Hikvision</Manufacturer>\r\n" +
                "<Model>IP Camera</Model>\r\n" +
                "<Owner>Owner</Owner>\r\n" +
                "<CivilCode>CivilCode</CivilCode>\r\n" +
                "<Address>Address</Address>\r\n" +
                "<Parental>0</Parental>\r\n" +
                "<ParentID>" + bean.getLocalUsername() + "</ParentID>\r\n" +
                "<SafetyWay>0</SafetyWay>\r\n" +
                "<RegisterWay>1</RegisterWay>\r\n" +
                "<Secrecy>0</Secrecy>\r\n" +
                "<Status>ON</Status>\r\n" +
                "</Item>\r\n" +
                "</DeviceList>\r\n" +
                "</Response>";
        Request request = createRequestWithBody(sdpData);
        return sendRequest(request);
    }

    public Response createDeviceControlResponseResquest(String sn) throws ParseException, SipException, InvalidArgumentException {
        String sdpData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<Response>\r\n" +
                "<CmdType>DeviceControl</CmdType>\r\n" +
                "<SN>" + sn + "</SN>\r\n" +
                "<DeviceID>34020000001320000004</DeviceID>\r\n" +
                "<Result>OK</Result>\r\n" +
                "</Response>";
        Request request = createRequestWithBody(sdpData);
        return sendRequest(request);
    }
}
