package edu.stevens.cs522.chat.rest.work;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker.Result;

import edu.stevens.cs522.base.work.Worker;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import edu.stevens.cs522.chat.rest.request.ErrorResponse;
import edu.stevens.cs522.chat.rest.request.PostMessageRequest;

public class PostMessageWorker extends Worker {

    private static final String TAG = PostMessageWorker.class.getCanonicalName();

    public static final String MESSAGE_KEY = "message";

    public static final String RESULT_RECEIVER_KEY = "receiver";

    private final Message message;

    private final ResultReceiver receiver;

    public PostMessageWorker(@NonNull Context context, @NonNull Bundle data) {
        super(context, data);

        message = data.getParcelable(MESSAGE_KEY);
        if (message == null) {
            throw new IllegalStateException("Missing message for post message worker!");
        }

        receiver = data.getParcelable(RESULT_RECEIVER_KEY);
    }

    @Override
    public boolean doWork() {

        PostMessageRequest postMessageRequest = new PostMessageRequest(message);

        RequestProcessor processor = RequestProcessor.getInstance(context);

        ChatServiceResponse response = processor.process(postMessageRequest);

        if (receiver != null) {
            // Use receiver to call back to UI

            if (response instanceof ErrorResponse) {
                Log.d(TAG, String.format("Failed to send message ('%s'): %s", message.messageText, response.httpResponseMessage));
                // TODO let activity know request failed

                receiver.send(Activity.RESULT_CANCELED, null);
            } else {
                Log.d(TAG, String.format("Message sent ('%s')!", message.messageText));
                // TODO let activity know request succeeded

                receiver.send(Activity.RESULT_OK, null);
            }
        } else {
            Log.d(TAG, "Missing receiver");
        }

        if (response instanceof ErrorResponse) {
            Log.i(TAG, "Failed to upload chat message: "+response.httpResponseMessage);
            return false;
        }

        return true;
    }
}
