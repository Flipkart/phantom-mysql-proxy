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

    ArrayList<byte[]> buffer;
    private long sequenceId;

    public MysqlConnection(String host, int port, ArrayList<ArrayList<byte[]>> connRefBytes) throws Exception {

        try {

            this.mysqlSocket = new Socket(host, port);
            this.mysqlSocket.setPerformancePreferences(0, 2, 1);
            this.mysqlSocket.setTcpNoDelay(true);
            this.mysqlSocket.setTrafficClass(0x10);
            this.mysqlSocket.setKeepAlive(true);
            //logger.info("Connected to mysql server at "+host+":"+port);
            this.mysqlIn = new BufferedInputStream(this.mysqlSocket.getInputStream(), 16384);
            this.mysqlOut = this.mysqlSocket.getOutputStream();

        } catch (Exception e) {
            throw e;
        }

        /* Assuming an successful connection by replaying the client connRefBytes. There is a possibility
        that in this phase there are error in mysql connection and requests may not get handled by the proxy.
        Need to handle scenarios when proxy connection fails.
        */

        byte[] packet = Packet.read_packet(this.mysqlIn);

        for (ArrayList<byte[]> buf : connRefBytes) {
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

