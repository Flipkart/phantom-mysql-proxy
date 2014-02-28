# Phantom Examples
The examples that follow explore the various uses of Phantom.

## Example: Mysql proxy in 5 minutes
Please refer to https://github.com/Flipkart/phantom/wiki/Maven-dependencies to setup the requisite repository location in your Maven build settings.
The sample Mysql proxy simply forwards Mysql queries from clients  to a target server and relays the response received.
Follow these steps to setup and run your first Phantom Mysql proxy:

* git clone phantom-mysql-proxy
* Run `mvn clean install` in `sample-mysql-proxy` directory. This might take a while to download Phantom, Trooper and their dependencies from the various Maven repositories.
* Start the proxy in `sample-mysql-proxy` directory:

```xml
java -cp "./target/lib/*" \
org.trpr.platform.runtime.impl.bootstrap.BootstrapLauncher \
./src/main/resources/external/bootstrap.xml
```

* A successful start will display a message like below on the console:

```xml
*************************************************************************
 Trooper __
      __/  \         Runtime Nature : SERVER
   __/  \__/         Component Container : com.flipkart.phantom.runtime.impl.spring.ServiceProxyComponentContainer
  /  \__/  \         Startup Time : 2,320 ms
  \__/  \__/         Host Name:ReguMac.local
     \__/
*************************************************************************
19:17:34.367 [main] INFO  o.t.p.r.i.bootstrap.spring.Bootstrap - ** Trooper Bootstrap complete **
```

* Point your mysql database config to host: localhost and port : 8080. You should see the mysql server configured in Mysql proxy config file located at: `sample-mysql-proxy/src/main/resources/external/spring-proxy-handler-config.xml` and `sample-mysql-proxy/src/main/resources/external/spring-proxy-listener-config.xml`
* The monitoring console is available at : `http://localhost:8081/admin/dashboard`. This is the Hystrix console for deployed handlers and commands.

## Phantom Consoles
![Monitor](https://github.com/Flipkart/phantom-mysql-proxy/raw/master/docs/console.png)
