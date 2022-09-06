package edu.stevens.cs522.chat.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;

import java.util.List;


import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.entities.Chatroom;
import edu.stevens.cs522.chat.ui.TextAdapter;
import edu.stevens.cs522.chat.viewmodels.ChatroomViewModel;

public class ChatroomsFragment extends Fragment implements View.OnClickListener, TextAdapter.OnItemClickListener<Chatroom> {

    @SuppressWarnings("unused")
    private final static String TAG = ChatroomsFragment.class.getCanonicalName();

    /**
     * The serialization (saved instance state) Bundle key representing the activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    public interface IChatroomListener {
        void addChatroom(String chatroomName);
        void setChatroom(Chatroom chatroom);
    }

    private IChatroomListener listener;

    private ChatroomViewModel chatroomViewModel;

    private TextAdapter<Chatroom> chatroomsAdapter;

    // For adding a new chatroom.
    private EditText chatroomName;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int activatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public ChatroomsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IChatroomListener) {
            listener = (IChatroomListener) context;
        } else {
            throw new IllegalStateException("Activity must implement INavigationListener!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chatrooms, container, false);

        RecyclerView chatroomList = rootView.findViewById(R.id.chatroom_list);
        chatroomList.setLayoutManager(new LinearLayoutManager(requireActivity()));

        // TODO Initialize the recyclerview and adapter for messages
        chatroomsAdapter = new TextAdapter<Chatroom>(chatroomList,this);
        chatroomList.setAdapter(chatroomsAdapter);

        chatroomName = rootView.findViewById(R.id.chatroom_add_text);

        Button chatroomButton = rootView.findViewById(R.id.chatroom_add_button);
        chatroomButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Called after onCreateView() returns.
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        // TODO initialize the chatroom view model
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);

        // TODO query the database asynchronously, and use messagesAdapter to display the result
        LiveData<List<Chatroom>> chatRooms = chatroomViewModel.fetchAllChatrooms();
        Observer<List<Chatroom>> observer = cht -> {
            chatroomsAdapter.setDataset(cht);
            chatroomsAdapter.notifyDataSetChanged();
        };
        chatRooms.observe(getViewLifecycleOwner(),observer);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*
     * This should be in StringUtils.
     */
    private static boolean isEmptyInput(Editable text) {
        return text.toString().trim().length() == 0;
    }

    @Override
    public void onClick(View v) {
        if (isEmptyInput(chatroomName.getText())) {
            Log.d(TAG, "...missing name for new chatroom.");
            Toast.makeText(requireActivity(), R.string.missing_chat_room_name, Toast.LENGTH_LONG).show();
            return;
        }

        // TODO request the activity to add the chatroom to the database
        listener.addChatroom(chatroomName.getText().toString());

        chatroomName.setText("");
    }

    @Override
    public void onItemClick(RecyclerView parent, View view, int position, Chatroom chatroom) {
        setActivatedPosition(position);
        // TODO ask the activity to respond to the selection (in single-pane layout, it will push detail fragment)
        listener.setChatroom(chatroom);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, activatedPosition);
        }
    }

    private void setActivatedPosition(int position) {

        if (position == ListView.INVALID_POSITION) {
            chatroomsAdapter.setItemChecked(activatedPosition);
        } else {
            chatroomsAdapter.setItemChecked(position);
        }
        activatedPosition = position;
    }

}