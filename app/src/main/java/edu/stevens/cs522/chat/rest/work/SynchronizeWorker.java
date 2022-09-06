package edu.stevens.cs522.chat.rest.work;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker.Result;

import edu.stevens.cs522.base.work.Worker;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import edu.stevens.cs522.chat.rest.request.ErrorResponse;
import edu.stevens.cs522.chat.rest.request.SynchronizeRequest;

public class SynchronizeWorker extends Worker {

    private static final String TAG = SynchronizeWorker.class.getCanonicalName();

    public SynchronizeWorker(@NonNull Context context, @NonNull Bundle data) {
        super(context, data);
    }

    @Override
    public boolean doWork() {
        SynchronizeRequest synchronizeRequest = new SynchronizeRequest();

        RequestProcessor processor = RequestProcessor.getInstance(context);

        ChatServiceResponse response = processor.process(synchronizeRequest);

        if (response instanceof ErrorResponse) {
            Log.i(TAG, "Failed to sync chat messages, will retry: "+response.httpResponseMessage);
            return false;
        }

        return true;
    }
}
