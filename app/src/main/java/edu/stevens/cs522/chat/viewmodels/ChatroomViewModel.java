package edu.stevens.cs522.chat.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import edu.stevens.cs522.chat.databases.ChatDatabase;
import edu.stevens.cs522.chat.entities.Chatroom;

public class ChatroomViewModel extends AndroidViewModel {

    public static final String TAG = ChatroomViewModel.class.getCanonicalName();

    private ChatDatabase chatDatabase;

    private LiveData<List<Chatroom>> chatrooms;

    public ChatroomViewModel(Application context) {
        super(context);
        Log.i(TAG, "Getting database in ChatViewModel....");
        chatDatabase = ChatDatabase.getInstance(context);
    }

    public LiveData<List<Chatroom>> fetchAllChatrooms() {
        if (chatrooms == null) {
            chatrooms = loadChatrooms();
        }
        return chatrooms;
    }

    private LiveData<List<Chatroom>> loadChatrooms() {
        return chatDatabase.chatroomDao().fetchAllChatrooms();
    }

    @Override
    public void onCleared() {
        super.onCleared();
        Log.i(TAG, "Clearing ChatViewModel....");
        chatDatabase = null;
    }
}
