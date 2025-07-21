package org.ppj.pp.event.core.xxljob.server;

import org.ppj.pp.event.core.xxljob.thread.ExecutorRegistryThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy from : https://github.com/xuxueli/xxl-rpc
 *
 * @author xuxueli 2020-04-11 21:25
 */
public class EmbedServer {
    private static final Logger logger = LoggerFactory.getLogger(org.ppj.pp.event.core.xxljob.server.EmbedServer.class);

    public void start(final String address, final String appname) {
        startRegistry(appname, address);
    }

    public void stop() throws Exception {
        // destroy server thread

        // stop registry
        stopRegistry();
        logger.info(">>>>>>>>>>> xxl-job remoting server destroy success.");
    }

    // ---------------------- registry ----------------------

    public void startRegistry(final String appname, final String address) {
        // start registry
        ExecutorRegistryThread.getInstance().start(appname, address);
    }

    public void stopRegistry() {
        // stop registry
        ExecutorRegistryThread.getInstance().toStop();
    }


}
