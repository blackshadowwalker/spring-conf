package org.springframework.conf.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.conf.listener.ConfChangedListener;
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

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 17:52
 * Description:
 */
public class ConfMonitorBean implements BeanFactoryPostProcessor, InitializingBean, DisposableBean, ApplicationContextAware, ApplicationListener, BeanNameAware {
    protected static Log logger = LogFactory.getLog(ConfMonitorBean.class);

    private PropertyPlaceholderConfigurer propertyPlaceholderConfigurer;
    private String propertyPlaceholderConfigurerName;
    private List<String> files;
    private List<ConfChangedListener> listeners;

    public void setListeners(List<ConfChangedListener> listeners) {
        this.listeners = listeners;
    }

    public void setPropertyPlaceholderConfigurerName(String propertyPlaceholderConfigurerName) {
        this.propertyPlaceholderConfigurerName = propertyPlaceholderConfigurerName;
    }

    public void setPropertyPlaceholderConfigurer(PropertyPlaceholderConfigurer propertyPlaceholderConfigurer) {
        this.propertyPlaceholderConfigurer = propertyPlaceholderConfigurer;
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
            confMonitorConfig.setPropertyPlaceholderConfigurer(this.propertyPlaceholderConfigurer);
            List<String> files = new ArrayList<String>();
            files.addAll(this.files);
            confMonitorConfig.setFiles(files);
            List<ConfChangedListener> listeners = new ArrayList<ConfChangedListener>();
            listeners.addAll(this.listeners);
            confMonitorConfig.setListeners(listeners);
            confMonitorConfig.init();
            if (this.name != null)
                confMonitorMain.setName(this.name);
            confMonitorMain.setConfMonitorConfig(confMonitorConfig);
            confMonitorMain.start();
            System.out.println(this.beanName + "@" + this.hashCode() + " onApplicationEvent");
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
            Field flocations = PropertiesLoaderSupport.class.getDeclaredField("locations");
            if (flocations != null) {
                flocations.setAccessible(true);
                Resource[] locations = (Resource[]) flocations.get(this.propertyPlaceholderConfigurer);
                if (locations != null) {
                    this.files = new ArrayList<String>();
                    for(Resource resource : locations){
                        try {
                            this.files.add(resource.getURL().toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(propertyPlaceholderConfigurerName);
        MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
        PropertyValue propertyValue = mutablePropertyValues.getPropertyValue("locations");
        ManagedList<TypedStringValue> managedList = (ManagedList) propertyValue.getValue();
        try {
            this.files = new ArrayList<String>();
            for (TypedStringValue value : managedList) {
                this.files.add(value.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
