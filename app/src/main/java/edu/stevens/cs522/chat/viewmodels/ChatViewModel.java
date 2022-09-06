package edu.stevens.cs522.chat.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import edu.stevens.cs522.chat.databases.ChatDatabase;
import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.entities.Message;

public class ChatViewModel extends AndroidViewModel {

    public static final String TAG = ChatViewModel.class.getCanonicalName();

    private ChatDatabase chatDatabase;

    private Chatroom chatroom;

    private LiveData<List<Message>> messages;

    public ChatViewModel(Application context) {
        super(context);
        Log.i(TAG, "Getting database in ChatViewModel....");
        chatDatabase = ChatDatabase.getInstance(context);
    }

    public LiveData<List<Message>> fetchAllMessages(@NonNull Chatroom chatroom) {
        if (this.chatroom == null || (!chatroom.name.equals(this.chatroom.name))) {
            this.chatroom = chatroom;
            this.messages = loadMessages(chatroom);
        }
        return messages;
    }

    private LiveData<List<Message>> loadMessages(@NonNull  Chatroom chatroom) {
        return chatDatabase.messageDao().fetchAllMessages(chatroom.name);
    }

    @Override
    public void onCleared() {
        super.onCleared();
        Log.i(TAG, "Clearing ChatViewModel....");
        chatroom = null;
        chatDatabase = null;
    }
}
