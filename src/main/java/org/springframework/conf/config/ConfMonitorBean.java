package org.springframework.conf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.beans.factory.config.*;
import org.springframework.conf.listener.FileChangedListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderSupport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 17:52
 * Description:
 */
public class ConfMonitorBean implements BeanFactoryPostProcessor, InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener, BeanNameAware {
    protected static Log log = LogFactory.getLog(ConfMonitorBean.class);

    private Set<PropertyPlaceholderConfigurer> propertyPlaceholderConfigurers;
    private List<Resource> resources;
    private List<FileChangedListener> listeners;
    private int pollingInterval = 2000;

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public void setListeners(List<FileChangedListener> listeners) {
        this.listeners = listeners;
    }

    public void setPropertyPlaceholderConfigurers(Set<PropertyPlaceholderConfigurer> propertyPlaceholderConfigurers) {
        this.propertyPlaceholderConfigurers = propertyPlaceholderConfigurers;
    }

    private ApplicationContext applicationContext;
    private String id;
    private String name;
    private String beanName;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private ConfMonitorMain confMonitorMain = new ConfMonitorMain();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event.getClass().getName().equals(ContextRefreshedEvent.class.getName())) {
            ConfMonitorConfig confMonitorConfig = new ConfMonitorConfig();
            confMonitorConfig.setPollingTime(this.pollingInterval);
            confMonitorConfig.setPropertyPlaceholderConfigurers(this.propertyPlaceholderConfigurers);
            List<Resource> files = new ArrayList<Resource>();
            files.addAll(this.resources);
            confMonitorConfig.setFiles(files);
            List<FileChangedListener> listeners = new ArrayList<FileChangedListener>();
            listeners.addAll(this.listeners);
            confMonitorConfig.setListeners(listeners);
            confMonitorConfig.init();
            if (this.name != null)
                confMonitorMain.setName(this.name);
            confMonitorMain.setConfMonitorConfig(confMonitorConfig);
            confMonitorMain.start();
            log.info(this.beanName + "@" + this.hashCode() + " onApplicationEvent");
        } else {
            log.debug(event);
        }
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void destroy() throws Exception {
        confMonitorMain.stopMonitor();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        try {
            this.resources = new ArrayList<Resource>();
            Field flocations = PropertiesLoaderSupport.class.getDeclaredField("locations");
            if (flocations != null) {
                flocations.setAccessible(true);
                for (PropertyPlaceholderConfigurer propertyPlaceholderConfigurer : this.propertyPlaceholderConfigurers) {
                    Resource[] locations = (Resource[]) flocations.get(propertyPlaceholderConfigurer);
                    if (locations != null) {
                        for (Resource resource : locations) {
                            try {
                                this.resources.add(resource);
                            } catch (Exception e) {
                                log.error("add resources[" + resource + "] to monitor list error", e);
                            }
                        }//end for
                    }//end if
                }//end for
            }//end if
        } catch (Exception e) {
            throw new BootstrapException("resolve locations error", e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
