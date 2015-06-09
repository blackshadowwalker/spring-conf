package org.springframework.conf.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 15:53
 * Description:
 */
public class DefaultConfChangedListener implements ConfChangedListener, ApplicationListener, ApplicationContextAware, BeanNameAware, InitializingBean {
    protected static Log logger = LogFactory.getLog(DefaultConfChangedListener.class);

    protected String name;
    protected String desp;
    protected ApplicationContext applicationContext;
    protected boolean started = false;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }

    private class RestartSpring extends Thread{
        @Override
        public void run() {
            while (true) {
                try {
                    for (ConfigurableApplicationContext cac : DefaultConfChangedListener.this.configurableApplicationContextList) {
                        cac.refresh();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    DefaultConfChangedListener.this.sleep(5000);
                }
                break;
            }
            logger.info("restart spring finish[SUCCESS]");
        }
    }

    private RestartSpring restartSpring = new RestartSpring();
    @Override
    public void fileChanged(final URL url) {
        logger.info("[" + this.name + "]: file is refreshed  " + url);
        if (started) {
            if(restartSpring.isAlive()){
                restartSpring.stop();
            }
            restartSpring.setName("RestartSpring");
            restartSpring.setDaemon(true);
            restartSpring.start();
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
        logger.debug("[" + this.name + "] onApplicationEvent event=" + event + " source=" + event.getSource());
        if (event.getSource() instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext cac = (ConfigurableApplicationContext) event.getSource();
            if(!configurableApplicationContextList.contains(cac)) {
                configurableApplicationContextList.add(cac);
                logger.info(this.name + "@" + this.hashCode() + " addConfigurableApplicationContext  " + cac + " size=" + configurableApplicationContextList.size());
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