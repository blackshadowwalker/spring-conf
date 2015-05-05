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
public class ClassPathResource implements Resource {

    private File file;
    private long lastModified=0;
    private URL url;
    private String classFilePath;

    public ClassPathResource(String classFilePath){
        try {
            if(!classFilePath.startsWith("/")) {
                this.classFilePath = "/" + classFilePath;
            }else{
                this.classFilePath = classFilePath;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void refresh(){
        try {
            URL url = ClassPathResource.class.getResource(this.classFilePath);
            if(url==null)
                url = ClassPathResource.class.getClassLoader().getResource(this.classFilePath);
            if(url==null)
                url = this.getClass().getResource(this.classFilePath);
            if(url!=null) {
                URI uri = url.toURI();
                this.url = uri.toURL();
                this.file = new File(uri);
                this.lastModified();
             //   System.out.println("exists " + this.file.exists() + "  " + this.url);
                if(!this.file.exists()) {
                    this.file = null;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isModified() {
        long last = this.lastModified;
        lastModified = this.lastModified();
        return (last != this.lastModified);
    }

    @Override
    public long lastModified() {
        if(this.file == null){
            refresh();
        }
        if(this.file !=null) {
            lastModified = this.file.lastModified();
        }
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
