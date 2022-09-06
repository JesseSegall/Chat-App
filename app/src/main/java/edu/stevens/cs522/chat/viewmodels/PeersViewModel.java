package edu.stevens.cs522.chat.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import edu.stevens.cs522.chat.databases.ChatDatabase;
import edu.stevens.cs522.chat.entities.Peer;

public class PeersViewModel extends AndroidViewModel {

    private static final String TAG = PeersViewModel.class.getCanonicalName();

    private ChatDatabase chatDatabase;

    private LiveData<List<Peer>> peers;

    public PeersViewModel(Application context) {
        super(context);
        Log.d(TAG, "Getting database in PeerViewModel");
        chatDatabase = ChatDatabase.getInstance(context);
    }

    // TODO finish this
    public LiveData<List<Peer>> fetchAllPeers() {
        if (peers == null) {
            peers = loadPeers();
        }
        return peers;
    }


    private LiveData<List<Peer>> loadPeers() {
        return chatDatabase.peerDao().fetchAllPeers();
    }

    @Override
    public void onCleared() {
        super.onCleared();
        Log.d(TAG, "Clearing PeersViewodel...");
        chatDatabase = null;
    }
}
