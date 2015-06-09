package org.springframework.conf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.conf.config.io.ClassPathResource;
import org.springframework.conf.config.io.FileResource;
import org.springframework.conf.config.io.Resource;
import org.springframework.conf.config.io.URLResource;
import org.springframework.conf.listener.ConfChangedListener;

import java.util.ArrayList;
import java.util.List;

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
    private List<Resource> locations = new ArrayList<Resource>();
    private long DEFAULT_POLLING_TIME = 5000;

    public void setConfMonitorConfig(ConfMonitorConfig confMonitorConfig) {
        this.confMonitorConfig = confMonitorConfig;
    }

    public void start(){
        if(isRunning)
            return ;
        if(this.getName()==null)
            this.setName("SpringConfMonitorMain");
        this.setDaemon(true);
        log.info( this.getName() + "@" + this.hashCode() + " start");
        super.start();
        this.isRunning = true;
    }

    public static final String FILE_URL_PREFIX = "file:";
    public static final String CLASSPATH_URL_PREFIX = "classpath:";
    public static final String HTTP_URL_PREFIX = "http";

    @Override
    public void run() {
        List<String> files = confMonitorConfig.getFiles();
        long pollTime = confMonitorConfig.getPollingTime();
        if(pollTime<1)
            pollTime = this.DEFAULT_POLLING_TIME;
        try {
            for (String location : files) {
                try {
                    Resource resource = null;
                    if (location.startsWith(CLASSPATH_URL_PREFIX)) {
                        resource = new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
                    } else if (location.startsWith(HTTP_URL_PREFIX)) {
                        resource = new URLResource(location);
                    } else {
                        resource = new FileResource(location);
                    }
                    resource.lastModified();
                    if (resource != null) {
                        log.info(this.getName()+": lastModified is " + resource.lastModified() + " [" + resource.getFilename() + "]");
                        locations.add(resource);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ;
        }
        while(true){
            try {
                if(isExit)
                    break ;
                Thread.sleep(pollTime);
                for(Resource resource : this.locations){
                    if(resource.isModified()){
                        log.info("\r\nfile is modified "+resource.getFilename());
                        List<ConfChangedListener> listeners = this.confMonitorConfig.getListeners();
                        if(listeners !=null) {
                            for (ConfChangedListener listener : listeners) {
                                try {
                                    listener.fileChanged(resource.getURL());
                                    log.info("notified listener " + listener);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }//end while\
        System.out.println(this.getName()+"Exit @" + this.hashCode() + "  " + this);
        log.warn(this.getName()+"Exit@" + this.hashCode() + "  " + this);
    }

    public void stopMonitor(){
        this.isExit = true;
    }


}
