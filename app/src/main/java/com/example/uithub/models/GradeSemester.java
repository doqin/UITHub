package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GradeSemester {
    @SerializedName("hocky")
    private int hocky;

    @SerializedName("namhoc")
    private int namhoc;

    @SerializedName("so_tin_chi")
    private double soTinChi;

    @SerializedName("diem_trung_binh")
    private String diemTrungBinh;

    @SerializedName("subjects")
    private List<GradeSubject> subjects;

    public int getHocky() { return hocky; }
    public int getNamhoc() { return namhoc; }
    public double getSoTinChi() { return soTinChi; }
    public String getDiemTrungBinh() { return diemTrungBinh; }
    public List<GradeSubject> getSubjects() { return subjects; }
}