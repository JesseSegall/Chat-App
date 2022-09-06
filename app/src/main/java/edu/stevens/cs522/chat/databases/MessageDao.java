package edu.stevens.cs522.chat.databases;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;

// TODO add annotations for Repository pattern
@Dao
public interface MessageDao {
    @Query("select * from message WHERE message.chatroom = :chatroom")
    LiveData<List<Message>> fetchAllMessages(String chatroom);

    @Query("SELECT * FROM message  WHERE message.sender = :peerName")
    LiveData<List<Message>> fetchMessagesFromPeer(String peerName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void persist(Message message);

}
