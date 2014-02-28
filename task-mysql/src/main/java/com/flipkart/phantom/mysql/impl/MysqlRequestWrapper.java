package com.flipkart.phantom.mysql.impl;

import com.flipkart.phantom.task.spi.RequestWrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * <code>MysqlRequestWrapper</code> has the the Mysql request buffer wrapped in ArrayList object.
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 *
 */
public class MysqlRequestWrapper implements RequestWrapper{


    /** flag to determine the state of request */
    private int flag;

    /** Mysql request buffer wrapped in ArrayList object */
    private ArrayList<byte[]> buffer;

    /** Mysql connection credentials*/
    private ArrayList<ArrayList<byte[]>> connRefBytes;

    /** Mysql command key (type of sql query) */
    private  String commandKey;

    /** Start Getter/Setter methods */

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }


    public ArrayList<byte[]> getBuffer() {
        return buffer;
    }

    public void setBuffer(ArrayList<byte[]> buffer) {
        this.buffer = buffer;
    }

    public ArrayList<ArrayList<byte[]>> getConnRefBytes() {
        return connRefBytes;
    }

    public void setConnRefBytes(ArrayList<ArrayList<byte[]>> connRefBytes) {
        this.connRefBytes = connRefBytes;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }


/**End Getter/Setter methods */

}