package org.springframework.conf.listener;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/21
 * Time: 15:52
 * Description:
 */
public interface ConfChangedListener {

    public void setName(String name);
    public void fileChanged(final URL url);

}
