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

import com.flipkart.phantom.task.spi.AbstractHandler;
import com.flipkart.phantom.task.spi.TaskContext;
import com.github.mpjct.jmpjct.mysql.proto.Com_Query;
import com.github.mpjct.jmpjct.mysql.proto.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * /**
 * Abstract class for handling Mysql proxy requests
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */

public abstract class MysqlProxy extends AbstractHandler {

    /**
     * Host to connect to
     */
    public String host;
    /**
     * port to connect to
     */
    public int port;
    /**
     * The default thread pool size
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private static Logger logger = LoggerFactory.getLogger(MysqlProxy.class);
    /**
     * Name of the proxy
     */
    private String name;
    /**
     * The thread pool size for this proxy
     */
    private int threadPoolSize = MysqlProxy.DEFAULT_THREAD_POOL_SIZE;
    /**
     * socket timeout in milis
     */
    private int operationTimeout;
    @Override
    public void init(TaskContext context) throws Exception {
    }
    @Override
    public void shutdown(TaskContext context) throws Exception {
    }
    /**
     * The main method which makes the Mysql request
     */
    public InputStream doRequest(MysqlRequestWrapper mysqlRequestWrapper) throws Exception {

        ArrayList<byte[]> buffer = mysqlRequestWrapper.getBuffer();
        String query = Com_Query.loadFromPacket(buffer.get(0)).query;

        //logger.info("Query to mysql from proxy :" + query);
        Packet.write(mysqlRequestWrapper.getMysqlOut(), buffer);
        return mysqlRequestWrapper.getMysqlIn();
    }
    /**
     * Abstract fallback request method
     *
     * @param mysqlRequestWrapper Mysql request
     * @return ResultSet response after executing the fallback
     */
    public abstract InputStream fallbackRequest(MysqlRequestWrapper mysqlRequestWrapper);
    /**
     * Abstract method which gives group key
     *
     * @return String group key
     */
    public abstract String getGroupKey();
    /**
     * Abstract method which gives command name
     *
     * @return String command name
     */
    public abstract String getCommandKey();
    /**
     * Abstract method which gives the thread pool name
     *
     * @return String thread pool name
     */
    public abstract String getThreadPoolKey();
    /**
     * Returns the thread pool size
     *
     * @return thread pool size
     */
    public int getThreadPoolSize() {
        return this.threadPoolSize;
    }
    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.AbstractHandler#getDetails()
     */
    public String getDetails() {
        if (this != null) {
            String details = "Endpoint: ";
            details += "jdbc:mysql://" + this.getHost() + ":" + this.getPort() + "\n";
            return details;
        }
        return "No endpoint configured";
    }
    /**
     * Abstract method implementation
     *
     * @see AbstractHandler#getType()
     */
    @Override
    public String getType() {
        return "MysqlProxy";
    }
    /**
     * getters / setters
     */
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
    public int getOperationTimeout() {
        return operationTimeout;
    }
    public void setOperationTimeout(int operationTimeout) {
        this.operationTimeout = operationTimeout;
    }
    /** getters / setters */
}
