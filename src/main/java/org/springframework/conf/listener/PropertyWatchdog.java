package org.springframework.conf.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 15:53
 * Description:
 */
public class PropertyWatchdog implements FileChangedListener, ApplicationListener, ApplicationContextAware, BeanNameAware, InitializingBean {
    protected static Log log = LogFactory.getLog(PropertyWatchdog.class);

    protected String name;
    protected String desp;
    protected ApplicationContext applicationContext;
    protected boolean started = false;
    protected Object lock = new Object();
    protected Object refreshingLock = new Object();
    protected volatile boolean refreshing = true;

    public boolean isRefreshing(){
        return this.refreshing;
    }

    @Override
    public String toString() {
        return this.name != null ? this.name + "@" + Integer.toHexString(this.hashCode()) : super.toString();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }

    public void refreshSpring() throws Exception{
        synchronized (refreshingLock) {
            refreshing = true;
            for (ConfigurableApplicationContext cac : PropertyWatchdog.this.configurableApplicationContextList) {
                cac.getBeanFactory().destroySingletons();
            }
            log.info("refreshSpring ... ");
//            Thread.sleep(1000 * 30);
            for (ConfigurableApplicationContext cac : PropertyWatchdog.this.configurableApplicationContextList) {
                cac.refresh();
            }
            refreshing = false;
        }
    }

    private class RestartSpring extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    refreshSpring();
                    break;
                } catch (Exception e) {
                    log.error("refresh spring application context error, try again later", e);
                    PropertyWatchdog.this.sleep(5000);
                }
            }
            log.info("refresh spring application context finish[SUCCESS]");
        }
    }

    private int sum = 0;
    @Override
    public synchronized void fileChanged(final URL url) throws Exception{
        log.info("[" + this.name + "]: file is refreshed  " + url);
        if(refreshing)
            return ;
        synchronized (lock) {
            sum ++;
            RestartSpring restartSpring = new RestartSpring();
            if (started) {
                restartSpring.setName("RestartSpring#"+sum);
                restartSpring.setDaemon(true);
                try {
                    restartSpring.start();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    private Set<ConfigurableApplicationContext> configurableApplicationContextList = new HashSet<ConfigurableApplicationContext>();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.debug("[" + this.name + "] onApplicationEvent event=" + event + " source=" + event.getSource());
        if(event instanceof ContextRefreshedEvent) {
            if (event.getSource() instanceof ConfigurableApplicationContext) {
                if (!configurableApplicationContextList.contains(event.getSource())) {
                    configurableApplicationContextList.add((ConfigurableApplicationContext)event.getSource());
                    log.info(this.name + "@" + this.hashCode() + " addConfigurableApplicationContext  " + event.getSource() + " size=" + configurableApplicationContextList.size());
                    this.started = true;
                    this.refreshing = false;
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanName(String name) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

}