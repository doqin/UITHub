package com.example.uithub.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.Deadline;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.ViewHolder> {

    private List<Deadline> deadlines;
    private Context context;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public DeadlineAdapter(Context context, List<Deadline> deadlines) {
        this.context = context;
        this.deadlines = deadlines;
    }

    public void setDeadlines(List<Deadline> deadlines) {
        this.deadlines = deadlines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deadline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Deadline deadline = deadlines.get(position);
        holder.tvTitle.setText(deadline.getTitle());
        holder.tvCourse.setText(deadline.getCourseCode() != null ? deadline.getCourseCode() : "");
        
        // Reset state for recycled views
        holder.tvDescription.setMaxLines(2);
        
        if (deadline.getUrl() != null && !deadline.getUrl().isEmpty()) {
            holder.btnOpenMoodle.setVisibility(View.VISIBLE);
        } else {
            holder.btnOpenMoodle.setVisibility(View.GONE);
        }
        
        if (deadline.getDescription() != null && !deadline.getDescription().isEmpty()) {
            holder.tvDescription.setText(deadline.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        if (deadline.getEnd() != null && !deadline.getEnd().isEmpty()) {
            try {
                Date date = inputFormat.parse(deadline.getEnd());
                holder.tvEnd.setText("Hạn chót: " + outputFormat.format(date));
            } catch (ParseException e) {
                holder.tvEnd.setText("Hạn chót: " + deadline.getEnd());
            }
        } else {
            holder.tvEnd.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (holder.tvDescription.getMaxLines() == 2) {
                holder.tvDescription.setMaxLines(Integer.MAX_VALUE);
            } else {
                holder.tvDescription.setMaxLines(2);
            }
        });

        holder.btnOpenMoodle.setOnClickListener(v -> {
            if (deadline.getUrl() != null && !deadline.getUrl().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deadline.getUrl()));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deadlines != null ? deadlines.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCourse, tvEnd, tvDescription;
        com.google.android.material.button.MaterialButton btnOpenMoodle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDeadlineTitle);
            tvCourse = itemView.findViewById(R.id.tvDeadlineCourse);
            tvEnd = itemView.findViewById(R.id.tvDeadlineEnd);
            tvDescription = itemView.findViewById(R.id.tvDeadlineDescription);
            btnOpenMoodle = itemView.findViewById(R.id.btnOpenMoodle);
        }
    }
}
