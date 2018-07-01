package com.xpacer.journalapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xpacer.journalapp.data.GistEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class JournalGistAdapter extends Adapter<JournalGistAdapter.GistViewHolder> {

    private List<GistEntry> mJournalData;
    final private ItemClickListener mItemClickListener;
    private Context mContext;

    public JournalGistAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    public interface ItemClickListener {
        void onItemClickListener(String itemId);
    }

    @NonNull
    @Override
    public GistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.card_view_gist, parent, false);

        return new GistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GistViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mJournalData == null) {
            return 0;
        }
        return mJournalData.size();
    }

    class GistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCaption;
        private TextView mDate;


        GistViewHolder(View itemView) {
            super(itemView);
            mCaption = itemView.findViewById(R.id.text_view_caption);
            mDate = itemView.findViewById(R.id.text_view_date);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            String caption = mJournalData.get(position).getCaption();
            Date date = mJournalData.get(position).getCreatedAt();
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            mCaption.setText(caption);
            // Convert Date to String in Java
            String dateString = sdf.format(date);
            mDate.setText(dateString);

        }


        @Override
        public void onClick(View view) {
            String elementId = mJournalData.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }

    }

    public void setJournalData(List<GistEntry> journalData) {
        mJournalData = journalData;
        notifyDataSetChanged();
    }
}
