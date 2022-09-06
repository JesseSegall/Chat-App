package edu.stevens.cs522.chat.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.stevens.cs522.chat.R;

public class TextAdapter<T> extends RecyclerView.Adapter<TextAdapter<T>.ViewHolder> {

    private List<T> dataset;

    private int selected;

    // Indicate if something is selected view
    private static final int TYPE_INACTIVE = 1;
    private static final int TYPE_ACTIVE = 1;

    private final RecyclerView recyclerView;

    private final OnItemClickListener<T> listener;

    /**
     * Initialize the dataset of the Adapter
     */
    public TextAdapter(RecyclerView recyclerView) {
        this(recyclerView, null);
    }

    public TextAdapter(RecyclerView recyclerView, OnItemClickListener<T> listener) {
        this.dataset = new ArrayList<>();
        this.recyclerView = recyclerView;
        this.listener = listener;
        this.selected = ListView.INVALID_POSITION;
    }

    /*
     * The big challenge with RecyclerView is how to add an item click listener.
     * Typically people do this in onBindViewHolder, but that means allocating a new
     * intermediate listener every time a row is scrolled in the recyclerview, or in
     * onCreateViewHolder, but that means allocating a new
     * intermediate listener every time a row is added to the recyclerview.
     * Better to define this intermediate callback (implementing OnClick) once,
     * and register this with each row as it is added.
     */

    /*
     * We define our own version of OnItemClickListener that does not require id.
     */
    public interface OnItemClickListener<T> {
        void onItemClick(RecyclerView parent, View row, int position, T item);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView textView;

        public ViewHolder(View view) {
            super(view);

            this.textView = view.findViewById(R.id.text1);

            view.setOnClickListener(this);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        @Override
        public void onClick(View v) {
            int position = this.getBindingAdapterPosition();

            // Display the item as selected
            selected = position;
            notifyItemChanged(position);

            // TODO invoke the listener
            listener.onItemClick(recyclerView, v, selected, dataset.get(selected));

        }
    }

    public void setItemChecked(int position) {
        selected = position;
    }

    @Override
    public int getItemViewType(int position) {
        return position == selected ? TYPE_ACTIVE : TYPE_INACTIVE;
    }

    /*
     * Create new views (invoked by the layout manager)
     */
    @Override @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Selected or otherwise list item
        final int layout = (viewType == TYPE_INACTIVE) ? R.layout.item_inactive : R.layout.item_active;
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(layout, viewGroup, false);

        return new ViewHolder(view);
    }

    /*
     * Replace the contents of a view (invoked by the layout manager)
     * Do not bind the position of the view holder here, it will not be updated
     * if the contents of the backing store are edited (deletions & insertions)
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        T data = dataset.get(position);
        viewHolder.setText(data.toString());
    }

    /*
     * Return the size of your dataset (invoked by the layout manager)
     */
    @Override
    public int getItemCount() {
        return dataset.size();
    }

    /*
     * Invoked by live data observer.
     */
    public void setDataset(List<T> dataset) {
        this.dataset = dataset;
    }

}

