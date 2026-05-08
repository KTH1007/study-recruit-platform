package com.study.platform.global.idempotency;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class IdempotencyResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream = new WrappedOutputStream(buffer);
    private final PrintWriter writer = new PrintWriter(outputStream);

    public IdempotencyResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        writer.flush();
        outputStream.flush();
        byte[] bytes = buffer.toByteArray();
        getResponse().getOutputStream().write(bytes);
    }

    public String getCapturedBody() {
        writer.flush();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static class WrappedOutputStream extends ServletOutputStream {

        private final ByteArrayOutputStream buffer;

        private WrappedOutputStream(ByteArrayOutputStream buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) {
            buffer.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            buffer.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {
        }
    }
}
