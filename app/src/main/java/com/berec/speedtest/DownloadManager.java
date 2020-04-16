package com.berec.speedtest;

import android.content.Context;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static com.berec.speedtest.ApiConfig.DURATION;
import static com.berec.speedtest.ApiConfig.MAX_BUFFER_SIZE;
import static com.berec.speedtest.ApiConfig.MAX_SLOW_SPEED;


public class DownloadManager extends Thread {

    private final CloseableHttpClient httpClient;
    private final HttpContext httpContext;
    private final HttpGet httpget;
    private Context context;
    private CloseableHttpResponse response = null;
    private String url;
    private final CountDownLatch latch;
    private ProgressListener listener;
    private double transferred;
    private long startTime, endTime;

    public DownloadManager(CloseableHttpClient httpClient, Context context, String url, CountDownLatch latch, ProgressListener listener) {
        this.httpClient = httpClient;
        this.httpContext = HttpClientContext.create();
        this.httpget = new HttpGet(url);
        this.url = url;
        this.context = context;
        this.latch = latch;
        this.listener = listener;
        this.transferred = 0;
    }

    @Override
    public void run() {

        try {
            // get response from server
            response = httpClient.execute(httpget, httpContext);

            if (response.getStatusLine().getStatusCode() == 200) {
                FileOutputStream outputStream = null;
                InputStream inputStream = null;

                    try {
                        String filename = FileCache.fileName(url);
                        inputStream = response.getEntity().getContent();
                        outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                        byte[] buffer = new byte[MAX_BUFFER_SIZE];

                        startTime = System.currentTimeMillis();
                        endTime = System.currentTimeMillis() + MAX_SLOW_SPEED;


                        while (startTime <= endTime){
                            if (inputStream.read(buffer) != -1)
                                outputStream.write(buffer);
                            startTime = System.currentTimeMillis();
                        }

                        startTime = System.currentTimeMillis();
                        endTime = System.currentTimeMillis();

                        while (endTime <= startTime + DURATION){
                            int read = 0;
                            if ((read = inputStream.read(buffer))!= -1) {
                                outputStream.write(buffer, 0, read);
                                transferred += read;
                                listener.progress(FileCache.getCurrentProgress(transferred, startTime, endTime), filename);
                            }
                            endTime = System.currentTimeMillis();
                        }

                        outputStream.flush();

                        // each thread will decrement the CountDownLatch, which has been passed in constructor
                        latch.countDown();

                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        if (outputStream != null)
                            outputStream.close();
                        if (response != null)
                            response.close();
                    }
            }
        } catch (ClientProtocolException e) {
            // Handle protocol errors
            e.printStackTrace();
        } catch (IOException e) {
            // Handle I/O errors
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

