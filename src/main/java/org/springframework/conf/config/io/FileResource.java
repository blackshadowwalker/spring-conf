package org.springframework.conf.config.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/22
 * Time: 11:13
 * Description:
 */
public class FileResource implements Resource {

    private File file;
    private long lastModified=0;
    private URL url;

    //start with 'file:///'  such as file:///E:/karl/war/ezhe/env/ezhe.properties
    public FileResource(String url){
        try {
            this.url = new URL(url);
            this.refresh();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void refresh(){
        try {
            if(this.url!=null) {
                this.file = new File(this.url.toURI());
                this.lastModified();
              //  System.out.println("exists " + this.file.exists() + "  " + url);
                if(!this.file.exists())
                    this.file = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isModified() {
        if(this.file==null){
            this.refresh();
        }
        if(this.file!=null) {
            long last = this.lastModified;
            lastModified = this.file.lastModified();
            return (last != this.lastModified);
        }
        return false;
    }

    @Override
    public long lastModified() {
        lastModified = this.file.lastModified();
        return lastModified;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public URL getURL() throws IOException {
        return null;
    }

    @Override
    public URI getURI() throws IOException {
        return null;
    }

    @Override
    public File getFile() throws IOException {
        return this.file;
    }

    @Override
    public long contentLength() throws IOException {
        return 0;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return null;
    }

    @Override
    public String getFilename() {
        if(this.file!=null)
            return this.file.getAbsolutePath();
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }
}
