package org.springframework.conf.config;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.conf.listener.FileChangedListener;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 21:04
 * Description:
 */
public class ConfMonitorConfig {

    private String id;
    private String name;
    private String version;
    private long pollingTime = 5000;
    private List<Resource> files;
    private Set<PropertyPlaceholderConfigurer> propertyPlaceholderConfigurers;
    private List<FileChangedListener> listeners;

    public void setListeners(List<FileChangedListener> listeners) {
        this.listeners = listeners;
    }

    public List<FileChangedListener> getListeners() {
        return listeners;
    }

    public void init() {
        try {
            // Field locationsField = propertyPlaceholderConfigurer.getClass().getDeclaredField(locations);
            //  System.out.println("propertyPlaceholderConfigurer="+ locationsField.get(propertyPlaceholderConfigurer) );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(long pollingTime) {
        this.pollingTime = pollingTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Resource> getFiles() {
        return files;
    }

    public void setFiles(List<Resource> files) {
        this.files = files;
    }

    public Set<PropertyPlaceholderConfigurer> getPropertyPlaceholderConfigurers() {
        return propertyPlaceholderConfigurers;
    }

    public void setPropertyPlaceholderConfigurers(Set<PropertyPlaceholderConfigurer> propertyPlaceholderConfigurers) {
        this.propertyPlaceholderConfigurers = propertyPlaceholderConfigurers;
    }
}
