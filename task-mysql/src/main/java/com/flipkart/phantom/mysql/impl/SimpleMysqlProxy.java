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

import java.io.InputStream;

/**
 * A simple implementation for the MysqlProxy abstract class
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class SimpleMysqlProxy extends MysqlProxy {

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.mysql.impl.MysqlProxy#fallbackRequest(com.flipkart.phantom.mysql.impl.MysqlRequestWrapper)
     */
    @Override
    public InputStream fallbackRequest(MysqlRequestWrapper mysqlRequestWrapper) {
        return null;
    }
    /**
     * Abstract method implementation
     *
     * @return String Group name
     */
    @Override
    public String getGroupKey() {
        return "SimpleMysqlProxy";
    }
    /**
     * Abstract method implementation
     *
     * @return String Command name
     */
    @Override
    public String getCommandKey() {
        return this.getName() + "MysqlPool";
    }
    /**
     * Abstract method implementation
     *
     * @return String Thread pool name
     */
    @Override
    public String getThreadPoolKey() {
        return "SimpleMysqlThreadPool";
    }
    @Override
    public int getOperationTimeout() {
        return 10000;
    }
}
