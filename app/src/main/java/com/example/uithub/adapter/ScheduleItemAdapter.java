package com.example.uithub.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.ScheduleItem;
import com.example.uithub.utils.ScheduleStatusUtils;

import java.util.List;

public class ScheduleItemAdapter extends RecyclerView.Adapter<ScheduleItemAdapter.ViewHolder> {

    public interface OnCalendarSyncListener {
        void onSync(String title, String location, String description,
                    String vietnameseDay, String dateStr, String endDateStr,
                    long beginTime, long endTime);
    }

    public interface OnCalendarSyncAllListener {
        void onSyncAll(List<ScheduleItem> items);
    }

    private List<ScheduleItem> list;
    private OnCalendarSyncListener calendarSyncListener;
    private OnCalendarSyncAllListener syncAllListener;

    public ScheduleItemAdapter(List<ScheduleItem> list) {
        this.list = list;
    }

    public void setData(List<ScheduleItem> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public void setOnCalendarSyncListener(OnCalendarSyncListener listener) {
        this.calendarSyncListener = listener;
    }

    public void setOnCalendarSyncAllListener(OnCalendarSyncAllListener listener) {
        this.syncAllListener = listener;
    }

    public List<ScheduleItem> getData() {
        return list;
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
    }

    private long convertToMillis(String date, String time) {
        return com.example.uithub.utils.CalendarUtils.convertToMillis(date, time);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvTime, tvRoom, tvTeacher, tvPeriod, tvStartEndTime, tvInactiveWeek;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            tvTeacher = itemView.findViewById(R.id.tvTeacher);
            tvPeriod = itemView.findViewById(R.id.tvPeriod);
            tvStartEndTime = itemView.findViewById(R.id.tvStartEndTime);
            tvInactiveWeek = itemView.findViewById(R.id.tvInactiveWeek);
        }
    }
}