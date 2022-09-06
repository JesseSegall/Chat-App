package edu.stevens.cs522.chat.rest.client;

import java.io.IOException;
import java.util.Map;

import edu.stevens.cs522.chat.rest.request.ChatServiceRequest;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/*
 * This interceptor adds app-specific headers to every message sent to HTTP server.
 */
public class HeaderInterceptor implements Interceptor {

    protected Map<String,String> headers;

    public HeaderInterceptor(Map<String,String> headers) {
        this.headers = headers;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder httpRequestBuilder = chain.request().newBuilder();
        for (Map.Entry<String,String> header : headers.entrySet()) {
            httpRequestBuilder.addHeader(header.getKey(), header.getValue());
        }
        return chain.proceed(httpRequestBuilder.build());
    }
}
