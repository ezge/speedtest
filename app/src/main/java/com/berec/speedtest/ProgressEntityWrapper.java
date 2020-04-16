package com.berec.speedtest;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.berec.speedtest.ApiConfig.BOUNDARY;
import static com.berec.speedtest.ApiConfig.DURATION;
import static com.berec.speedtest.ApiConfig.MAX_BUFFER_SIZE;
import static com.berec.speedtest.ApiConfig.MAX_SLOW_SPEED;


public class ProgressEntityWrapper extends HttpEntityWrapper{
    private ProgressListener listener;
    private File file;
    private long endTime, startTime;
    private double transferred;
    private ByteArrayOutputStream out;
    private boolean isSetLast = false;
    private boolean isSetFirst = false;
    private InputStream inputStream = null;


    public ProgressEntityWrapper(HttpEntity entity, ProgressListener listener, File file) {
        super(entity);
        this.listener = listener;
        this.file = file;
        out = new ByteArrayOutputStream();
        this.transferred = 0;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {

        inputStream = new FileInputStream(file);

        writeFirstBoundaryIfNeeds();
        try {
            final String type = "Content-Type: " + "application/octet-stream" + "\r\n";
            out.write(("Content-Disposition: form-data; name=\"" + "upload_file" + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
            out.write(type.getBytes());
            out.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());

            byte[] buffer = new byte[MAX_BUFFER_SIZE];

            startTime = System.currentTimeMillis();
            endTime = System.currentTimeMillis() + MAX_SLOW_SPEED;


            while (startTime <= endTime){
                if (inputStream.read(buffer) != -1)
                    out.write(buffer);
                outstream.write(out.toByteArray());
                startTime = System.currentTimeMillis();
            }

            startTime = System.currentTimeMillis();
            endTime = System.currentTimeMillis();

            while (endTime <= startTime + DURATION){
                int read = 0;
                if ((read = inputStream.read(buffer))!= -1) {
                    out.write(buffer, 0, read);
                    transferred += read;
                    listener.progress(FileCache.getCurrentProgress(transferred, startTime, endTime), file.getName());
                }
                outstream.write(out.toByteArray());
                endTime = System.currentTimeMillis();
            }

            out.flush();

        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeFirstBoundaryIfNeeds() {
        if (!isSetFirst) {
            try {
                out.write(("--" + BOUNDARY + "\r\n").getBytes());
            } catch (final IOException e) {

            }
        }
        isSetFirst = true;
    }

    private void writeLastBoundaryIfNeeds() {
        if (isSetLast) {
            return;
        }
        try {
            out.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());
        } catch (final IOException e) {

        }
        isSetLast = true;
    }

    public Header getContentType() {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
    }

    public long getContentLength() {
        writeLastBoundaryIfNeeds();
        return out.toByteArray().length;
    }

    public Header getContentEncoding() {
        return null;
    }

    public void consumeContent() throws UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
        }
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isChunked() {
        return false;
    }

    public boolean isStreaming() { return false; }
}

