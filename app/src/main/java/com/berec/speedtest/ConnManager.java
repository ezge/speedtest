package com.berec.speedtest;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.berec.speedtest.ApiConfig.CONNECTION_TIMEOUT;
import static com.berec.speedtest.ApiConfig.DOWNLOAD_URLS;
import static com.berec.speedtest.ApiConfig.FIRST_ACTION;
import static com.berec.speedtest.ApiConfig.IDLE_TIMEOUT;
import static com.berec.speedtest.ApiConfig.MAX_CONNECTIONS;
import static com.berec.speedtest.ApiConfig.MAX_CONNECTIONS_PER_ROUTE;
import static com.berec.speedtest.ApiConfig.SECOND_ACTION;
import static com.berec.speedtest.ApiConfig.SO_TIMEOUT;
import static com.berec.speedtest.ApiConfig.THIRD_ACTION;

public class ConnManager {

    private Context context;
    private final CountDownLatch latch;


    public ConnManager(Context context){
        this.context = context;
        this.latch = new CountDownLatch(DOWNLOAD_URLS.length);
    }

    /** GET request **/
    public void startDownload(){

        DownloadManager[] threads = new DownloadManager[DOWNLOAD_URLS.length];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new DownloadManager(httpClient, context, DOWNLOAD_URLS[i], latch, listener);
        }


        for (int j = 0; j < threads.length; j++) {
            threads[j].start();
        }

        // main thread waits till the count has become zero
        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** PUT request **/
    public void startUpload(){
        FileCache fileCache = new FileCache(context);
        File dir = fileCache.cacheDir;
        File[] files = fileCache.cacheDir.listFiles();

        final UploadManager[] threads_upload = new UploadManager[files.length];
        int i = 0;

        for (File f : files){
            threads_upload[i++] = new UploadManager(httpClient, context, f, latch, listener);
        }


        for (int j = 0; j < files.length; j++) {
            threads_upload[j].start();
        }

        // main thread waits till the count has become zero
        try {
            latch.await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private final static CloseableHttpClient httpClient = getHttpClient();

    private final static CloseableHttpClient getHttpClient() {

        return HttpClients.custom()
                .useSystemProperties()
                .setConnectionManager(getConnManager())
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    private final static RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(SO_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setConnectionRequestTimeout(CONNECTION_TIMEOUT)
            .setRedirectsEnabled(true)
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();

    private static PoolingHttpClientConnectionManager getConnManager(){

        Registry<ConnectionSocketFactory> socketFactoryRegistry = null;
        try {
            SSLConnectionSocketFactory trustSelfSignedSocketFactory = new SSLConnectionSocketFactory(
                        new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build(),
                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            socketFactoryRegistry = RegistryBuilder
                        .<ConnectionSocketFactory> create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", trustSelfSignedSocketFactory)
                        .build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                e.printStackTrace();
        }

        PoolingHttpClientConnectionManager pool = (socketFactoryRegistry != null) ?
                new PoolingHttpClientConnectionManager(socketFactoryRegistry) :
                new PoolingHttpClientConnectionManager();

        pool.setMaxTotal (MAX_CONNECTIONS);
        pool.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        pool.closeExpiredConnections();
        pool.closeIdleConnections(IDLE_TIMEOUT, TimeUnit.MILLISECONDS);
        pool.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(SO_TIMEOUT)
                                                         .setSoKeepAlive(true)
                                                         .setSoReuseAddress(true)
                                                         .build());

        return pool;
    }

    public void close() throws IOException {
        httpClient.close();

    }

    public void shutdownHttpClient()
    {
        if (httpClient != null && httpClient.getConnectionManager() != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }

    ProgressListener listener = new ProgressListener() {
        @Override
        public void progress(double speed, String filename) {

            Intent intent = new Intent();

            if (speed > 0) {
                switch (filename) {
                    case FIRST_ACTION:
                        intent.setAction(filename);
                        break;
                    case SECOND_ACTION:
                        intent.setAction(filename);
                        break;
                    case THIRD_ACTION:
                        intent.setAction(filename);
                        break;
                }
                intent.putExtra(filename, speed);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }
    };
}
