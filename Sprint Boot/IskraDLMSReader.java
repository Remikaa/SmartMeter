package com.Meter.IskraemcoInterns;
import gurux.common.GXCommon;
import gurux.common.ReceiveParameters;
import gurux.dlms.*;
import gurux.dlms.enums.*;
import gurux.dlms.objects.*;
import gurux.io.BaudRate;
import gurux.io.Parity;
import gurux.io.StopBits;
import gurux.serial.GXSerial;
import gurux.dlms.GXReplyData;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;
import static gurux.io.BaudRate.*;
import static gurux.io.Parity.*;
import static gurux.io.StopBits.*;


/**
 * This is the class that holds all the attributes and functions to deal with the meter
 *
 */
public  class IskraDLMSReader {

    /**
     *
     */

    private static  GXSerial serial ;
    private static  GXDLMSClient client;
    private static  String comPortName ;
    private static  BaudRate baud ;
    private static  int dataBits ;
    private static  StopBits stopBits ;
    private static  Parity parity ;
    private static Scanner scanner = new Scanner(System.in);

    //Client Info
    private static int clientAddress;
    private static int serverAddress ;
    private static Authentication authentication;
    private static String password;
    private static InterfaceType interfaceType;

    private static  GXReplyData reply;
    private static  boolean connected;
    private static GXDLMSClock clock;
    private static  GXDLMSRegister energyRegister;
    private static GXDLMSDisconnectControl dc ;

    /**
     *  This function instantiates the meter's configuration into an object which has methods to communicate with the meter
     * @param comPort the communication port number on your device
     * @param baudrate the serial's baud rate
     * @param databit the serial's data bit
     * @param stopbit the serial's stop bit
     * @param parityType the serial's parity type
     * @param clientaddress the adrress of the client (meter configured)
     * @param serveraddress the adrress of the server (meter configured)
     * @param authenticationType the meter's security type
     * @param pass the password to bypass the security
     * @param interfacetype the meter's interface type (meter configured)
     */

