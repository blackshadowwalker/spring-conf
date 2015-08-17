# spring-conf
==============

Author: Karl

Email: BlackShadowWalker@163.com


##### confMonitor


Monitor property files the spring PropertyPlaceholderConfigurer configured and  notify listeners.

The default listener {@link org.springframework.conf.listener.DefaultConfChangedListener} will refresh "spring WebApplicationContexts" when listened file updated.

##### Usage:

    please see the test sources.
    
    web.xml add SpringConfFilter(http response 503 when refreshing spring) 
    
````xml
<filter>
   <filter-name>springConfFilter</filter-name>
   <filter-class>com.gozap.ezhe.filter.SpringConfFilter</filter-class>
</filter>
<filter-mapping>
   <filter-name>springConfFilter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
````

SpringConfFilter.java

````java
import org.springframework.conf.listener.PropertyWatchdog;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class SpringConfFilter implements Filter {

    private FilterConfig filterConfig;
    private WebApplicationContext ctx;
    private volatile PropertyWatchdog watchdog;
    private boolean isRestarting = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        this.ctx = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        watchdog = this.ctx.getBean(PropertyWatchdog.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        if(watchdog==null){
            watchdog = this.ctx.getBean(PropertyWatchdog.class);
        }
        if(watchdog==null || watchdog.isRefreshing()){
            isRestarting = true;
            response.sendError(503, "["+new Date()+"] service restarting...");
            return ;
        }else if(isRestarting){
            watchdog = null;
            isRestarting = false;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}

````
