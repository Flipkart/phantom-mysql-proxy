package com.flipkart.phantom.mysql.impl;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * <code>MysqlConnectionObjectFactory</code> is a @link{PoolableObjectFactory} for Socket instances meant to be used with {@link org.apache.commons.pool.impl.GenericObjectPool}
 * It is initialized with a Mysql proxy or it's parameters and is passed to a GenericObjectPool object
 *
 * @author samaitra
 * @version 1.0
 * @date : 16/11/13
 *
 */
public class MysqlConnectionObjectFactory implements PoolableObjectFactory<MysqlConnection> {

    /** Mysql Proxy instance for initializing the Factory */
    private MysqlProxy mysqlProxy;


    private ArrayList<ArrayList<byte[]>> connRefBytes;

    /**
     * Constructor for initializing this Factory with a MysqlProxy
     *
     */
    public MysqlConnectionObjectFactory(MysqlProxy mysqlProxy,ArrayList<ArrayList<byte[]>> connRefBytes) {
        this.setMysqlProxy(mysqlProxy);
        this.setConnRefBytes(connRefBytes);
    }


    /** Logger for this class*/
    private static final Logger LOGGER = LoggerFactory.getLogger(MysqlConnectionObjectFactory.class);

    /**
     * Interface method implementation. Creates and returns a new {@link com.flipkart.phantom.mysql.impl.MysqlConnection}
     * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
     */
    public MysqlConnection makeObject() throws Exception {
       return new MysqlConnection(mysqlProxy.getHost(),mysqlProxy.getPort(),this.connRefBytes);
    }

    /**
     * Interface method implementation. Closes the specified Mysql socket instance
     * @see org.apache.commons.pool.PoolableObjectFactory#destroyObject(Object)
     */

    public void destroyObject(MysqlConnection conn) throws Exception {
        LOGGER.info("Closing a mysql connection for server : {} at port : {}", this.getMysqlProxy().getHost(), this.getMysqlProxy().getPort());
        conn.mysqlSocket.close();
        conn.mysqlOut.close();
        conn.mysqlIn.close();
    }

    /**
     * Interface method implementation. Checks if the socket is open and then attempts to set Mysql specific socket properties.
     * An error in any of these operations will invalidate the specified Mysql Socket.
     * @see org.apache.commons.pool.PoolableObjectFactory#validateObject(Object)
     */
    public boolean validateObject(MysqlConnection conn) {
        if (conn.mysqlSocket.isClosed()) {
            return false;
        }
        try {
            conn.mysqlSocket.setSoLinger(false, 0);
            conn.mysqlSocket.setTcpNoDelay(true);
            return true;
        } catch (Exception e) {
            LOGGER.info("Mysql connection is not valid for server : {} at port : {}", this.getMysqlProxy().getHost(), this.getMysqlProxy().getPort());
            return false;
        }
    }

    /**
     * Interface method implementation. Does nothing
     * @see org.apache.commons.pool.PoolableObjectFactory#activateObject(Object)
     */
    public void activateObject(MysqlConnection conn) throws Exception {
        // no op
    }

    /**
     * Interface method implementation. Does nothing
     * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(Object)
     */
    public void passivateObject(MysqlConnection conn) throws Exception {
        // no op
    }


    /** Getter/Setter Methods */

    public MysqlProxy getMysqlProxy() {
        return mysqlProxy;
    }

    public void setMysqlProxy(MysqlProxy mysqlProxy) {
        this.mysqlProxy = mysqlProxy;
    }

    public ArrayList<ArrayList<byte[]>> getConnRefBytes() {
        return connRefBytes;
    }

    public void setConnRefBytes(ArrayList<ArrayList<byte[]>> connRefBytes) {
        this.connRefBytes = connRefBytes;
    }

    /** End Getter/Setter Methods */
}