    IskraDLMSReader(String comPort,int baudrate,int databit ,String stopbit,String parityType,
                    int clientaddress,int serveraddress,String authenticationType,String pass,String interfacetype)
    {
        switch (baudrate) {
            case 300:
                this.baud = BAUD_RATE_300;
                break;
            case 600:
                this.baud = BAUD_RATE_600;
                break;
            case 1800:
                this.baud = BAUD_RATE_1800;
                break;
            case 2400:
                this.baud = BAUD_RATE_2400;
                break;
            case 4800:
                this.baud = BAUD_RATE_4800;
                break;
            default:
                this.baud = BAUD_RATE_9600;
        }
        switch (stopbit) {
            case "1.5":
                this.stopBits = ONE_POINT_FIVE;
                break;
            case "2":
                this.stopBits = TWO;
                break;
            default:
                this.stopBits = ONE;
        }
        switch (parityType.toUpperCase()) {
            case "ODD":
                this.parity = ODD;
                break;
            case "EVEN":
                this.parity = EVEN;
                break;
            case "MARK":
                this.parity = MARK;
                break;
            case "SPACE":
                this.parity = SPACE;
                break;
            default:
                this.parity = NONE;
        }
        switch (authenticationType.toUpperCase()) {
            case "NONE":
                this.authentication = Authentication.NONE;
                break;
            case "HIGH":
                this.authentication = Authentication.HIGH;
                break;
            case "HIGH_MD5":
                this.authentication = Authentication.HIGH_MD5;
                break;
            case "HIGH_SHA1":
                this.authentication = Authentication.HIGH_SHA1;
                break;
            case "HIGH_GMAC":
                this.authentication = Authentication.HIGH_GMAC;
                break;
            case "HIGH_SHA256":
                this.authentication = Authentication.HIGH_SHA256;
                break;
            case "HIGH_ECDSA":
                this.authentication = Authentication.HIGH_ECDSA;
                break;
            default:
                this.authentication = Authentication.LOW;
        }
        switch (interfacetype.toUpperCase()) {
            case "HDLC":
                this.interfaceType = interfaceType.HDLC;
                break;
            case "WRAPPER":
                this.interfaceType = interfaceType.WRAPPER;
                break;
            case "WIRELESS_MBUS":
                this.interfaceType = interfaceType.WIRELESS_MBUS;
                break;
            case "PLC":
                this.interfaceType = interfaceType.PLC;
                break;
            case "PLC_HDLC":
                this.interfaceType = interfaceType.PLC_HDLC;
                break;
            case "LPWAN":
                this.interfaceType = interfaceType.LPWAN;
                break;
            case "WI_SUN":
                this.interfaceType = interfaceType.WI_SUN;
                break;
            case "PLC_PRIME":
                this.interfaceType = interfaceType.PLC_PRIME;
                break;
            case "WIRED_MBUS":
                this.interfaceType = interfaceType.WIRED_MBUS;
                break;
            case "SMS":
                this.interfaceType = interfaceType.SMS;
                break;
            case "PRIME_DC_WRAPPER":
                this.interfaceType = interfaceType.PRIME_DC_WRAPPER;
                break;
            case "COAP":
                this.interfaceType = interfaceType.COAP;
                break;
            default:
                this.interfaceType = interfaceType.HDLC_WITH_MODE_E;
        }
        this.client = new GXDLMSClient();
        this.comPortName=comPort;
        this.dataBits=databit;
        this.clientAddress=clientaddress;
        this.serverAddress=serveraddress;
        if(this.authentication !=  Authentication.NONE)
            this.password=pass;

        this.serial = new GXSerial(this.comPortName, this.baud, this.dataBits, this.parity, this.stopBits);

        this.client.setUseLogicalNameReferencing(true);
        this.client.setInterfaceType(this.interfaceType);
        this.client.setClientAddress(this.clientAddress);
        this.client.setServerAddress(this.serverAddress);
        this.client.setMaxReceivePDUSize(128);
        this.client.setAuthentication(this.authentication);
        this.client.setPassword(this.password);
        reply = new GXReplyData();
        connected=false;
        this.clock = new GXDLMSClock("0.0.1.0.0.255");
        this.energyRegister = new GXDLMSRegister("1.0.1.8.0.255");
        this.dc = new GXDLMSDisconnectControl("0.0.96.3.10.255");
    }

    /**
     *  this function closes the serial connection
     */
    public void close(){
        if (serial != null && serial.isOpen())
        {
            try {
                // Send disconnect request
                GXReplyData reply = new GXReplyData();
                byte[] disconnectData = client.disconnectRequest();
                if (disconnectData != null && disconnectData.length > 0) {
                    readDLMSPacket(disconnectData, reply);
                }
            } catch (Exception e) {
                System.err.println("Error during disconnect: " + e.getMessage());
            }
            serial.close();
            connected = false;
            try{
                Thread.sleep(5000);
            }
            catch (Exception e)
            {
                System.out.println("No negative numbers in sleep");
            }

            System.out.println("🔌 Closed serial port");
        }
    }

    /**
     * this function is for reading the energy of a meter
     * @return a string that carries the value of the energy reading
     * @throws Exception when meter is not connected
     */
    public String registerReader() throws Exception {
        if(connected==false)
            throw new Exception("Meter is not connected properly.");
        System.out.println("🔄 Reading register...");
        reply.clear();


        byte[] regData = client.read(energyRegister, 2)[0];
        readDataBlock(regData, reply);

        client.updateValue(energyRegister, 2, reply.getValue());

        Object regValue = energyRegister.getValue();
        System.out.println("✅ Energy register value: " + regValue);


        Object scaledValue = energyRegister.getScaler();
        System.out.println("✅ Scaled energy value: " + scaledValue);
        return "✅ Energy register value: " + regValue +"\n✅ Scaled energy value: " + scaledValue;
    }
    // Example modifications (optional):


