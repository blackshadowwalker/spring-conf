package org.springframework.conf.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;

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

    private class RestartSpring extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    for (ConfigurableApplicationContext cac : PropertyWatchdog.this.configurableApplicationContextList) {
                        cac.refresh();
                    }
                    break;
                } catch (Exception e) {
                    log.error("spring application context refresh error", e);
                    PropertyWatchdog.this.sleep(5000);
                }
            }
            log.info("refresh spring application context finish[SUCCESS]");
        }
    }

    @Override
    public synchronized void fileChanged(final URL url) {
        log.info("[" + this.name + "]: file is refreshed  " + url);
        RestartSpring restartSpring = new RestartSpring();
        if (started) {
            restartSpring.setName("RestartSpring");
            restartSpring.setDaemon(true);
            try {
                restartSpring.start();
            }catch (Exception e){
                log.error(e);
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
        if (event.getSource() instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext cac = (ConfigurableApplicationContext) event.getSource();
            if (!configurableApplicationContextList.contains(cac)) {
                configurableApplicationContextList.add(cac);
                log.info(this.name + "@" + this.hashCode() + " addConfigurableApplicationContext  " + cac + " size=" + configurableApplicationContextList.size());
                this.started = true;
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