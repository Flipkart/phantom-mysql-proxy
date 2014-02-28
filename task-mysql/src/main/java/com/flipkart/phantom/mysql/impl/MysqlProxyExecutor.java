package com.flipkart.phantom.mysql.impl;

import com.flipkart.phantom.task.spi.Executor;
import com.flipkart.phantom.task.spi.RequestWrapper;
import com.flipkart.phantom.task.spi.TaskContext;
import com.netflix.hystrix.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Implements the HystrixCommand class for executing HTTP proxy requests
 * @author : samaitra
 * @version : 1.0
 * @date : 15/11/13
 */
public class MysqlProxyExecutor extends HystrixCommand<InputStream> implements Executor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlProxyExecutor.class);

    /** the proxy client */
    private MysqlProxy proxy;

    /** current task context */
    private TaskContext taskContext;

    MysqlRequestWrapper mysqlRequestWrapper;

    /** only constructor uses the proxy client, task context and the mysql requestWrapper */
    public MysqlProxyExecutor(MysqlProxy proxy, TaskContext taskContext, RequestWrapper requestWrapper) {

        super(
                HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(proxy.getGroupKey()))
                        .andCommandKey(HystrixCommandKey.Factory.asKey(proxy.getCommandKey()+((MysqlRequestWrapper)requestWrapper).getCommandKey()))
                        .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(proxy.getThreadPoolKey()))
                        .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(proxy.getThreadPoolSize()))
                        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(proxy.getOperationTimeout()))
        );
        this.proxy = proxy;
        this.taskContext = taskContext;

        /** Get the Mysql Request */
       this.mysqlRequestWrapper = (MysqlRequestWrapper) requestWrapper;

    }
    /**
     * Interface method implementation
     * @return response ResultSet for the give request
     * @throws Exception
     */
    @Override
    public InputStream run() {
        try{
        return this.proxy.doRequest(this.mysqlRequestWrapper);
        }catch(Exception e){
            LOGGER.error("Error while making mysql request : " + e.getMessage(),e);
        }
        return null;
    }

    /**
     * Interface method implementation
     * @return response ResultSet from the fallback
     * @throws Exception
     */
    @Override
    protected InputStream getFallback() {
        return this.proxy.fallbackRequest(this.mysqlRequestWrapper);
    }

    /** Getter/Setter methods */
    public MysqlProxy getProxy() {
        return proxy;
    }
    /** End Getter/Setter methods */
}
