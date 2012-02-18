package esl.cuenet.query.drivers.webjson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HttpDownloader {

    private Logger logger = Logger.getLogger(HttpDownloader.class);

    public byte[] get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        HttpResponse response;
        HttpClient client = new DefaultHttpClient();
        response = client.execute(get);

        HttpEntity entity = response.getEntity();

        byte[] bytes = EntityUtils.toByteArray(entity);
        if (bytes != null) logger.info("Get (" + url + ") response size: " + bytes.length);
        return bytes;
    }

    public byte[] post(String url, String jsonString) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        StringEntity se = new StringEntity(jsonString);
        post.setEntity(se);

        HttpResponse response;
        byte[] bytes;

        HttpClient client = new DefaultHttpClient();

        response = client.execute(post);
        HttpEntity entity = response.getEntity();

        bytes = EntityUtils.toByteArray(entity);
        if (bytes != null) logger.info("Post (" + url + ") response size: " + bytes.length);

        return bytes;
    }
}