    /**
     * This function is to read the clock of a meter
     * @return a string that carries the date and time of the clock
     * @throws Exception when meter is not connected
     */
    public String clockReader() throws Exception {
        System.out.println("🔄 Reading clock...");
        Thread.sleep(1000);

        byte[] clockData = client.read(clock, 2)[0];
        reply.clear();
        readDataBlock(clockData, reply);

        client.updateValue(clock, 2, reply.getValue());

        Object timeValue = clock.getTime();
        System.out.println("✅ Clock time: " + timeValue);
        return timeValue.toString();
    }

    /**
     * this function calls another function called media connection which is responsible for opening the serial connection,
     * making a handshake with the meter and doing the required protocols to open a connection with the meter.
     * @throws Exception when media connection has invalid configurations or cant connect with the meter due to unknown circumstances
     */
    public void meterConnection() throws Exception {
        if(connected==true) return;
        mediaConnection();
        handshake();
        connected=true;
    }

    /**
     * this function is called upon by meter connection
     * @throws Exception when media connection has invalid configurations or cant connect with the meter due to unknown circumstances
     */
    public void mediaConnection() throws Exception {

            serial.open();
            ReceiveParameters<byte[]> p = new ReceiveParameters<byte[]>(byte[].class);
            p.setAllData(false);
            p.setEop((byte) '\n');
            p.setWaitTime(3000);

            String replyStr;
            synchronized (serial.getSynchronous()) {

                byte[] data = GXCommon.hexToBytes("2F 3F 21 0D 0A");
                serial.send(data, null);
                if (!serial.receive(p)) {
                    throw new Exception("Invalid meter type.");
                }
                replyStr = new String(p.getReply());
                if (data.equals(replyStr)) {
                    if (!serial.receive(p)) {
                        throw new Exception("Invalid meter type.");
                    }
                    replyStr = new String(p.getReply());
                }
            }
            if (replyStr.length() == 0 || replyStr.charAt(0) != '/' || replyStr.isEmpty()) {
                throw new Exception("Invalid responce.");
            }

            System.out.println(replyStr);

            int bitrate = 0;
            char baudrate = replyStr.charAt(4);
            switch (baudrate) {
                case '0':
                    bitrate = 300;
                    break;
                case '1':
                    bitrate = 600;
                    break;
                case '2':
                    bitrate = 1200;
                    break;
                case '3':
                    bitrate = 2400;
                    break;
                case '4':
                    bitrate = 4800;
                    break;
                case '5':
                    bitrate = 9600;
                    break;
                case '6':
                    bitrate = 19200;
                    break;
                default:
                    throw new Exception("Unknown baud rate.");
            }

            //Send ACK
            byte controlCharacter = (byte) '2';

            byte ModeControlCharacter = (byte) '2';
            byte[] tmp = new byte[]{0x06, controlCharacter, (byte) baudrate, ModeControlCharacter, 13, 10};

            synchronized (serial.getSynchronous()) {
                serial.send(tmp, null);
            }
            System.out.println("🔁 Serial port tmp sent: " + GXCommon.bytesToHex(tmp));

            // Close and reopen serial port with new settings
            serial.close();
            System.out.println("🔌 Closed serial port for reconfiguration");

            Thread.sleep(1000);

    }

    /**
     * send the SNRM protocol for the handshake, called by the handshake function
     * @throws Exception if we have no reply from the meter
     */

    public void SNRM() throws Exception {

            System.out.println("🔄 Sending SNRM frame for HDLC_WITH_MODE_E...");
            GXReplyData reply = new GXReplyData();
            byte[] data = client.snrmRequest();

            readDLMSPacket(data, reply);
            client.parseUAResponse(reply.getData());
            System.out.println("✅ SNRM successful, UA response received");
    }

