package com.example.uithub.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uithub.R;
import com.example.uithub.models.GradeSubject;

import java.util.List;

public class GradeSubjectAdapter extends RecyclerView.Adapter<GradeSubjectAdapter.SubjectViewHolder> {

    private List<GradeSubject> subjectList;

    public GradeSubjectAdapter(List<GradeSubject> subjectList) {
        this.subjectList = subjectList;
    }

    public void setSubjects(List<GradeSubject> subjects) {
        this.subjectList = subjects;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grade_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        GradeSubject subject = subjectList.get(position);
        holder.bind(subject);
    }

    @Override
    public int getItemCount() {
        return subjectList != null ? subjectList.size() : 0;
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStt;
        private final TextView tvCode;
        private final TextView tvName;
        private final TextView tvCredits;
        private final TextView tvDiemQt;
        private final TextView tvDiemGk;
        private final TextView tvDiemTh;
        private final TextView tvDiemCk;
        private final TextView tvDiemHp;
        private final View layoutQt;
        private final View layoutGk;
        private final View layoutTh;

        SubjectViewHolder(View itemView) {
            super(itemView);
            tvStt = itemView.findViewById(R.id.tvSubjectStt);
            tvCode = itemView.findViewById(R.id.tvSubjectCode);
            tvName = itemView.findViewById(R.id.tvSubjectName);
            tvCredits = itemView.findViewById(R.id.tvSubjectCredits);
            tvDiemQt = itemView.findViewById(R.id.tvDiemQt);
            tvDiemGk = itemView.findViewById(R.id.tvDiemGk);
            tvDiemTh = itemView.findViewById(R.id.tvDiemTh);
            tvDiemCk = itemView.findViewById(R.id.tvDiemCk);
            tvDiemHp = itemView.findViewById(R.id.tvDiemHp);
            layoutQt = itemView.findViewById(R.id.layoutQt);
            layoutGk = itemView.findViewById(R.id.layoutGk);
            layoutTh = itemView.findViewById(R.id.layoutTh);
        }

        void bind(GradeSubject subject) {
            tvStt.setText(String.valueOf(subject.getStt()));
            tvCode.setText(subject.getMaHp());
            tvName.setText(subject.getTenHocPhan());
            tvCredits.setText(subject.getTinChi() + " TC");

            // Show/hide QT
            if (subject.getDiemQt() != null) {
                layoutQt.setVisibility(View.VISIBLE);
                tvDiemQt.setText(formatScore(subject.getDiemQt()));
            } else {
                layoutQt.setVisibility(View.GONE);
            }

            // Show/hide GK
            if (subject.getDiemGk() != null) {
                layoutGk.setVisibility(View.VISIBLE);
                tvDiemGk.setText(formatScore(subject.getDiemGk()));
            } else {
                layoutGk.setVisibility(View.GONE);
            }

            // Show/hide TH
            if (subject.getDiemTh() != null) {
                layoutTh.setVisibility(View.VISIBLE);
                tvDiemTh.setText(formatScore(subject.getDiemTh()));
            } else {
                layoutTh.setVisibility(View.GONE);
            }

            // CK
            if (subject.getDiemCk() != null) {
                tvDiemCk.setText(formatScore(subject.getDiemCk()));
            } else {
                tvDiemCk.setText("--");
            }

            // Điểm HP (string: có thể là số hoặc "Miễn", "---")
            tvDiemHp.setText(subject.getDiemHp() != null ? subject.getDiemHp() : "--");
        }

        private String formatScore(Double score) {
            if (score == null) return "--";
            if (score == score.longValue()) {
                return String.valueOf(score.longValue());
            }
            return String.format("%.1f", score);
        }
    }
}