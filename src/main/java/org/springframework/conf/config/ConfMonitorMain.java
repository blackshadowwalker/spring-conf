package org.springframework.conf.config;

import org.springframework.conf.config.io.ClassPathResource;
import org.springframework.conf.config.io.FileResource;
import org.springframework.conf.config.io.Resource;
import org.springframework.conf.config.io.URLResource;
import org.springframework.conf.listener.ConfChangedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Wi with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 20:53
 * Description:
 */
public class ConfMonitorMain extends Thread {
    private static Logger log = Logger.getLogger(ConfMonitorMain.class.getName());
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
                        log.info(this.getName()+" init lastModified is " + resource.lastModified() + " [" + resource.getFilename() + "]");
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
             //   System.out.print(".");
                for(Resource resource : this.locations){
                    if(resource.isModified()){
                        log.info("\r\nfile is modified "+resource.getFilename());
                        List<ConfChangedListener> listeners = this.confMonitorConfig.getListeners();
                        if(listeners !=null) {
                            for (ConfChangedListener listener : listeners) {
                                listener.fileChanged(resource.getURL());
                            }
                        }
                    }
                }
                Thread.sleep(pollTime);
            }catch (Exception e){
                e.printStackTrace();
            }
        }//end while\
        log.info(this.getName()+" exit @ "+this);
    }

    public void stopMonitor(){
        this.isExit = true;
        super.stop();
    }


}
