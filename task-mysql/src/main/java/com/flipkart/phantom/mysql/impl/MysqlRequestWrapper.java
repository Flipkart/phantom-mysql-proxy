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

import com.flipkart.phantom.task.spi.RequestWrapper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;


/**
 * <code>MysqlRequestWrapper</code> has the the Mysql request buffer wrapped in ArrayList object.
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlRequestWrapper implements RequestWrapper {

    /**
     * Mysql request buffer wrapped in ArrayList object
     */
    private ArrayList<byte[]> buffer;
    /**
     * Mysql command key (type of sql query)
     */
    private String commandKey;
    /**
     * Mysql socket for the connection
     */
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
     * Start Getter/Setter methods
     */
    public Socket getMysqlSocket() {
        return mysqlSocket;
    }
    public void setMysqlSocket(Socket mysqlSocket) {
        this.mysqlSocket = mysqlSocket;
    }
    public InputStream getMysqlIn() {
        return mysqlIn;
    }
    public void setMysqlIn(InputStream mysqlIn) {
        this.mysqlIn = mysqlIn;
    }
    public OutputStream getMysqlOut() {
        return mysqlOut;
    }
    public void setMysqlOut(OutputStream mysqlOut) {
        this.mysqlOut = mysqlOut;
    }
    public ArrayList<byte[]> getBuffer() {
        return buffer;
    }
    public void setBuffer(ArrayList<byte[]> buffer) {
        this.buffer = buffer;
    }
    public String getCommandKey() {
        return commandKey;
    }
    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }
    /**End Getter/Setter methods */
}