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

import com.flipkart.phantom.mysql.impl.registry.MysqlProxyRegistry;
import com.flipkart.phantom.task.spi.Executor;
import com.flipkart.phantom.task.spi.RequestWrapper;
import com.flipkart.phantom.task.spi.TaskContext;
import com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry;
import com.flipkart.phantom.task.spi.repository.ExecutorRepository;
/**
 * Provides a repository of MysqlProxyExecutor classes which execute Mysql requests using Hystrix commands
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlProxyExecutorRepository implements ExecutorRepository {

    /**
     * repository
     */
    private MysqlProxyRegistry registry;
    /**
     * The TaskContext instance
     */
    private TaskContext taskContext;
    /**
     * Returns {@link com.flipkart.phantom.task.spi.Executor} for the specified requestWrapper
     *
     * @param commandName    command Name as specified by Executor. Not used in this case.
     * @param proxyName      proxyName the MysqlProxy name
     * @param requestWrapper requestWrapper Object containing requestWrapper Data
     * @return an {@link MysqlProxyExecutor} instance
     */
    public Executor getExecutor(String commandName, String proxyName, RequestWrapper requestWrapper) {
        MysqlProxy proxy = (MysqlProxy) registry.getHandler(proxyName);

        if (proxy.isActive()) {
            return new MysqlProxyExecutor(proxy, this.taskContext, requestWrapper);
        }
        throw new RuntimeException("The MysqlProxy is not active.");
    }
    /**
     * Getter/Setter methods
     */
    @Override
    public AbstractHandlerRegistry getRegistry() {
        return registry;
    }
    @Override
    public void setRegistry(AbstractHandlerRegistry registry) {
        this.registry = (MysqlProxyRegistry) registry;
    }
    @Override
    public TaskContext getTaskContext() {
        return this.taskContext;
    }
    @Override
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }
    /** End Getter/Setter methods */
}
