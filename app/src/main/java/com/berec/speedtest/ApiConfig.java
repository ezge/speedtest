package com.berec.speedtest;

public class ApiConfig {

    // The base uris for download files from server
    public static final String[] DOWNLOAD_URLS = {
            "https://s3.dualstack.eu-central-1.amazonaws.com/ncdefloq19dl/get/1gb1.xwd",
            "https://s3.dualstack.eu-central-1.amazonaws.com/ncdefloq19dl/get/1gb2.xwd",
            "https://s3.dualstack.eu-central-1.amazonaws.com/ncdefloq19dl/get/1gb3.xwd"
    };

    // The base uri for upload files to server
    public static final String UPLOAD_URL = "https://s3.dualstack.eu-central-1.amazonaws.com/ncdefloq19ul/upload/";

    public static final int CONNECTION_TIMEOUT = 120000;
    public static final int SO_TIMEOUT = 120000 + 30000;
    public static final int MAX_CONNECTIONS = 500;
    public static final int MAX_CONNECTIONS_PER_ROUTE = 100;
    public static final int IDLE_TIMEOUT = 30000;

    public static final int MAX_BUFFER_SIZE = 4096;

    public static final long MAX_SLOW_SPEED = 3000;
    public static final long DURATION = 10000;

    public static final int MAX_PROGRESSBAR_SIZE = 165;

    public static final int Bits = 8;
    public static final int SECONDS = 1000;
    public static final int MB = 1000000;
    public static final long MAX_KB = 10000;

    public static final String FIRST_ACTION = "1gb1.xwd";
    public static final String SECOND_ACTION = "1gb2.xwd";
    public static final String THIRD_ACTION = "1gb3.xwd";

    public static final String BOUNDARY = "------------------------80b26201cf2ec6a2";
}
