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

package com.flipkart.phantom.mysql.impl.registry;

import com.flipkart.phantom.mysql.impl.MysqlProxy;
import com.flipkart.phantom.task.spi.AbstractHandler;
import com.flipkart.phantom.task.spi.TaskContext;
import com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry;
import com.flipkart.phantom.task.spi.registry.HandlerConfigInfo;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

import java.util.*;

/**
 * Implementation of {@link AbstractHandlerRegistry} for MysqlProxy instances
 *
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlProxyRegistry implements AbstractHandlerRegistry {

    /**
     * logger
     */
    private static Logger LOGGER = LogFactory.getLogger(MysqlProxyRegistry.class);

    /**
     * list of proxies by name
     */
    private Map<String, MysqlProxy> proxies = new HashMap<String, MysqlProxy>();

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#init(java.util.List, com.flipkart.phantom.task.spi.TaskContext)
     */
    @Override
    public AbstractHandlerRegistry.InitedHandlerInfo[] init(List<HandlerConfigInfo> handlerConfigInfoList, TaskContext taskContext) throws Exception {
        List<AbstractHandlerRegistry.InitedHandlerInfo> initedHandlerInfos = new LinkedList<InitedHandlerInfo>();
        for (HandlerConfigInfo handlerConfigInfo : handlerConfigInfoList) {
            String[] proxyBeanIds = handlerConfigInfo.getProxyHandlerContext().getBeanNamesForType(MysqlProxy.class);
            for (String proxyBeanId : proxyBeanIds) {
                MysqlProxy proxy = (MysqlProxy) handlerConfigInfo.getProxyHandlerContext().getBean(proxyBeanId);
                try {
                    LOGGER.info("Initializing MysqlProxy: " + proxy.getName());
                    proxy.init(taskContext);
                    proxy.activate();
                    initedHandlerInfos.add(new AbstractHandlerRegistry.InitedHandlerInfo(proxy, handlerConfigInfo));
                } catch (Exception e) {
                    LOGGER.error("Error initializing MysqlProxy {}. Error is: " + e.getMessage(), proxy.getName(), e);
                    throw new PlatformException("Error initializing MysqlProxy: " + proxy.getName(), e);
                }
                this.proxies.put(proxy.getName(), proxy);
            }
        }
        return initedHandlerInfos.toArray(new AbstractHandlerRegistry.InitedHandlerInfo[0]);
    }

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#reinitHandler(String, com.flipkart.phantom.task.spi.TaskContext)
     */
    @Override
    public void reinitHandler(String name, TaskContext taskContext) throws Exception {
        MysqlProxy proxy = this.proxies.get(name);
        if (proxy != null) {
            try {
                proxy.deactivate();
                proxy.shutdown(taskContext);
                proxy.init(taskContext);
                proxy.activate();
            } catch (Exception e) {
                LOGGER.error("Error initializing MysqlProxy {}. Error is: " + e.getMessage(), name, e);
                throw new PlatformException("Error reinitialising MysqlProxy: " + name, e);
            }
        }
    }

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#shutdown(com.flipkart.phantom.task.spi.TaskContext)
     */
    @Override
    public void shutdown(TaskContext taskContext) throws Exception {
        for (String name : proxies.keySet()) {
            try {
                LOGGER.info("Shutting down MysqlProxy: " + name);
                proxies.get(name).shutdown(taskContext);
                proxies.get(name).deactivate();
            } catch (Exception e) {
                LOGGER.warn("Failed to shutdown MysqlProxy: " + name, e);
            }
        }
    }

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#getHandlers()
     */
    @Override
    public List<AbstractHandler> getHandlers() {
        return new ArrayList<AbstractHandler>(proxies.values());
    }

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#getHandler(String)
     */
    @Override
    public AbstractHandler getHandler(String name) {
        return proxies.get(name);
    }

    /**
     * Abstract method implementation
     *
     * @see com.flipkart.phantom.task.spi.registry.AbstractHandlerRegistry#unregisterTaskHandler(com.flipkart.phantom.task.spi.AbstractHandler)
     */
    @Override
    public void unregisterTaskHandler(AbstractHandler taskHandler) {
        this.proxies.remove(taskHandler.getName());
    }
}
