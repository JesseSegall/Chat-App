package edu.stevens.cs522.chat.rest.client;

import java.io.OutputStream;

import edu.stevens.cs522.chat.entities.Message;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/*
 * The API for the chat server.
 *
 * TODO annotate the methods with HTTP operations and context paths
 */
public interface ServerApi {

    String CHAT_NAME = "chat-name";

    String LAST_SEQ_NUM = "last-seq-num";

    @POST("chat")
    Call<Void> register(@Query(CHAT_NAME) String chatName);

    @POST("chat/{chat-name}/messages")
    Call<Void> postMessage(@Path(CHAT_NAME) String chatName, @Body Message chatMessage);


    @POST("chat/{chat-name}/sync")
    public Call<ResponseBody> syncMessages(@Path(CHAT_NAME) String chatName,
                                           @Query(LAST_SEQ_NUM) long lastSeqNum,
                                           @Body RequestBody requestBody);

}