package edu.stevens.cs522.chat.databases;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

import edu.stevens.cs522.chat.entities.Message;

@Dao
/**
 * These are synchronous operations used on a background thread for syncing messages with a server.
 */
public abstract class RequestDao {

    /**
     * Get the last sequence number in the messages database.
     */
    @Query("SELECT MAX(seqNum) AS max_seq_num FROM Message WHERE seqNum <> 0")
    public abstract long getLastSequenceNumber();

    /**
     * Get all unsent messages, identified by sequence number = 0.
     */
    @Query("SELECT * FROM Message WHERE seqNum = 0")
    public abstract List<Message> getUnsentMessages();

    /**
     * After syncing with server, update the sequence numbers of uploaded messages
     */
    @Query("UPDATE Message SET seqNum = :seqNum WHERE id = :id")
    public abstract void updateSeqNum(long id, long seqNum);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insert(Message message);

    @Update
    protected abstract void update(Message message);

    /**
     * Insert other peer's messages and update our own, with input from server.
     */
    public void upsert(UUID appID, Message message) {
        if (appID.equals(message.appID)) {
            // One of our own messages returned from the server, update sequenceId
            update(message);
        } else {
            // Another peer's message, with sequenceId set by server
            message.id = 0;
            insert(message);
        }
    }

}
