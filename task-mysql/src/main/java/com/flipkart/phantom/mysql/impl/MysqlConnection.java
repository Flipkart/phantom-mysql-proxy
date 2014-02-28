package com.flipkart.phantom.mysql.impl;

import com.github.jmpjct.mysql.proto.Flags;
import com.github.jmpjct.mysql.proto.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


 /**
 *  Class for Mysql Connection.
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlConnection {

     /** Logger for this class*/
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


    public MysqlConnection(String host, int port,ArrayList<ArrayList<byte[]>> connRefBytes) throws Exception{

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
        int c = 0;
        for(ArrayList<byte[]> buf : connRefBytes){
            //logger.info("connRefBytes : "+new String(buf.get(0)));
            Packet.write(this.mysqlOut, buf);

            if(c>0){

                boolean bufferResultSet = false;
                OutputStream clientOut = new ByteArrayOutputStream();
                packet = Packet.read_packet(this.mysqlIn);
                this.buffer.add(packet);
                this.sequenceId = Packet.getSequenceId(packet);

                switch (Packet.getType(packet)) {
                    case Flags.OK:
                    case Flags.ERR:
                        break;

                    default:
                        this.buffer = Packet.read_full_result_set(this.mysqlIn, clientOut, this.buffer, bufferResultSet);
                        break;
                }
            }else{
                packet = Packet.read_packet(this.mysqlIn);
                this.buffer = new ArrayList<byte[]>();
                this.buffer.add(packet);

                if (Packet.getType(packet) != Flags.OK) {
                    logger.debug("Auth is not okay!");
                }
            }
            c++;

        }
    }
}
