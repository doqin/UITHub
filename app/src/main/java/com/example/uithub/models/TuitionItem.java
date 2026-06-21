package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class TuitionItem {
    @SerializedName("namhoc")
    private int namHoc;
    @SerializedName("hocky")
    private int hocKy;
    @SerializedName("sotien")
    private double soTien;

    public TuitionItem(int namHoc, int hocKy, double soTien) {
        this.namHoc = namHoc;
        this.hocKy = hocKy;
        this.soTien = soTien;
    }

    public String getHocKyNamHoc() { return "HK" + hocKy + " - " + namHoc; }
    public double getSoTien() { return soTien; }
}