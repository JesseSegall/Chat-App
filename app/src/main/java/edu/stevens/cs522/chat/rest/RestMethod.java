package edu.stevens.cs522.chat.rest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.rest.client.ExcludeStrategy;
import edu.stevens.cs522.chat.rest.client.HeaderInterceptor;
import edu.stevens.cs522.chat.rest.client.ServerApi;
import edu.stevens.cs522.chat.rest.client.StreamingOutput;
import edu.stevens.cs522.chat.rest.client.StreamingResponse;
import edu.stevens.cs522.chat.rest.client.TimestampSerializer;
import edu.stevens.cs522.chat.rest.client.UUIDSerializer;
import edu.stevens.cs522.chat.rest.request.ErrorResponse;
import edu.stevens.cs522.chat.rest.request.PostMessageRequest;
import edu.stevens.cs522.chat.rest.request.RegisterRequest;
import edu.stevens.cs522.chat.rest.request.ChatServiceRequest;
import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import edu.stevens.cs522.chat.rest.request.SynchronizeRequest;
import edu.stevens.cs522.chat.settings.Settings;
import edu.stevens.cs522.base.StringUtils;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dduggan.
 */

public class RestMethod {

    private static final String TAG = RestMethod.class.getCanonicalName();

    private static final boolean DEBUG = true;

    public static final String CHARSET = "UTF-8";



    /*
     * HTTP Request headers
     */
    public final static String CONTENT_TYPE = "CONTENT-TYPE";

    public final static String ACCEPT = "ACCEPT";

    public final static String USER_AGENT = "USER-AGENT";

    public final static String CONNECTION = "CONNECTION";

    /*
     * MIME types
     */
    public final static String JSON_TYPE = "application/json";

    /*
     * Timeouts
     */
    public final static int SERVICE_DURATION = 5000;


    /*
     * HTTP response
     */
    public static final int HTTP_RESPONSE_CODE_UNKNOWN = 400;

    public static final int HTTP_RESPONSE_STRING_UNKNOWN = R.string.http_response_unknown;

    public static final int HTTP_RESPONSE_CODE_UNAVAILABLE = 503;

    public static final int HTTP_RESPONSE_STRING_UNAVAILABLE = R.string.http_response_unavailable;

    /*
     * JSON labels
     */
    public static final String PEERS = "peers";

    public static final String CHATROOMS = "chatrooms";

    public static final String MESSAGES = "messages";

    private final Context context;

    private final Gson gson;


    public RestMethod(Context context) {
        this.context = context;
        /*
         * Create the GSON parser/unparser.
         */
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new TimestampSerializer())
                   .registerTypeAdapter(UUID.class, new UUIDSerializer())
                   .setExclusionStrategies(new ExcludeStrategy());
        this.gson = gsonBuilder.create();
    }

    public Gson getGson() {
        return gson;
    }

    /*
     * Create a retrofit client stub around an OkHttp client.
     */
    protected ServerApi createClient(Uri serverUri, ChatServiceRequest request) {
        /*
         * Create the HTTP client stub.
         */
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Map<String,String> headers = request.getRequestHeaders();
        headers.put(USER_AGENT, buildUserAgent(context));
        Interceptor interceptor = new HeaderInterceptor(headers);
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();

        /*
         * TODO Wrap the okhttp client with a retrofit stub factory.
         *
         */

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUri.toString())
                .addConverterFactory(GsonConverterFactory.create(this.gson))
                .client(client)
                .build();

        return retrofit.create(ServerApi.class);
    }


    public ChatServiceResponse perform(RegisterRequest request) {
        try {
            Log.d(TAG, "Performing REST method for registration....");
            ServerApi server = createClient(request.chatServer, request);
            Response<Void> response;
            // TODO execute the Web service call
            Call<Void> servCall = server.register(request.chatname);
            response = servCall.execute();
            Log.d(TAG, "Response has gone through" +response);


            return request.getResponse(response);
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "Socket timeout.", e);
            return isUnavailable(request);
        } catch (IOException e) {
            Log.e(TAG, "Registration: Web service error.", e);
            return new ErrorResponse(0, ErrorResponse.Status.SYSTEM_ERROR, e.getMessage());
        }
    }

    public ChatServiceResponse perform(PostMessageRequest request) {
        try {
            ServerApi server = createClient(Objects.requireNonNull(Settings.getServerUri(context)), request);
            Log.d(TAG, String.format("Sending \"%s\" to %s", request.message.messageText, request.message.chatroom));

            Response<Void> response = null;
            // TODO execute the Web service call
            Call<Void> servCall = server.postMessage(request.message.sender, request.message);
            response = servCall.execute();


            return request.getResponse(response);
        } catch (SocketTimeoutException e) {
            return isUnavailable(request);
        } catch (IOException e) {
            Log.e(TAG, "Post message: Web service error.", e);
            return new ErrorResponse(0, ErrorResponse.Status.SYSTEM_ERROR, e.getMessage());
        }
    }

    protected MediaType jsonType = MediaType.get("application/json");

    public StreamingResponse perform(SynchronizeRequest request, final StreamingOutput out) throws IOException {

        ServerApi server = createClient(Objects.requireNonNull(Settings.getServerUri(context)), request);

        String chatName = Settings.getChatName(context);

        /*
         * We will stream the output JSON data in this request body.
         */
        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return jsonType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                try (OutputStream os = sink.outputStream()) {
                    out.write(os);
                }
            }
        };

        Response<ResponseBody> callResponse = null;

        ChatServiceResponse response = null;

        // TODO execute the Web service call
        Call<ResponseBody> servCall = server.syncMessages(chatName, request.lastSequenceNumber, requestBody);
        callResponse = servCall.execute();
        response = request.getResponse(callResponse);

        // end TODO

        /*
         * If the connection was successful, the request processor will process the streaming input JSON data.
         */
        ResponseBody responseBody = response.isValid() ? callResponse.body() : callResponse.errorBody();

        return new StreamingResponse(response, responseBody);
    }


    /**
     * Build and return a user-agent string that can identify this application to remote servers. Contains the package
     * name and version code.
     */
    private static String buildUserAgent(Context context) {
        String versionName = "unknown";
        int versionCode = 0;

        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return context.getPackageName() + "/" + versionName + " (" + versionCode + ") (gzip)";
    }

    private ErrorResponse isUnavailable(ChatServiceRequest request) {
        return new ErrorResponse(
                HTTP_RESPONSE_CODE_UNAVAILABLE,
                ErrorResponse.Status.NETWORK_UNAVAILABLE,
                context.getString(R.string.http_response_unavailable),
                context.getString(HTTP_RESPONSE_STRING_UNAVAILABLE));
    }

}