    /**
     * send the AARQ protocol for the handshake, called by the handshake function
     * @throws Exception if we get no reply from the meter
     */

    public  void AARQ() throws Exception {
        for (byte[] it : client.aarqRequest())
        {
            reply.clear();
            readDLMSPacket(it, reply);
        }
        client.parseAareResponse(reply.getData());

        System.out.println("✅ AARQ successful, AARE response received");
    }

    /**
     * This function tells us the relay state of the meter (opened or closed)
     * @return a string that carries the relay state
     * @throws Exception when the meter is inaccessible
     */

    public String relayStateReader() throws Exception {
        System.out.println("🔍 Reading current relay state...");
        Object currentState = readObject(dc, 2); // Attribute 2 = ControlState
        Object outputState = readObject(dc, 3);  // Attribute 3 = OutputState
        System.out.println("✅ ControlState: " + currentState);
        System.out.println("✅ OutputState: " + outputState);
        return "✅ ControlState: " + currentState +"\n✅ OutputState: " + outputState;
    }

    /**
     *  closes off the relay of the meter
     * @return a string that says the operation was successful
     * @throws Exception if the meter is inaccessible
     */

    public String relayDisconnector() throws Exception {
        System.out.println("🛑 Sending disconnect command...");
        byte[][] disconnectCmd = dc.remoteDisconnect(client);
        for (byte[] packet : disconnectCmd) {
            readDataBlock(packet, new GXReplyData());
        }
        Thread.sleep(5000);
        return relayStateReader();

    }

    /**
     * open the relay of the meter
     * @return  a string that says the operation was successful
     * @throws Exception if the meter is inaccessible
     */

    public String relayConnector() throws Exception {
        System.out.println("🔌 Sending reconnect command...");
        byte[][] reconnectCmd = dc.remoteReconnect(client);
        for (byte[] packet : reconnectCmd) {
            readDataBlock(packet, new GXReplyData());
        }
        Thread.sleep(5000);
        return relayStateReader();

    }

    /**
     * sets the clock of the meter to the time given to it
     * @param Time an object of type Calendar that carries the time
     * @throws Exception if the meter is inaccessible
     */

    public  void clockWritter(Calendar Time) throws Exception {
        System.out.println("🔄 Setting meter clock to the time...");
        reply.clear();

        // Create a new clock object for writing (reuse the existing one)
        clock.setTime(new GXDateTime(Time.getTime()));

        byte[][] clockWriteData = client.write(clock, 2);
        for (byte[] writePacket : clockWriteData) {
            reply.clear();
            readDataBlock(writePacket, reply);
        }


        // Optional: Read back the clock to verify it was set correctly
        System.out.println("🔄 Verifying clock setting...");
        clockReader();

    }

    /**
     * This calls the AARQ and SNRM functions to perform a complete handshake with the meter to open the connection
     * @throws Exception if the meter is inaccessible or doesn't respond
     */

    public void handshake() throws Exception {
        Thread.sleep(1000);

        serial = new GXSerial(comPortName, BAUD_RATE_9600, 8, NONE, ONE);
        serial.open();
        System.out.println("♻️ Reopened serial port at 9600 baud, 8N1");

        // Give the meter time to stabilize
        Thread.sleep(1000);
        SNRM();
        AARQ();
    }

    /**
     * this function reads the DLMS responses from the meter
     * @param data Data is not always fit to one packet. In this case data can be split to blocks and one block to frames. Gurux DLMS component handles this automatically.
     * When a full frame is received check if there is more data available using IsMoreData method. If there is more data, generate a new read message using ReceiverReady method and check are there all data received again.
     * After receiving the full frame check that there wasn't any errors from err.
     * @param reply a string that carries the response of the data or what the meter replied with
     * @throws Exception if the meter is inaccessible or disconnected
     */

