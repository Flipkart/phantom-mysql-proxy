/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.phantom.runtime.impl.server.netty.handler.mysql;

import com.flipkart.phantom.event.ServiceProxyEventProducer;
import com.flipkart.phantom.mysql.impl.MysqlProxyExecutor;
import com.flipkart.phantom.mysql.impl.MysqlRequestWrapper;
import com.flipkart.phantom.task.spi.Executor;
import com.flipkart.phantom.task.spi.repository.ExecutorRepository;
import com.github.mpjct.jmpjct.mysql.proto.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>MysqlChannelHandler</code> is a sub-type of {@link SimpleChannelHandler} that acts as a proxy for Mysql calls using the mysql protocol.
 * It wraps the Mysql call using a {@link MysqlProxyExecutor} that provides useful features like monitoring, fallback etc.
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlChannelHandler extends SimpleChannelHandler implements InitializingBean {

    /**
     * Mysql server Host to connect to
     */
    public String host;
    /**
     * Mysql server port to connect to
     */
    public int port;
    /**
     * Mysql socket for the connection
     */
    public Socket mysqlSocket = null;
    /**
     * mysql socket input stream
     */
    public InputStream mysqlIn = null;
    /**
     * mysql socket output stream
     */
    public OutputStream mysqlOut = null;
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlChannelHandler.class);
    /**
     * The default channel group
     */
    private ChannelGroup defaultChannelGroup;
    /**
     * The MysqlProxyRepository to lookup MysqlProxy from
     */
    private ExecutorRepository repository;
    /**
     * The Mysql proxy handler map
     */
    private Map<String, String> proxyMap = new HashMap<String, String>();
    /**
     * The default Mysql proxy handler
     */
    private String defaultProxy;
    /**
     * The publisher used to broadcast events to Service Proxy Subscribers
     */
    private ServiceProxyEventProducer eventProducer;

    /**
     * Event Type for publishing all events which are generated here
     */
    private final static String Mysql_HANDLER = "Mysql_HANDLER";
    /**
     * The flag to determine the state in connection
     */
    private int flag = Flags.MODE_INIT;
    /**
     * The response byte array holder
     */
    private ArrayList<byte[]> buffer;
    /**
     * The response InputStream from mysql connection socket
     */
    private InputStream in = null;
    /**
     * Mysql query sent from client.
     */
    private String query;
    /**
     * Hystrix command key that is being passed to proxy.
     * This is the CRUD command operation being sent to proxy.
     */
    private String commandKey;
    /**
     * A boolean variable to toggle to check if additional response buffer is present
     *
     */
    private boolean bufferResultSet = true;
    /**
     * Mysql packet sequence id
     */
    private long sequenceId;
    /**
     * Mysql database schema information
     */
    private String schema;
    /**
     * Mysql proxy executor.
     */
    private Executor executor;
    /**
     * Mysql packet size
     */
    private static final int MYSQL_PACKET_SIZE = 16384;
    /**
    * @param  CONNECTION_TIME
    * An <tt>int</tt> expressing the relative importance of a short
    * connection time
    *
    * @param  LATENCY
    * An <tt>int</tt> expressing the relative importance of low
    * latency
    *
    * @param  BANDWIDTH
    * An <tt>int</tt> expressing the relative importance of high
    * bandwidth
    */
    private static final int CONNECTION_TIME = 0;
    private static final int LATENCY = 2;
    private static final int BANDWIDTH = 1;
   /**
     * IP traffic class.
     * RFC 1349 defines the TOS values as follows:
     * <p>
     * <UL>
     * <LI><CODE>IPTOS_LOWCOST (0x02)</CODE></LI>
     * <LI><CODE>IPTOS_RELIABILITY (0x04)</CODE></LI>
     * <LI><CODE>IPTOS_THROUGHPUT (0x08)</CODE></LI>
     * <LI><CODE>IPTOS_LOWDELAY (0x10)</CODE></LI>
     * </UL>
     *
     */
     private final static int TC = 0x10;
    /**
     * size of a command packet that the
     * client intends to send to the server.
     */
    private final static int PACKED_PACKET_SIZE = 65535;
    /**
     * Current reader offset in the buffer.
     * */
    private static int offset = 0;
    /**
     * Start Getter/Setter methods
     */
    public ChannelGroup getDefaultChannelGroup() {
        return this.defaultChannelGroup;
    }
    public void setDefaultChannelGroup(ChannelGroup defaultChannelGroup) {
        this.defaultChannelGroup = defaultChannelGroup;
    }
    public ExecutorRepository getRepository() {
        return this.repository;
    }
    public void setRepository(ExecutorRepository repository) {
        this.repository = repository;
    }
    public Map<String, String> getProxyMap() {
        return this.proxyMap;
    }
    public void setProxyMap(Map<String, String> proxyMap) {
        this.proxyMap = proxyMap;
    }
    public String getDefaultProxy() {
        return this.defaultProxy;
    }
    public void setDefaultProxy(String defaultProxy) {
        this.defaultProxy = defaultProxy;
    }
    public void setEventProducer(ServiceProxyEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    /** End Getter/Setter methods */
    /**
     * Interface method implementation. Checks if all mandatory properties have been set
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
    }
    /**
     * Overriden superclass method. Adds the newly created Channel to the default channel group and calls the super class {@link #channelOpen(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)} method
     *
     * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelOpen(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
     */
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent event) throws Exception {
        super.channelOpen(ctx, event);
    }
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent event) throws Exception {

        if (this.flag == Flags.MODE_INIT) {
            this.in = initConnection(ctx, this.flag);
            writeServerHandshake(ctx, event, this.in);
        }

        super.handleUpstream(ctx, event);
    }
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {

        if (this.flag == Flags.MODE_READ_AUTH) {
            handleAuthentication(ctx, messageEvent);
        } else {
            handleQueries(ctx, messageEvent);
        }
    }
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent event) throws Exception {
        LOGGER.warn("Exception {} thrown on Channel {}. Disconnect initiated", event, event.getChannel());
        event.getChannel().close();
        closeConnection();
        //super.exceptionCaught(ctx, event);
    }
    private InputStream initConnection(ChannelHandlerContext ctx, int flag) throws Exception {

        this.mysqlSocket = new Socket(this.host, this.port);
        /**
        * The application prefers high bandwidth above low
        * latency, and low latency above short connection time
        */
        this.mysqlSocket.setPerformancePreferences(CONNECTION_TIME, LATENCY, BANDWIDTH);
        this.mysqlSocket.setTcpNoDelay(true);
        this.mysqlSocket.setTrafficClass(TC);
        this.mysqlSocket.setKeepAlive(true);

        //LOGGER.info("Connected to mysql server at "+this.host+":"+this.port);
        this.mysqlIn = new BufferedInputStream(this.mysqlSocket.getInputStream(), MYSQL_PACKET_SIZE);
        this.mysqlOut = this.mysqlSocket.getOutputStream();
        return this.mysqlIn;
    }
    private void writeServerHandshake(ChannelHandlerContext ctx, ChannelEvent event, InputStream in) throws Exception {

        byte[] packet = Packet.read_packet(in);
        Handshake authChallenge = Handshake.loadFromPacket(packet);

        // Remove some flags from the reply
        authChallenge.removeCapabilityFlag(Flags.CLIENT_COMPRESS);
        authChallenge.removeCapabilityFlag(Flags.CLIENT_SSL);
        authChallenge.removeCapabilityFlag(Flags.CLIENT_LOCAL_FILES);

        // Set the default result set creation to the server's character set
        ResultSet.characterSet = authChallenge.characterSet;

        // Set Replace the packet in the buffer
        this.buffer = new ArrayList<byte[]>();
        this.buffer.add(authChallenge.toPacket());

        write(event.getChannel(), this.buffer);
        this.flag = Flags.MODE_READ_AUTH;

    }
    private void handleAuthentication(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {

        this.buffer = (ArrayList<byte[]>) messageEvent.getMessage();
        HandshakeResponse response = HandshakeResponse.loadFromPacket(this.buffer.get(0));

        if (!response.hasCapabilityFlag(Flags.CLIENT_PROTOCOL_41)) {
            LOGGER.error("We do not support Protocols under 4.1");
            return;
        }

        response.removeCapabilityFlag(Flags.CLIENT_COMPRESS);
        response.removeCapabilityFlag(Flags.CLIENT_SSL);
        response.removeCapabilityFlag(Flags.CLIENT_LOCAL_FILES);

        this.in = authenticate(ctx, Flags.MODE_SEND_AUTH, this.buffer);
        writeAuthResponse(ctx, messageEvent, this.in);

    }
    private InputStream authenticate(ChannelHandlerContext ctx, int flag, ArrayList<byte[]> buffer) throws Exception {

        if(flag == Flags.MODE_SEND_AUTH)
                Packet.write(this.mysqlOut, buffer);

        return this.mysqlIn;
    }
    private void writeAuthResponse(ChannelHandlerContext ctx, MessageEvent messageEvent, InputStream in) throws Exception {

        byte[] packet = Packet.read_packet(in);
        this.buffer = new ArrayList<byte[]>();
        this.buffer.add(packet);

        if (Packet.getType(packet) != Flags.OK) {
            LOGGER.debug("Auth is not okay!");
        }

        write(messageEvent.getChannel(), this.buffer);
        this.flag = Flags.MODE_READ_QUERY;
    }
    private void handleQueries(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {

        this.bufferResultSet = false;
        this.buffer = new ArrayList<byte[]>();
        this.buffer = (ArrayList<byte[]>) messageEvent.getMessage();

        byte[] packet = this.buffer.get(0);
        this.sequenceId = Packet.getSequenceId(packet);

        switch (Packet.getType(packet)) {
            case Flags.COM_QUIT:
                this.halt();
                break;
            // Extract out the new default schema
            case Flags.COM_INIT_DB:
                this.schema = Com_Initdb.loadFromPacket(packet).schema;
                break;
            // Query
            case Flags.COM_QUERY:
                this.query = Com_Query.loadFromPacket(packet).query;
                break;
            default:
                break;

        }
        // If the query is of type COM_QUIT then closing the socket connection above using halt method.
        if (Packet.getType(packet) != Flags.COM_QUIT) {
            this.in = executeQueries(ctx, Flags.MODE_SEND_QUERY, this.buffer);
            writeQueryResponse(ctx, messageEvent, this.in);
        }
    }
    private InputStream executeQueries(ChannelHandlerContext ctx, int flag, ArrayList<byte[]> buffer) throws Exception {

        MysqlRequestWrapper executorMysqlRequest = new MysqlRequestWrapper();
        executorMysqlRequest.setBuffer(buffer);
        executorMysqlRequest.setMysqlSocket(this.mysqlSocket);
        executorMysqlRequest.setMysqlIn(this.mysqlIn);
        executorMysqlRequest.setMysqlOut(this.mysqlOut);
        this.commandKey = getQueryCommand(this.query);
        executorMysqlRequest.setCommandKey(this.commandKey);
        Executor executor = this.repository.getExecutor(this.commandKey, this.defaultProxy, executorMysqlRequest);
        try {
            this.in = (InputStream) executor.execute();
        } catch (Exception e) {
            throw new RuntimeException("Error in reading server Queries Response :" + this.defaultProxy + ".", e);
        }
        return this.in;
    }
    private void writeQueryResponse(ChannelHandlerContext ctx, MessageEvent messageEvent, InputStream in) throws Exception {

        byte[] packet = Packet.read_packet(in);
        this.buffer = new ArrayList<byte[]>();
        this.buffer.add(packet);
        this.sequenceId = Packet.getSequenceId(packet);

        switch (Packet.getType(packet)) {
            case Flags.OK:
            case Flags.ERR:
                break;
            default:
                this.buffer = readFullResultSet(in, messageEvent, this.buffer, this.bufferResultSet);
                break;
        }

        write(messageEvent.getChannel(), this.buffer);
        this.flag = Flags.MODE_READ_QUERY;

    }
    private void halt() throws Exception {
        //close mysql socket and input/output stream
        closeConnection();
    }
    /**
     * A utility method to identify Hystrix command key or in other words the CRUD Mysql operations.
     * @param query Mysql query from client
     * @return
     */
    private String getQueryCommand(String query) {
        query = query.trim();
        int i = query.indexOf(' ');
        String command = query.substring(0, i);
        command = command.toUpperCase();
        return command;
    }
    /**
     * Closes local Mysql connection socket and I/O streams in case of exception in Netty Channels or
     * client sends a connection close request
     * @throws Exception
     */
    private void closeConnection() throws Exception {
        this.mysqlSocket.close();
        this.mysqlIn.close();
        this.mysqlOut.close();
    }
    /**
     * read complete result set from mysql server all the input bytes
     */
    private ArrayList<byte[]> readFullResultSet(InputStream in, MessageEvent messageEvent, ArrayList<byte[]> buffer, boolean bufferResultSet) throws IOException {
        // Assume we have the start of a result set already

        byte[] packet = buffer.get((buffer.size() - 1));
        long colCount = ColCount.loadFromPacket(packet).colCount;

        // Read the columns and the EOF field
        for (int i = 0; i < (colCount + 1); i++) {
            packet = Packet.read_packet(in);
            if (packet == null) {
                throw new IOException();
            }
            buffer.add(packet);

            if (!bufferResultSet) {
                write(messageEvent.getChannel(), buffer);
                buffer.clear();
            }
        }
        byte[] packedPacket = new byte[PACKED_PACKET_SIZE];
        int position = offset;

        while (true) {
            packet = Packet.read_packet(in);

            if (packet == null) {
                throw new IOException();
            }

            int packetType = Packet.getType(packet);

            if (packetType == Flags.EOF || packetType == Flags.ERR) {
                byte[] newPackedPacket = new byte[position];
                System.arraycopy(packedPacket, offset, newPackedPacket, offset, position);
                buffer.add(newPackedPacket);
                packedPacket = packet;
                break;
            }

            if (position + packet.length > PACKED_PACKET_SIZE) {
                int subsize = PACKED_PACKET_SIZE - position;
                System.arraycopy(packet, offset, packedPacket, position, subsize);
                buffer.add(packedPacket);

                if (!bufferResultSet) {
                    write(messageEvent.getChannel(), buffer);
                    buffer.clear();
                }

                packedPacket = new byte[PACKED_PACKET_SIZE];
                position = offset;
                System.arraycopy(packet, subsize, packedPacket, position, packet.length - subsize);
                position += packet.length - subsize;
            } else {
                System.arraycopy(packet, offset, packedPacket, position, packet.length);
                position += packet.length;
            }
        }
        buffer.add(packedPacket);

        if (!bufferResultSet) {
            write(messageEvent.getChannel(), buffer);
            buffer.clear();
        }

        if (Packet.getType(packet) == Flags.ERR) {
            return buffer;
        }

        if (EOF.loadFromPacket(packet).hasStatusFlag(Flags.SERVER_MORE_RESULTS_EXISTS)) {
            buffer.add(Packet.read_packet(in));
            buffer = readFullResultSet(in, messageEvent, buffer, bufferResultSet);
        }
        return buffer;
    }
    /**
     * Writes all the output bytes into the output ChannelBuffer
     */
    private void write(Channel channel, ArrayList<byte[]> buffer) {

        for (byte[] packet : buffer) {
            ChannelBuffer cb = ChannelBuffers.copiedBuffer(packet);
            channel.write(cb);
        }
    }

}
