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

package com.flipkart.phantom.mysql.impl;

import com.github.mpjct.jmpjct.mysql.proto.Flags;
import com.github.mpjct.jmpjct.mysql.proto.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Class for Mysql Connection.
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlConnection {

    /**
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(MysqlConnection.class);

    /**
     * mysql socket to connect mysql server
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
     * Mysql packet size
     */
    private static final int MYSQL_PACKET_SIZE = 16384;
    /**
     * @param  connectionTime
     *         An <tt>int</tt> expressing the relative importance of a short
     *         connection time
     *
     * @param  latency
     *         An <tt>int</tt> expressing the relative importance of low
     *         latency
     *
     * @param  bandwidth
     *         An <tt>int</tt> expressing the relative importance of high
     *         bandwidth
     */
    private static final int connectionTime = 0;
    private static final int latency = 2;
    private static final int bandwidth = 1;

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
    private final static int tc = 0x10;

    ArrayList<byte[]> buffer;

    /**
     * Mysql packet sequence Id.
     */
    private long sequenceId;

    public MysqlConnection(String host, int port, ArrayList<ArrayList<byte[]>> userCredentials) throws Exception {

        try {

            this.mysqlSocket = new Socket(host, port);
            this.mysqlSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
            this.mysqlSocket.setTcpNoDelay(true);
            this.mysqlSocket.setTrafficClass(tc);
            this.mysqlSocket.setKeepAlive(true);
            this.mysqlIn = new BufferedInputStream(this.mysqlSocket.getInputStream(), MYSQL_PACKET_SIZE);
            this.mysqlOut = this.mysqlSocket.getOutputStream();

        } catch (Exception e) {
            throw e;
        }

        /* Assuming an successful connection by replaying the client connRefBytes. There is a possibility
        that in this phase there are error in mysql connection and requests may not get handled by the proxy.
        Need to handle scenarios when proxy connection fails.
        */

        byte[] packet = Packet.read_packet(this.mysqlIn);

        for (ArrayList<byte[]> buf : userCredentials) {
            Packet.write(this.mysqlOut, buf);

                packet = Packet.read_packet(this.mysqlIn);
                this.buffer = new ArrayList<byte[]>();
                this.buffer.add(packet);

                if (Packet.getType(packet) != Flags.OK) {
                    logger.debug("Auth is not okay!");
                }
            }

        }
}

