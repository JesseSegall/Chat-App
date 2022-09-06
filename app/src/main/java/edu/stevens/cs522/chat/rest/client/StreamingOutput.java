package edu.stevens.cs522.chat.rest.client;

import java.io.IOException;
import java.io.OutputStream;

/**
 * For SYNC: callback from request processor for streaming upload to server
 */
public interface StreamingOutput {
    void write(OutputStream os) throws IOException;
}
