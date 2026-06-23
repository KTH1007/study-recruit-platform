package com.study.platform.global.idempotency

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

class IdempotencyResponseWrapper(response: HttpServletResponse) : HttpServletResponseWrapper(response) {

    private val buffer = ByteArrayOutputStream()
    private val outputStream: ServletOutputStream = WrappedOutputStream(buffer)
    private val writer: PrintWriter = PrintWriter(outputStream)

    override fun getOutputStream(): ServletOutputStream = outputStream

    override fun getWriter(): PrintWriter = writer

    @Throws(IOException::class)
    override fun flushBuffer() {
        writer.flush()
        outputStream.flush()
        val bytes = buffer.toByteArray()
        response.outputStream.write(bytes)
    }

    val capturedBody: String
        get() {
            writer.flush()
            return buffer.toString(StandardCharsets.UTF_8)
        }

    private class WrappedOutputStream(private val buffer: ByteArrayOutputStream) : ServletOutputStream() {

        override fun write(b: Int) {
            buffer.write(b)
        }

        @Throws(IOException::class)
        override fun write(b: ByteArray) {
            buffer.write(b)
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            buffer.write(b, off, len)
        }

        override fun isReady(): Boolean = true

        override fun setWriteListener(listener: WriteListener) {}
    }
}
