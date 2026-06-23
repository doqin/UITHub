package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class TuitionItem {
    @SerializedName("namhoc")
    private int namHoc;
    @SerializedName("hocky")
    private int hocKy;
    @SerializedName("sotien")
    private double soTien;
    @SerializedName("status")
    private String status;
    @SerializedName("deadline")
    private String deadline;

    public TuitionItem(int namHoc, int hocKy, double soTien, String status, String deadline) {
        this.namHoc = namHoc;
        this.hocKy = hocKy;
        this.soTien = soTien;
        this.status = status;
        this.deadline = deadline;
    }

    public String getHocKyNamHoc() { return "HK" + hocKy + " - " + namHoc; }
    public double getSoTien() { return soTien; }
    public String getStatus() { return status; }
    public String getDeadline() { return deadline; }
}