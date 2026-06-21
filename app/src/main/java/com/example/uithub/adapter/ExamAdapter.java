package com.example.uithub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uithub.R;
import com.example.uithub.models.ExamModel;
import java.util.List;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {
    private List<ExamModel> examList;

    public ExamAdapter(List<ExamModel> examList) { this.examList = examList; }

    public static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvClass, tvTime, tvRoom;
        public ExamViewHolder(View v) {
            super(v);
            tvSubject = v.findViewById(R.id.tvExamSubject);
            tvClass = v.findViewById(R.id.tvExamClass);
            tvTime = v.findViewById(R.id.tvExamTime);
            tvRoom = v.findViewById(R.id.tvExamRoom);
        }
    }

    @Override
    public ExamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam_row, parent, false);
        return new ExamViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ExamViewHolder holder, int position) {
        ExamModel item = examList.get(position);
        holder.tvSubject.setText(item.getMa_mh());
        holder.tvClass.setText("Lớp: " + item.getMa_lop());
        holder.tvTime.setText("Ngày: " + item.getNgay_thi() + " - " + item.getCa_tiet_thi());
        holder.tvRoom.setText("Phòng thi: " + item.getPhong_thi());
    }

    @Override
    public int getItemCount() { return examList != null ? examList.size() : 0; }
}