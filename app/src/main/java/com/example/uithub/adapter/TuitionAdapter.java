package com.example.uithub.adapter;

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
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKy, tvTien;
        public ViewHolder(View v) {
            super(v);
            tvKy = v.findViewById(R.id.tvHocKyNamHoc);
            tvTien = v.findViewById(R.id.tvSoTien);
        }
    }
}