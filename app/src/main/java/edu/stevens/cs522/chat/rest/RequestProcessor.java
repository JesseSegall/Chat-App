package edu.stevens.cs522.chat.rest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import edu.stevens.cs522.base.DateUtils;
import edu.stevens.cs522.base.StringUtils;
import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.databases.ChatDatabase;
import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.location.CurrentLocation;
import edu.stevens.cs522.chat.rest.client.StreamingOutput;
import edu.stevens.cs522.chat.rest.client.StreamingResponse;
import edu.stevens.cs522.chat.rest.request.ChatServiceRequest;
import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import edu.stevens.cs522.chat.rest.request.DummyResponse;
import edu.stevens.cs522.chat.rest.request.ErrorResponse;
import edu.stevens.cs522.chat.rest.request.PostMessageRequest;
import edu.stevens.cs522.chat.rest.request.PostMessageResponse;
import edu.stevens.cs522.chat.rest.request.RegisterRequest;
import edu.stevens.cs522.chat.rest.request.RegisterResponse;
import edu.stevens.cs522.chat.rest.request.SynchronizeRequest;
import edu.stevens.cs522.chat.settings.Settings;

/**
 * Created by dduggan.
 */

public class RequestProcessor {

    private static final String TAG = RequestProcessor.class.getCanonicalName();

    private final Context context;

    private final CurrentLocation location;

    private final RestMethod restMethod;

    private final ChatDatabase chatDatabase;

    private RequestProcessor(Context context) {
        this.context = context;

        this.location = new CurrentLocation(context);

        this.restMethod = new RestMethod(context);

        this.chatDatabase = ChatDatabase.getInstance(context);
    }

    public static RequestProcessor getInstance(Context context) {
        return new RequestProcessor(context);
    }

    /**
     * We use the Visitor pattern to dispatch to the appropriate request processing.
     * This is also where we attach metadata to the request that is attached as
     * application-specific request headers to the HTTP request.
     * @param request
     * @return
     */
    public ChatServiceResponse process(ChatServiceRequest request) {
        request.appID = Settings.getAppId(context);
        request.timestamp = DateUtils.now();
        request.latitude = location.getLatitude();
        request.longitude = location.getLongitude();
        return request.process(this);
    }

    public ChatServiceResponse perform(RegisterRequest request) {

        Log.d(TAG, "Registering as " + request.chatname);
        ChatServiceResponse response = restMethod.perform(request);

        if (response instanceof RegisterResponse) {
            /*
             * Add a record for this peer to the local database.
             */
            RegisterResponse registration = (RegisterResponse) response;

            final Peer peer = new Peer();
            peer.name = request.chatname;
            peer.timestamp = request.timestamp;
            peer.latitude = request.latitude;
            peer.longitude = request.longitude;
            chatDatabase.peerDao().upsert(peer);

            // Initialize the chatrooms database with the default chatroom
            chatDatabase.chatroomDao().insert(new Chatroom(context.getString(R.string.default_chat_room)));

            // TODO save the server URI and user name in settings
            Settings.saveChatName(context, request.chatname);
            Settings.saveServerUri(context, request.chatServer);

        }
        return response;
    }

    public ChatServiceResponse perform(PostMessageRequest request) {

        Log.d(TAG, "Posting message."+request.message.messageText);

        Log.d(TAG, "Adding the chatroom to the local database, if not already there.");
        chatDatabase.chatroomDao().insert(new Chatroom(request.message.chatroom));

        Log.d(TAG, "Inserting the message into the local database.");
        long id = -1;  // Local PK of the message in the DB
        // TODO insert the message into the local database
        id = chatDatabase.requestDao().insert(request.message);

        if (!Settings.SYNC) {
            /*
             * We are synchronously uploading messages to the server.
             */
            Log.d(TAG, "Synchronization turned off, upload the message to the server directly.");
            ChatServiceResponse response = restMethod.perform(request);
            if (response instanceof PostMessageResponse) {
                Log.d(TAG, "Message upload successful!");
                PostMessageResponse postMessageResponse = (PostMessageResponse)response;

                // TODO update the message in the database with the sequence number

                chatDatabase.requestDao().updateSeqNum(id, postMessageResponse.getMessageId());

            }
            return response;
        } else {
            /*
             * We will just insert the message into the database, and rely on background
             * synchronization driven by alarms to upload it asynchronously.
             */
            Log.d(TAG, "We will upload the message when we synchronize with the database later.");
            return request.getDummyResponse();
        }
    }

    private final Type peerType = TypeToken.get(Peer.class).getType();

    private final Type chatroomType = TypeToken.get(Chatroom.class).getType();

    private final Type messageType = TypeToken.get(Message.class).getType();

