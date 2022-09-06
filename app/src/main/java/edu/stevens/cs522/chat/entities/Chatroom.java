package edu.stevens.cs522.chat.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Created by dduggan.
 */

@Entity(indices = {@Index(value = {"name"}, unique = true)})
public class Chatroom implements Parcelable {

    // Primary key in the database
    @PrimaryKey(autoGenerate = true)
    public long id;

    // Name of the chat room
    public String name;

    public Chatroom() { }

    @Ignore
    public Chatroom(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    protected Chatroom(Parcel in) {
        id = in.readLong();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

}
