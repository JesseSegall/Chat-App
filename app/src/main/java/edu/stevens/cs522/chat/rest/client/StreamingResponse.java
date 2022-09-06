package edu.stevens.cs522.chat.rest.client;

import java.io.InputStream;

import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import okhttp3.ResponseBody;

/**
 * For SYNC: This is the response returned from a streaming download.  The request processor
 * will read the streaming input (from the server) on the download stream, with HTTP response
 * headers in the response field.  It is the responsibility of the request processor to
 * close the input stream when done.
 */
public class StreamingResponse {
    private final ChatServiceResponse response;
    private final ResponseBody responseBody;

    public StreamingResponse(ChatServiceResponse response, ResponseBody responseBody) {
        this.response = response;
        this.responseBody = responseBody;
    }

    public InputStream getInputStream() {
        return responseBody.byteStream();
    }

    public ChatServiceResponse getResponse() {
        return response;
    }

    public void disconnect() {
        if (responseBody != null) {
            responseBody.close();
        }
    }
}
