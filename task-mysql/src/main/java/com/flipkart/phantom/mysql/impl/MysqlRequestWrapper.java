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
     * flag to determine the state of request
     */
    private int flag;

    /**
     * Mysql request buffer wrapped in ArrayList object
     */
    private ArrayList<byte[]> buffer;

    /**
     * Mysql connection credentials
     */
    private ArrayList<ArrayList<byte[]>> connRefBytes;

    /**
     * Mysql command key (type of sql query)
     */
    private String commandKey;

    /**
     * Start Getter/Setter methods
     */

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