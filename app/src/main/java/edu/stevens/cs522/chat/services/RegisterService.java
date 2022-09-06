package edu.stevens.cs522.chat.services;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.activities.RegisterActivity;
import edu.stevens.cs522.chat.rest.RequestProcessor;
import edu.stevens.cs522.chat.rest.request.ErrorResponse;
import edu.stevens.cs522.chat.rest.request.ChatServiceRequest;
import edu.stevens.cs522.chat.rest.request.ChatServiceResponse;
import edu.stevens.cs522.chat.rest.request.RegisterRequest;
import edu.stevens.cs522.chat.rest.request.SynchronizeRequest;
import edu.stevens.cs522.chat.settings.Settings;

/**
 * A service for handling asynchronous task requests on a separate handler thread.
 */
public class RegisterService extends Service {

    private static final String TAG = RegisterService.class.getCanonicalName();

    private static final String SERVER_URL_KEY = "edu.stevens.cs522.chat.rest.extra.SERVER_URL";

    private static final String CHAT_NAME_KEY = "edu.stevens.cs522.chat.rest.extra.CHAT_NAME";

    private static final String RECEIVER_KEY = "edu.stevens.cs522.chat.rest.extra.RECEIVER";

    private RequestProcessor processor;

    protected Executor executor;

    protected Handler mainLoop;

    public static void register(Context context, Uri serverUri, String chatName) {
        register(context, serverUri, chatName, null);
    }

    public static void register(Context context, Uri serverUri, String chatName, ResultReceiver receiver) {
        Intent intent = new Intent(context, RegisterService.class);
        intent.putExtra(SERVER_URL_KEY, serverUri);
        intent.putExtra(CHAT_NAME_KEY, chatName);
        if (receiver != null) {
            intent.putExtra(RECEIVER_KEY, receiver);
        }
        Log.d(TAG, "Starting foreground service for registration....");
        context.startForegroundService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Initializing registration service....");
        executor = Executors.newSingleThreadExecutor();
        mainLoop = new Handler(Looper.getMainLooper());
        processor = RequestProcessor.getInstance(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, final int startId) {

        Intent notificationIntent = new Intent(this, RegisterActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        /*
         * If you are feeling ambitious, try adding a CANCEL action to the notification.....
         */
        String channelId = getString(R.string.chat_channel_id);
        Notification.Builder notificationBuilder =
                new Notification.Builder(this, channelId)
                        .setContentTitle(getText(R.string.register_notification_title))
                        .setContentText(getText(R.string.register_notification_message))
                        .setSmallIcon(R.drawable.ic_chat)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.register_ticker_text))
                        .setSmallIcon(R.drawable.ic_chat);
        /*
         * What's the point in using a foreground service if we can't see the notification?
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            notificationBuilder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        Notification notification = notificationBuilder.build();

        // The doc says to make this non-zero, does not warn about other values......
        int registerNotificationId = getResources().getInteger(R.integer.register_notification_id);
        Log.d(TAG, "Displaying notification for foreground registration service....");
        startForeground(registerNotificationId, notification);

        /*
         * Now we've set the notification for the foreground service, let's register!
         */
        final Uri serverUrl = intent.getParcelableExtra(SERVER_URL_KEY);
        if (serverUrl == null) {
            throw new IllegalStateException("No server URL specified for registration!");
        }

        final String chatName = intent.getStringExtra(CHAT_NAME_KEY);
        if (chatName == null || chatName.isEmpty()) {
            throw new IllegalStateException("No chat name specified for registration!");
        }

        final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_KEY);

        /*
         * Insert the peer record (on a background thread) and save the data about this peer.
         */
        final Context context = getApplicationContext();

        executor.execute(() -> {

            ChatServiceResponse response = null;

            // We will sleep for a few seconds just to show a foreground service in action
            try {
                Log.d(TAG, "Registering with the chat server on a background thread.....");
                Thread.sleep(12000);

                RegisterRequest registerRequest = new RegisterRequest(serverUrl, chatName);

                response = processor.process(registerRequest);

            } catch (Exception e) {

                Log.d(TAG, "Registration service threw an excepion....", e);

            } finally {

                final ChatServiceResponse registerResponse = response;

                /*
                 * Now we're registered, update the activity and shut down the service on the main thread.
                 */
                mainLoop.post(() -> {

                    Log.d(TAG, "Processing registration response back on the main thread....");
                    RegisterService.this.stopForeground(0);

                    if (receiver != null) {
                        // Use receiver to call back to activity
                        if (registerResponse != null && !(registerResponse instanceof ErrorResponse)) {
                            // TODO let activity know request succeeded
                            receiver.send(Activity.RESULT_OK, null);


                        } else {
                            // TODO let activity know request failed
                            receiver.send(Activity.RESULT_CANCELED, null);


                        }
                    } else {
                        Log.d(TAG, "Missing receiver for registration.");
                    }

                    Log.d(TAG, "Stopping the registration service.....");
                    RegisterService.this.stopSelf(startId);

                });

            }
        });

        return START_NOT_STICKY;

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        String id = getString(R.string.chat_channel_id);
        String name = getString(R.string.chat_channel_name);
        String description = getString(R.string.chat_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new IllegalStateException("Unimplemented onBind!");
    }

    public void onDestroy() {
        super.onDestroy();
        executor = null;
        mainLoop = null;
        processor = null;
    }
}
