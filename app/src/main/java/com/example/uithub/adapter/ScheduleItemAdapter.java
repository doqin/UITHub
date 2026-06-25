package com.example.uithub.adapter;

import android.content.Intent;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.CalendarUtils;
import com.example.uithub.utils.ScheduleStatusUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleItemAdapter extends RecyclerView.Adapter<ScheduleItemAdapter.ViewHolder> {

    private List<ScheduleItem> list;

    public ScheduleItemAdapter(List<ScheduleItem> list) {
        this.list = list;
    }

    public void setData(List<ScheduleItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleItem item = list.get(position);

        holder.tvName.setText(item.name);
        holder.tvRoom.setText(item.room);
        holder.tvTeacher.setText(item.teacher);
        holder.tvPeriod.setText(item.period);
        holder.tvStartEndTime.setText(item.start_time + " - " + item.end_time);
        holder.tvInactiveWeek.setVisibility(ScheduleStatusUtils.isOpenThisWeek(item) ? View.GONE : View.VISIBLE);

        holder.btnOpenCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(CalendarContract.CONTENT_URI);
            v.getContext().startActivity(intent);
        });

        holder.btnSyncCalendar.setOnClickListener(v -> {
            long start = convertToMillis(item.start_time);
            long end = convertToMillis(item.end_time);
            CalendarUtils.addEvent(v.getContext(), "Học: " + item.name, item.room, start, end);
            Toast.makeText(v.getContext(), "Đang mở lịch...", Toast.LENGTH_SHORT).show();
        });
    }

    private long convertToMillis(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date d = sdf.parse(time);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvTime, tvRoom, tvTeacher, tvPeriod, tvStartEndTime, tvInactiveWeek;
        ImageView btnOpenCalendar, btnSyncCalendar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            tvStartEndTime = itemView.findViewById(R.id.tvStartEndTime);
            tvInactiveWeek = itemView.findViewById(R.id.tvInactiveWeek);

            btnOpenCalendar = itemView.findViewById(R.id.btnOpenCalendar);
            btnSyncCalendar = itemView.findViewById(R.id.btnSyncCalendar);
        }
    }
}
