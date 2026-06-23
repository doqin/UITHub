package com.example.uithub.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.R;
import com.example.uithub.models.TuitionItem;
import java.util.List;

public class TuitionAdapter extends RecyclerView.Adapter<TuitionAdapter.ViewHolder> {
    private final List<TuitionItem> list;

    public TuitionAdapter(List<TuitionItem> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tuition, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionItem item = list.get(position);
        holder.tvKy.setText(item.getHocKyNamHoc());
        holder.tvTien.setText(String.format("%,.0f VNĐ", item.getSoTien()));

        String status = item.getStatus();
        if (status != null && !status.isEmpty()) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            if ("PAID".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Đã thanh toán");
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A"));
            } else {
                holder.tvStatus.setText("Chưa thanh toán");
                holder.tvStatus.setTextColor(Color.parseColor("#DC2626"));
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        String deadline = item.getDeadline();
        if (deadline != null && !deadline.isEmpty()) {
            holder.tvDeadline.setVisibility(View.VISIBLE);
            holder.tvDeadline.setText("Hạn: " + deadline);
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKy, tvTien, tvStatus, tvDeadline;
        public ViewHolder(View v) {
            super(v);
            tvKy = v.findViewById(R.id.tvHocKyNamHoc);
            tvTien = v.findViewById(R.id.tvSoTien);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvDeadline = v.findViewById(R.id.tvDeadline);
        }
    }
}
