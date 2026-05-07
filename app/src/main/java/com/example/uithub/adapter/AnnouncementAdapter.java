package com.example.uithub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.Announcement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> list;

    public AnnouncementAdapter(List<Announcement> list) {
        this.list = list;
    }

    public void setData(List<Announcement> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    private String formatDate(String raw) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date date = input.parse(raw);
            SimpleDateFormat output = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
            return output.format(date);
        } catch (ParseException e) {
            return raw;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Announcement a = list.get(position);

        holder.title.setText(a.getTitle());
        holder.date.setText(formatDate(a.getDate()));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
        }
    }
}