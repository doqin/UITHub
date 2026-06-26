package com.example.uithub.adapter;

import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.ExamModel;
import com.example.uithub.utils.CalendarUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ExamScheduleAdapter extends RecyclerView.Adapter<ExamScheduleAdapter.ViewHolder> {

    public interface OnExamCalendarSyncListener {
        void onSync(String title, String location, String description, long beginTime, long endTime);
    }

    private List<ExamModel> list;
    private OnExamCalendarSyncListener calendarSyncListener;

    public ExamScheduleAdapter(List<ExamModel> list) {
        this.list = list;
    }

    public void setData(List<ExamModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public void setOnCalendarSyncListener(OnExamCalendarSyncListener listener) {
        this.calendarSyncListener = listener;
    }

    public List<ExamModel> getData() {
        return list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exam_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExamModel item = list.get(position);

        holder.tvCourseCode.setText(item.getCourse_code());
        holder.tvClassCode.setText(item.getClass_code());

        // Exam shift - if null/empty, show fallback
        String examShift = item.getExam_shift();
        if (examShift != null && !examShift.isEmpty()) {
            holder.tvExamShift.setText(examShift);
            holder.tvExamShift.setVisibility(View.VISIBLE);
        } else {
            holder.tvExamShift.setVisibility(View.GONE);
        }

        // Format date: YYYY-MM-DD -> weekday DD/MM
        String dateStr = item.getExam_date();
        String weekday = item.getWeekday();
        if (dateStr != null && dateStr.length() >= 10) {
            String[] parts = dateStr.split("-");
            String formattedDate = weekday + " " + parts[2] + "/" + parts[1];
            holder.tvExamDate.setText(formattedDate);
        } else {
            holder.tvExamDate.setText(weekday + " " + dateStr);
        }

        Log.d("ExamAdapter", "start_time='" + item.getStart_time() + "' for course=" + item.getCourse_code());
        String startTime = item.getStart_time();
        if (startTime != null && !startTime.isEmpty()) {
            holder.tvExamTime.setText(startTime);
        } else {
            holder.tvExamTime.setText("Theo thông báo của giảng viên");
        }
        holder.tvExamRoom.setText(item.getRoom());

        // Status - computed locally using exam_date
        int days = calculateDaysRemaining(item.getExam_date());
        if (days >= 0) {
            holder.tvExamStatus.setText("Sắp thi");
            holder.tvExamStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.success_green));
        } else {
            holder.tvExamStatus.setText("Đã thi");
            holder.tvExamStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.muted_foreground));
        }

        // Days remaining - calculate locally using exam_date
        if (days > 0) {
            holder.tvDaysRemaining.setText(days + " ngày nữa");
            holder.tvDaysRemaining.setVisibility(View.VISIBLE);
        } else if (days == 0) {
            holder.tvDaysRemaining.setText("Hôm nay");
            holder.tvDaysRemaining.setVisibility(View.VISIBLE);
        } else {
            holder.tvDaysRemaining.setText("Đã qua " + Math.abs(days) + " ngày");
            holder.tvDaysRemaining.setVisibility(View.VISIBLE);
        }
    }
    private long convertToMillis(String date, String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date d = sdf.parse(date + " " + time);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
    private int calculateDaysRemaining(String examDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            Date exam = sdf.parse(examDate);
            // Get current time in local timezone (already Ho Chi Minh time)
            Date now = new Date();
            long diff = exam.getTime() - now.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseCode, tvClassCode, tvExamShift, tvExamDate, tvExamTime, tvExamRoom, tvExamStatus, tvDaysRemaining;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseCode = itemView.findViewById(R.id.tvCourseCode);
            tvClassCode = itemView.findViewById(R.id.tvClassCode);
            tvExamShift = itemView.findViewById(R.id.tvExamShift);
            tvExamDate = itemView.findViewById(R.id.tvExamDate);
            tvExamTime = itemView.findViewById(R.id.tvExamTime);
            tvExamRoom = itemView.findViewById(R.id.tvExamRoom);
            tvExamStatus = itemView.findViewById(R.id.tvExamStatus);
            tvDaysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
        }
    }
}