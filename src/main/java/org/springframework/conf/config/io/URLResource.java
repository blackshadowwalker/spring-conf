package org.springframework.conf.config.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: Karl
 * Email: BlackShadowWalker@163.com
 * Date: 2015/4/22
 * Time: 11:10
 * Description:
 */
public class URLResource implements Resource {

    private URL url;
    private URI uri;
    private File file;
    private long lastModified;
    private String ETag;

    public URLResource(String url) {
        try {
            this.url = new URL(url);
            this.uri = this.url.toURI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isModified() {
        long last = this.lastModified;
        String tag = this.ETag;
        this.lastModified();
        boolean isModified = (last != this.lastModified || (tag != null && !tag.equals(this.ETag)));
        if (isModified) {
            System.out.println("modified from " + last + "->" + this.lastModified + "  " + tag + "->" + this.ETag);
        }
        return isModified;
    }

    private long lastPrintError = 0;
    private long maxIner = 1000 * 60 * 60;

    @Override
    public long lastModified() {
        URLConnection con = null;
        try {
            con = this.url.openConnection();
            con.setConnectTimeout(30);
            con.setReadTimeout(30);
            con.connect();
            long last = con.getLastModified();
            if (last > this.lastModified)
                this.lastModified = con.getLastModified();
            String temp = con.getHeaderField("ETag");
            if (temp != null) {
                ETag = temp.substring(temp.indexOf("\"") + 1, temp.lastIndexOf("\""));
            }
        } catch (java.net.SocketTimeoutException se) {
        } catch (java.net.ConnectException ce) {
        } catch (Exception e) {
            if (System.currentTimeMillis() - lastPrintError > maxIner) {
                lastPrintError = System.currentTimeMillis();
                System.err.println(e.getLocalizedMessage());
            }
        } finally {
            if (con != null && con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
        }
        return this.lastModified;
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
        return this.url;
    }

    @Override
    public URI getURI() throws IOException {
        return this.uri;
    }

    @Override
    public File getFile() throws IOException {
        if (this.file == null) {
            String tempDir = System.getProperty("java.io.tmpdir");
            //download
            this.file = new File(tempDir + "/" + System.currentTimeMillis() + ".tmp");
        }
        return null;
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
        return this.url.toString();
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
