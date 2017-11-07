package cn.goldencis.gbserver;

import cn.goldencis.gbclient.serturity.DigestClientAuthenticationMethod;
import gov.nist.javax.sip.stack.MessageProcessor;
import org.xml.sax.SAXException;

import javax.sip.*;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;
import java.util.TooManyListenersException;

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

    @Override
    public void onFirstRegister(Request request) throws ParseException, InvalidArgumentException, SipException {

    }

    @Override
    public void onSecondRegister(Request request) throws ParseException, InvalidArgumentException, SipException {

    }

    @Override
    public void onHeartBeat(Request request) throws ParseException, InvalidArgumentException, SipException, IOException, SAXException, ParserConfigurationException {

    }

    @Override
    public void onQueryCamera() throws ParseException, InvalidArgumentException, SipException {

    }

    @Override
    public void processRequest(RequestEvent requestEvent) {

    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
