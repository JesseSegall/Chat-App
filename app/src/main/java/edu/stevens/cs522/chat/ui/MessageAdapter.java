package edu.stevens.cs522.chat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.R;
import edu.stevens.cs522.chat.entities.Message;

public abstract class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final String TAG = MessageAdapter.class.getCanonicalName();

    private List<Message> messages;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView headingView;  // either sender or chatroom, depending on activity

        private final TextView messageView;

        public ViewHolder(View view) {
            super(view);

            headingView = view.findViewById(R.id.heading);

            messageView = view.findViewById(R.id.message);
        }

        public void setMetadata(String heading) {
            headingView.setText(heading);
        }

        public void setMessage(String message) {
            messageView.setText(message);
        }

    }

    /**
     * Initialize the dataset of the Adapter
     */
    public MessageAdapter() {
        this.messages = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.message, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // TODO set the fields of the view for the message
        // Use getHeading() to get the heading in the message
        Message message = messages.get(position);
        viewHolder.setMetadata(getHeading(message));
        viewHolder.setMessage("- "+message.messageText);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return messages.size();
    }

    public abstract String getHeading(Message message);

    /*
     * Invoked by live data observer.
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}

