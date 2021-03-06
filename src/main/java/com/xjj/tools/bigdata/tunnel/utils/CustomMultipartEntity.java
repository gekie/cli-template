package com.xjj.tools.bigdata.tunnel.utils;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

public class CustomMultipartEntity extends MultipartEntity implements Serializable {
    private ProgressListener listener;
    public CustomMultipartEntity(){
        super(HttpMultipartMode.BROWSER_COMPATIBLE,
                null, Charset.forName("UTF-8"));
    }
    public CustomMultipartEntity(final ProgressListener listener) {
        super();
        this.listener = listener;
    }

    public CustomMultipartEntity(final HttpMultipartMode mode,
                                 final ProgressListener listener) {
        super(mode);
        this.listener = listener;
    }

    public CustomMultipartEntity(HttpMultipartMode mode, final String boundary,
                                 final Charset charset, final ProgressListener listener) {
        super(mode, boundary, charset);
        this.listener = listener;
    }

    public void setListener( final ProgressListener listener){
        this.listener = listener;
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        super.writeTo(new CountingOutputStream(outstream, this.listener));
    }

    public interface ProgressListener {
        void transferred(long num);
    }

    public static class CountingOutputStream extends FilterOutputStream {

        private final ProgressListener listener;
        private long transferred;

        public CountingOutputStream(final OutputStream out,
                                    final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            out.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }
    }
}
