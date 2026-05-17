package com.example.uithub.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.AnnouncementDetail;
import com.example.uithub.R;
import com.example.uithub.models.Announcement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private List<Announcement> list;

    public AnnouncementAdapter(List<Announcement> list) {
        this.list = list != null ? list : new ArrayList<>();
    }

    public void setData(List<Announcement> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addData(List<Announcement> moreList) {
        if (moreList != null && !moreList.isEmpty()) {
            int startPos = list.size();
            list.addAll(moreList);
            notifyItemRangeInserted(startPos, moreList.size());
        }
    }

    private String formatDate(String raw) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date date = input.parse(raw);
            if (date == null) return raw;

            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            Calendar then = Calendar.getInstance();
            then.setTime(date);
            then.set(Calendar.HOUR_OF_DAY, 0);
            then.set(Calendar.MINUTE, 0);
            then.set(Calendar.SECOND, 0);
            then.set(Calendar.MILLISECOND, 0);

            long diffMillis = now.getTimeInMillis() - then.getTimeInMillis();
            long days = diffMillis / (24 * 60 * 60 * 1000);

            if (days == 0) {
                return "Hôm nay";
            } else if (days == 1) {
                return "Hôm qua";
            } else if (days > 1 && days < 7) {
                return days + " ngày trước";
            } else if (days == 7) {
                return "Một tuần trước";
            } else {
                SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return output.format(date);
            }
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

        Log.d("ANNOUNCEMENT", "Title: " + a.getTitle());

        if (a.getDetails() != null) {
            Log.d("ANNOUNCEMENT",
                    "Content: " + a.getDetails().getContent());
        } else {
            Log.d("ANNOUNCEMENT",
                    "Details is NULL");
        }

        holder.title.setText(a.getTitle());
        holder.date.setText(formatDate(a.getDate()));

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(
                    v.getContext(),
                    AnnouncementDetail.class
            );

            intent.putExtra("node_id", a.getNode_id());

            v.getContext().startActivity(intent);
        });
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
