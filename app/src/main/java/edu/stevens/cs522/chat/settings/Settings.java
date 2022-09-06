package edu.stevens.cs522.chat.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.UUID;

import edu.stevens.cs522.chat.R;

public class Settings {

    private static final String TAG = Settings.class.getCanonicalName();

    /**
     * This static flag is used to specify synchronous message vs asynchronous posting of messages.
     * Synchronous means that a chat message is sent immediately to the chat server (on a background
     * thread using RequestService and RequestProcessor).  In this mode, the app only uploads
     * chat messages to the server.  Asynchronous means that messages are added to the local
     * message database, which is then periodically synchronized with the server message database
     * by an alarm-driven process.  In this latter scenario, messages are downloaded from the
     * server to this device as part of the synchronization.
     */
    public static final boolean SYNC = true;

    /*
     * Key for storing UUID identifying this installation in preferences.
     */
    private static final String APPID_KEY = "app-id";

    /*
     * URI for the chat server (set during registration).
     */
    private static final String CHAT_SERVER_KEY = "server_uri";

    /*
     * The chat (peer) name for this device on the server (set during registration).
     */
    private static final String CHAT_NAME_KEY = "user-name";

    private static SharedPreferences getPreferences(Context context) {
        // return context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static UUID getAppId(Context context) {
        SharedPreferences prefs = getPreferences(context);
        String appID = prefs.getString(APPID_KEY, null);
        if (appID == null) {
            appID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(APPID_KEY, appID);
            String chatName = prefs.getString(CHAT_NAME_KEY, null);
            if (chatName == null) {
                editor.putString(CHAT_NAME_KEY, context.getString(R.string.user_name_default));
            }
            editor.apply();
        }
        return UUID.fromString(appID);
    }

    private static String defaultUserName;

    public static String getChatName(Context context) {
        if (defaultUserName == null) {
            defaultUserName = context.getString(R.string.default_user_name);
        }
        SharedPreferences prefs = getPreferences(context);
        return prefs.getString(CHAT_NAME_KEY, defaultUserName);
    }

    public static void saveChatName(Context context, String chatName) {
        SharedPreferences.Editor editor =  getPreferences(context).edit();
        editor.putString(CHAT_NAME_KEY, chatName);
        editor.apply();
    }

    public static Uri getServerUri(Context context) {
        SharedPreferences prefs = getPreferences(context);
        String serverText = prefs.getString(CHAT_SERVER_KEY, null);
        return (serverText == null) ? null : Uri.parse(serverText);
    }

    public static void saveServerUri(Context context, Uri serverUri) {
        SharedPreferences.Editor editor =  getPreferences(context).edit();
        String serverText = (serverUri == null) ? null : serverUri.toString();
        editor.putString(CHAT_SERVER_KEY, serverText);
        editor.apply();
    }

    public static boolean isDefaultChatname(Context context, String chatName) {
        if (defaultUserName == null) {
            defaultUserName = context.getString(R.string.default_user_name);
        }
        return !defaultUserName.equals(chatName);
    }

    public static boolean isRegistered(Context context) {
        if (defaultUserName == null) {
            defaultUserName = context.getString(R.string.default_user_name);
        }
        SharedPreferences prefs = getPreferences(context);
        return !defaultUserName.equals(prefs.getString(CHAT_NAME_KEY, defaultUserName));
    }
}
