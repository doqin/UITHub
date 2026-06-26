package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class GradesSummary {
    @SerializedName("diem_trung_binh_chung_tich_luy")
    private double gpaTichLuy;

    @SerializedName("so_tin_chi_tich_luy")
    private double tinChiTichLuy;

    public double getGpaTichLuy() {
        return gpaTichLuy;
    }

    public void setGpaTichLuy(double gpaTichLuy) {
        this.gpaTichLuy = gpaTichLuy;
    }

    public double getTinChiTichLuy() {
        return tinChiTichLuy;
    }

    public void setTinChiTichLuy(double tinChiTichLuy) {
        this.tinChiTichLuy = tinChiTichLuy;
    }
}