    private static  void readDLMSPacket(byte[] data, GXReplyData reply)
            throws Exception {
        if (data == null || data.length == 0) {
            return;
        }

        Object eop = (byte) 0x7E;
        // In network connection terminator is not used.
        if (client.getInterfaceType() == InterfaceType.WRAPPER) {
            eop = null;
        }

        Integer pos = 0;
        boolean succeeded = false;
        ReceiveParameters<byte[]> p = new ReceiveParameters<byte[]>(byte[].class);
        p.setAllData(false);  // Changed to false for better packet handling
        p.setEop(eop);
        p.setCount(5);
        p.setWaitTime(5000);  // Increased timeout

        synchronized (serial.getSynchronous()) {
            while (!succeeded && pos < 3) {
                writeTrace("<- " + now() + "\t" + GXCommon.bytesToHex(data));
                serial.send(data, null);

                if (p.getEop() == null) {
                    p.setCount(1);
                }

                succeeded = serial.receive(p);
                if (!succeeded) {
                    pos++;
                    if (pos < 3) {
                        System.out.println("Data send failed. Try to resend " + pos + "/3");
                        Thread.sleep(500); // Small delay before retry
                        continue;
                    }
                    throw new RuntimeException("Failed to receive reply from the device in given time.");
                }
            }

            // Loop until whole DLMS packet is received.
            while (!client.getData(p.getReply(), reply)) {
                if (p.getEop() == null) {
                    p.setCount(1);
                }
                if (!serial.receive(p)) {
                    throw new Exception("Failed to receive reply from the device in given time.");
                }
            }
        }

        writeTrace("-> " + now() + "\t" + GXCommon.bytesToHex(p.getReply()));
        if (reply.getError() != 0) {
            throw new GXDLMSException(reply.getError());
        }
    }

    /**
     * to print a certain message
     * @param message (String)the message that we want to print
     */

    private static void writeTrace(String message) {
        System.out.println(message);
    }

    /**
     *   This is a convenience wrapper that:
     *   <ol>
     *     <li>Builds the READ-request APDU via {@code client.read(...)}.</li>
     *     <li>Transmits the request and receives the complete reply through
     *         {@link #readDataBlock(byte[], GXReplyData)}.</li>
     *     <li>Parses the returned data and updates the supplied
     *         {@link GXDLMSObject} instance with the received value.</li>
     *   </ol>
     * @param item the DLMS/COSEM object whose attribute is to be read
     * @param attributeIndex attributeIndex 1-based index of the attribute inside the object
     * (e.g., 2 for “value” in most profiles)
     * @return the Java object that represents the attribute value
     * (type depends on the attribute’s data type)
     * @throws Exception if any communication, parsing or DLMS-level error occurs
     */

    private static Object readObject(GXDLMSObject item, int attributeIndex) throws Exception
    {
        GXReplyData reply = new GXReplyData();
        byte[] data = client.read(item, attributeIndex)[0];
        readDataBlock(data, reply);
        return client.updateValue(item, attributeIndex, reply.getValue());
    }

    /**
     * Performs a complete multi-block data transfer with the meter
     * The method keeps issuing Receiver-Ready (RR) frames until the meter
     * indicates that no more data is available ({@link GXReplyData#isMoreData()}
     * returns {@code false}).  Each incoming block is appended to {@code reply}.
     * @param data data  the initial request frame (typically a GET-Request APDU)
     * @param reply reply container that accumulates the data blocks and the final
     * response; its {@code moreData} flag is updated after every
     * interaction
     * @throws Exception if a timeout, framing error or DLMS exception response is encountered during the exchange
     */

    private static void readDataBlock(byte[] data, GXReplyData reply) throws Exception {
        if (data.length != 0) {
            readDLMSPacket(data, reply);
            while (reply.isMoreData()) {
                data = client.receiverReady(reply.getMoreData());
                readDLMSPacket(data, reply);
            }
        }
    }

    /**
     * Returns the current time formatted as {@code HH:mm:ss.SSS}.
     * @return formatted timestamp string
     */

    private static String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

}
