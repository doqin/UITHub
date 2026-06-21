package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class ExamModel {
    @SerializedName("stt") private int stt;
    @SerializedName("ma_mh") private String ma_mh;
    @SerializedName("ma_lop") private String ma_lop;
    @SerializedName("ca_tiet_thi") private String ca_tiet_thi;
    @SerializedName("thu_thi") private String thu_thi;
    @SerializedName("ngay_thi") private String ngay_thi;
    @SerializedName("phong_thi") private String phong_thi;
    @SerializedName("ghi_chu") private String ghi_chu;

    public String getMa_mh() { return ma_mh; }
    public String getMa_lop() { return ma_lop; }
    public String getCa_tiet_thi() { return ca_tiet_thi; }
    public String getNgay_thi() { return ngay_thi; }
    public String getPhong_thi() { return phong_thi; }
}