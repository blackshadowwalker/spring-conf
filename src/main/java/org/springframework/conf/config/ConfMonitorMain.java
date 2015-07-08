package org.springframework.conf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.conf.listener.FileChangedListener;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wi with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 20:53
 * Description:
 */
public class ConfMonitorMain extends Thread {
    protected static Log log = LogFactory.getLog(ConfMonitorMain.class);

    private volatile boolean isRunning = false;
    private volatile boolean isExit = false;
    private ConfMonitorConfig confMonitorConfig;
    private long DEFAULT_POLLING_TIME = 5000;

    public void setConfMonitorConfig(ConfMonitorConfig confMonitorConfig) {
        this.confMonitorConfig = confMonitorConfig;
    }

    public synchronized void start() {
        if (isRunning)
            return;
        this.isRunning = true;
        if (this.getName() == null)
            this.setName("SpringConfMonitorMain");
        this.setDaemon(true);
        log.info(this.getName() + "@" + Integer.toHexString(this.hashCode()) + " start");
        super.start();
    }

    private volatile Map<Resource, Long> filesModifyCheckMap = new HashMap<Resource, Long>();

    @Override
    public void run() {
        List<Resource> files = confMonitorConfig.getFiles();
        long pollingInterval = confMonitorConfig.getPollingTime();
        if (pollingInterval < 1) {
            pollingInterval = this.DEFAULT_POLLING_TIME;
        }
        for (Resource resource : files) {
            try {
                long lastModified = resource.lastModified();
                log.info(this.getName() + ": lastModified at " + resource.lastModified() + " [" + resource + "]");
                filesModifyCheckMap.put(resource, lastModified);
            } catch (FileNotFoundException notFound) {
                log.error("FileNotFoundException file[" + resource + "]: " + notFound.getMessage());
            } catch (Exception e) {
                log.error("load file[" + resource + "] error ", e);
            }
        }
        log.info("-------------------------------------------------------------------------------------");
        while (true) {
            if (isExit)
                break;
            try {
                Thread.sleep(pollingInterval);
            } catch (Exception e) {
                log.error("Thread sleep error ", e);
            }
            for (Resource resource : files) {
                if (!resource.exists()) {
                    continue;
                }
                try {
                    long lastModified = resource.lastModified();
                    if (lastModified > filesModifyCheckMap.get(resource)) {
                        log.info("file is changed " + resource);
                        List<FileChangedListener> listeners = this.confMonitorConfig.getListeners();
                        if (listeners != null) {
                            for (FileChangedListener listener : listeners) {
                                try {
                                    listener.fileChanged(resource.getURL());
                                    log.info("notified listener " + listener);
                                } catch (Exception e) {
                                    log.error("notify to listener error ", e);
                                }
                                log.info("-------------------------------------------------------------------------------------");
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("monitor file[" + resource + "] error", e);
                }
            }
        }//end while\
        log.info(this.getName() + "@" + Integer.toHexString(this.hashCode()) + "  Exit " + this);
    }

    public void stopMonitor() {
        this.isExit = true;
    }

}
