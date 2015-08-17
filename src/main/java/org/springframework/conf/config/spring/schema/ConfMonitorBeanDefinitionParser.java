package org.springframework.conf.config.spring.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 17:45
 * Description:
 */
public class ConfMonitorBeanDefinitionParser implements BeanDefinitionParser {
    private static Log log = LogFactory.getLog(ConfMonitorBeanDefinitionParser.class.getName());

    private Class<?> beanClass;
    public static final String BEAN_CONF_MONITOR_BEAN_NAME = "org.springframework.conf.ConfMonitorBean";

    public ConfMonitorBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, this.beanClass, true);
    }

    private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
        if(parserContext.getRegistry().containsBeanDefinition(BEAN_CONF_MONITOR_BEAN_NAME)){
            return null;
        }

        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");
        String name = element.getAttribute("name");
        String propertyPlaceholderConfigurers = element.getAttribute("propertyPlaceholderConfigurers");
        String listeners = element.getAttribute("listeners");
        log.info("propertyPlaceholderConfigurers = " + propertyPlaceholderConfigurers);

        if(id==null){
            id = BEAN_CONF_MONITOR_BEAN_NAME;
        }
        beanDefinition.getPropertyValues().add("id", id);
        beanDefinition.getPropertyValues().add("name", name);
        beanDefinition.getPropertyValues().add("beanName", BEAN_CONF_MONITOR_BEAN_NAME);
        if (parserContext.getRegistry().containsBeanDefinition(id)) {
            return beanDefinition;
        }
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);//向spring注册为bean
        if (propertyPlaceholderConfigurers == null || propertyPlaceholderConfigurers.trim().isEmpty()) {
            throw new IllegalArgumentException("propertyPlaceholderConfigurers cannot be empty");
        }
        String[] propertyPlaceholderConfigurersTemp = propertyPlaceholderConfigurers.split(",");
        if (propertyPlaceholderConfigurersTemp == null || propertyPlaceholderConfigurersTemp.length < 1) {
            throw new IllegalArgumentException("propertyPlaceholderConfigurers cannot be empty");
        }
        ManagedSet managedSet = new ManagedSet<RuntimeBeanReference>();
        for (String propertyPlaceholderConfigurer : propertyPlaceholderConfigurersTemp) {
            if (parserContext.getRegistry().containsBeanDefinition(propertyPlaceholderConfigurer)) {
                BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(propertyPlaceholderConfigurer);
                log.info("refBean=" + refBean);
            }
            managedSet.setMergeEnabled(true);
            managedSet.setElementTypeName(RuntimeBeanReference.class.getName());
            managedSet.add(new RuntimeBeanReference(propertyPlaceholderConfigurer));
        }
        beanDefinition.getPropertyValues().addPropertyValue(new PropertyValue("propertyPlaceholderConfigurers", managedSet));

        if (listeners != null) {
            listeners = listeners.trim();
            String[] listenerList = listeners.split(",");
            if (listenerList == null) {
                beanDefinition.getPropertyValues().addPropertyValue("listeners", new RuntimeBeanReference(listeners));
            } else {
                for (String lis : listenerList) {
                    if (lis != null && !lis.trim().isEmpty()) {
                        ManagedList<RuntimeBeanReference> managedList = new ManagedList<RuntimeBeanReference>();
                        managedList.setMergeEnabled(true);
                        managedList.add(new RuntimeBeanReference(lis.trim()));
                        PropertyValue propertyValue = new PropertyValue("listeners", managedList);
                        beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
                    }
                }
            }
        }
        return beanDefinition;
    }
}
