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

@Dao
/*
 * Make sure to declare an index on chatroom name, that specifies chat names are unique.
 */
public abstract class ChatroomDao {

    /*
     * List of chatrooms for the UI (asynchronous)
     */
    @Query("SELECT * FROM Chatroom")
    public abstract LiveData<List<Chatroom>> fetchAllChatrooms();

    /*
     * List of chatrooms to be synced with the server (synchronous)
     */
    @Query("SELECT * FROM Chatroom")
    public abstract List<Chatroom> getAllChatrooms();

    /*
     * Insert a chatroom, ignore conflict if it already occurs
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(Chatroom chatroom);

}
