package org.springframework.conf.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.conf.config.ConfMonitorBean;

import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 17:38
 * Description:
 */
public class ConfMonitorNamespaceHandler extends NamespaceHandlerSupport {
    private Logger log = Logger.getLogger("ConfMonitorNamespaceHandler");
    @Override
    public void init() {
        log.info("ConfMonitorNamespaceHandler init");
        registerBeanDefinitionParser("application", new ConfMonitorBeanDefinitionParser(ConfMonitorBean.class));
    }
}
