package com.berec.speedtest;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.SSLException;

import static com.berec.speedtest.ApiConfig.UPLOAD_URL;

public class UploadManager extends Thread {
    private final CloseableHttpClient httpClient;
    private final HttpContext httpContext;
    private final HttpPut httpput;
    private CloseableHttpResponse response = null;
    private final CountDownLatch latch;
    private File f;
    private Context context;
    private ProgressListener listener;
    private String responseString;


    public UploadManager(CloseableHttpClient httpClient, Context context, File file, CountDownLatch latch, ProgressListener listener){
        this.httpClient = httpClient;
        this.httpContext = HttpClientContext.create();
        this.httpput = new HttpPut(UPLOAD_URL);
        this.context = context;
        this.f = file;
        this.latch = latch;
        this.listener = listener;
    }

    @Override
    public void run() {

        try {
            if (f.isFile()){
                MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();
                multipartEntity.addBinaryBody(f.getName(), f, ContentType.APPLICATION_OCTET_STREAM, f.getName());
                HttpEntity multipart = multipartEntity.build();

                httpput.setEntity(new ProgressEntityWrapper(multipart, listener, f));

                response = httpClient.execute(httpput, httpContext);
                HttpEntity responseEntity = response.getEntity();
                responseString = EntityUtils.toString(responseEntity, "UTF-8");

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    // each thread will decrement the CountDownLatch, which has been passed in constructor
                    latch.countDown();
                }
            }
        } catch (ClientProtocolException e) {
            // Handle protocol errors
            e.printStackTrace();
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // Handle I/O errors
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (response != null){
                Log.i("response", responseString);
                try{
                    response.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }
}
