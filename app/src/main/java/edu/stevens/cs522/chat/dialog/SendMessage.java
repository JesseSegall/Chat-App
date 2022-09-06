package edu.stevens.cs522.chat.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Date;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.entities.Chatroom;

/**
 * Created by dduggan.
 */

public class SendMessage extends DialogFragment {

    private static final String TAG = SendMessage.class.getCanonicalName();

    public interface IMessageSender {
        void send(String chatroom, String text);
    }

    public static final String CHATROOM_KEY = "chatroom";

    public static void launch(FragmentActivity activity, String tag, Chatroom chatroom) {
        SendMessage dialog = new SendMessage();
        Bundle args = new Bundle();
        args.putParcelable(CHATROOM_KEY, chatroom);
        dialog.setArguments(args);
        dialog.show(activity.getSupportFragmentManager(), tag);
    }

    private IMessageSender listener;

    private Chatroom currentChatroom;

    private EditText messageText;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (!(activity instanceof IMessageSender)) {
            throw new IllegalStateException("Activity must implement IMessageSender.");
        }
        listener = (IMessageSender) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // If not using AlertDialog
        View rootView = inflater.inflate(R.layout.send_message, container, false);

        // Initialize the UI

        currentChatroom = getArguments().getParcelable(CHATROOM_KEY);

        messageText = rootView.findViewById(R.id.message_text);

        Button confirm = rootView.findViewById(R.id.send);
        confirm.setOnClickListener(confirmListener);

        Button cancel = rootView.findViewById(R.id.cancel);
        cancel.setOnClickListener(cancelListener);

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Not much to do unless using AlertDialog
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    /*
     * This should be in StringUtils.
     */
    private static boolean isEmptyInput(Editable text) {
        return text.toString().trim().length() == 0;
    }

    private final OnClickListener confirmListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Confirming message send...");
            Context context = requireActivity();
            if (isEmptyInput(messageText.getText())) {
                Log.d(TAG, "...missing message text.");
                Toast.makeText(context, R.string.missing_chat_text, Toast.LENGTH_LONG).show();
                return;
            }

            String message = messageText.getText().toString();
            Log.d(TAG, String.format("...sending \"%s\" to %s....", message, currentChatroom.name));

            // TODO get the activity to send the message
            listener.send(currentChatroom.name, message);


            Log.d(TAG, "...dismissing dialog.");
            SendMessage.this.dismiss();
        }
    };

    private final OnClickListener cancelListener = new OnClickListener() {
        public void onClick(View view) {
            SendMessage.this.getDialog().cancel();
        }
    };



}