    /**
     * For SYNC: perform a sync using a request manager.  These requests are
     * generated from an alarm that is scheduled at periodic intervals.
     */
    public ChatServiceResponse perform(SynchronizeRequest request) {

        if (!Settings.SYNC) {
            throw new IllegalStateException("Performing synchronization but SYNC flag is false!");
        }

        if (!Settings.isRegistered(context)) {
            Log.d(TAG, "Background sync before registration will be skipped...");
            return new DummyResponse();
        }

        Log.d(TAG, "Performing synchronization request.");

        StreamingResponse response = null;

        Gson gson = restMethod.getGson();

        /*
         * We upload a list of all our chatrooms to the server.
         */
        final List<Chatroom> chatrooms = chatDatabase.chatroomDao().getAllChatrooms();

        /*
         * Get the messages that we have not yet uploaded to the server (sequenceId = 0).
         */
        final List<Message> messages = chatDatabase.requestDao().getUnsentMessages();

        /*
         * The server needs the sequence number of the last message it downloaded to this device.
         * The server will download any messages it has "seen" since it last synced with this device.
         */
        request.lastSequenceNumber = chatDatabase.requestDao().getLastSequenceNumber();

        try {
            /*
             * This is the callback from streaming new local messages to the server.
             */
            StreamingOutput out = new StreamingOutput() {
                @Override
                public void write(final OutputStream os) throws IOException {

                    try (JsonWriter wr = gson.newJsonWriter(new OutputStreamWriter(new BufferedOutputStream(os), RestMethod.CHARSET))) {

                        wr.beginObject();

                        wr.name(RestMethod.CHATROOMS);
                        // Upload a list of all chatrooms.
                        wr.beginArray();
                        for (Chatroom chatroom : chatrooms) {
                            Log.d(TAG, "Uploading chatroom: "+chatroom.name);
                            gson.toJson(chatroom, chatroomType, wr);
                        }
                        wr.endArray();

                        wr.name(RestMethod.MESSAGES);
                        // TODO upload a list of unread messages.
                        wr.beginArray();
                        for(Message message : messages){
                            Log.d(TAG, "Uploading messages: "+message);
                            gson.toJson(message,messageType,wr);
                        }
                        wr.endArray();


                        wr.endObject();

                        wr.flush();

                    }
                }
            };
            /*
             * Connect to the server and upload messages not yet shared.
             */
            response = restMethod.perform(request, out);

            /*
             * Stream downloaded peer, chatroom and message information, and update the database.
             * The connection is closed in the finally block below.
             */
            try (JsonReader rd = gson.newJsonReader(new InputStreamReader(new BufferedInputStream(response.getInputStream()), StringUtils.CHARSET))) {
                // Parse data from server (messages and peers) and update database
                rd.beginObject();

                /*
                 * Download list of peers (some new, some updated).
                 */
                String label = rd.nextName();
                if (!RestMethod.PEERS.equals(label)) {
                    throw new IllegalStateException("Expected 'peers', unexpected JSON label: " + label);
                }

                rd.beginArray();
                while (rd.peek() != JsonToken.END_ARRAY) {
                    Peer peer = gson.fromJson(rd, peerType);
                    peer.id = 0;
                    Log.d(TAG, "Upserting peer: "+peer.name);
                    chatDatabase.peerDao().upsert(peer);
                }
                rd.endArray();

                /*
                 * Download list of chatrooms (some already in database).
                 */
                label = rd.nextName();
                if (!RestMethod.CHATROOMS.equals(label)) {
                    throw new IllegalStateException("Expected 'chatrooms', unexpected JSON label: " + label);
                }

                rd.beginArray();
                while (rd.peek() != JsonToken.END_ARRAY) {
                    Chatroom chatroom = gson.fromJson(rd, chatroomType);
                    chatroom.id = 0;
                    Log.d(TAG, "Upserting chatroom: "+chatroom.name);
                    chatDatabase.chatroomDao().insert(chatroom);
                }
                rd.endArray();

                /*
                 * Download new messages that have been uploaded to the server.
                 */
                label = rd.nextName();
                if (!RestMethod.MESSAGES.equals(label)) {
                    throw new IllegalStateException("Expected 'messages', unexpected JSON label: " + label);
                }

                UUID appID = Settings.getAppId(context);

                // TODO parse the list of messages and upsert them into the database.
                rd.beginArray();
                while(rd.peek() != JsonToken.END_ARRAY){
                    Message message = gson.fromJson(rd, messageType);
                    chatDatabase.requestDao().upsert(appID, message);
                }

                rd.endArray();
                rd.endObject();

            }

            return response.getResponse();

        } catch (IOException e) {
            Log.e(TAG, "Failure during synchronization!", e);
            return new ErrorResponse(0, ErrorResponse.Status.SERVER_ERROR, e.getMessage());

        } finally {
            if (response != null) {
                response.disconnect();
            }
        }
    }


}
