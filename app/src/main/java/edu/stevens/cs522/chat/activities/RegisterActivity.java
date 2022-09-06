/*********************************************************************

 Chat server: accept chat messages from clients.

 Sender chatName and GPS coordinates are encoded
 in the messages, and stripped off upon receipt.

 Copyright (c) 2017 Stevens Institute of Technology

 **********************************************************************/
package edu.stevens.cs522.chat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.rest.ChatHelper;
import edu.stevens.cs522.chat.services.ResultReceiverWrapper;
import edu.stevens.cs522.chat.settings.Settings;

public class RegisterActivity extends Activity implements OnClickListener, ResultReceiverWrapper.IReceive {

    final static public String TAG = RegisterActivity.class.getCanonicalName();

    /*
     * Widgets for server Uri, chat name, register button.
     */
    private EditText serverUriText;

    private EditText userNameText;

    /*
     * Helper for Web service
     */
    private ChatHelper helper;

    /*
     * For receiving ack when registered.
     */
    private ResultReceiverWrapper registerResultReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * We only get to register once.
         */
        if (Settings.isRegistered(this)) {
            finish();
            return;
        }

        setContentView(R.layout.register);

        // TODO instantiate helper for service (remember to use static context!)
        helper = new ChatHelper(getApplicationContext());


        // TODO initialize registerResultReceiver
        registerResultReceiver = new ResultReceiverWrapper(new Handler());

        serverUriText = findViewById(R.id.chat_server_text);
        serverUriText.setText(getString(R.string.server_uri_default));

        userNameText = findViewById(R.id.chat_name_text);

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(this);

    }

    public void onResume() {
        super.onResume();
        registerResultReceiver.setReceiver(this);
    }

    public void onPause() {
        super.onPause();
        registerResultReceiver.setReceiver(null);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    /*
     * Callback for the REGISTER button.
     */
    public void onClick(View v) {

        if (!Settings.isRegistered(this) && helper != null) {

            String serverAddress = serverUriText.getText().toString();
            if (serverAddress.isEmpty()) {
                Log.d(TAG, "Empty server URI for registration!");
                return;
            }

            if (!serverAddress.endsWith("/")) {
                serverAddress += "/";
            }
            Log.d(TAG, "Registering with server URI: "+serverAddress);

            Uri serverUri = Uri.parse(serverUriText.getText().toString()).normalizeScheme();

            String userName = userNameText.getText().toString();
            if (userName.isEmpty()) {
                Log.d(TAG, "Empty chat name for registration!");
                return;
            }
            Log.d(TAG, "Registering with chat name: "+userName);

            // TODO use helper to register
            helper.register(serverUri, userName, registerResultReceiver);

            // End todo

        } else {

            Toast.makeText(this, "Already Registered!", Toast.LENGTH_LONG).show();

        }

    }

    @Override
    /**
     * Callback to confirm registration success or failure.
     *
     * If success, the settings are updated in the request processor.
     */
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode) {
            case RESULT_OK:
                Log.d(TAG, "Successfully registered!");
                finish();
                break;
            default:
                Log.d(TAG, "Registration failed!");
                // TODO show a failure toast message
                Toast.makeText(this, "There has been an error registering, please try again.", Toast.LENGTH_LONG).show();
                break;
        }
    }

}