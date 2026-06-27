package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class GradeSubject {
    @SerializedName("stt")
    private int stt;

    @SerializedName("ma_hp")
    private String maHp;

    @SerializedName("ten_hoc_phan")
    private String tenHocPhan;

    @SerializedName("tin_chi")
    private int tinChi;

    @SerializedName("diem_qt")
    private Double diemQt;

    @SerializedName("diem_gk")
    private Double diemGk;

    @SerializedName("diem_th")
    private Double diemTh;

    @SerializedName("diem_ck")
    private Double diemCk;

    @SerializedName("diem_hp")
    private String diemHp;

    @SerializedName("ghi_chu")
    private String ghiChu;

    public int getStt() { return stt; }
    public String getMaHp() { return maHp; }
    public String getTenHocPhan() { return tenHocPhan; }
    public int getTinChi() { return tinChi; }
    public Double getDiemQt() { return diemQt; }
    public Double getDiemGk() { return diemGk; }
    public Double getDiemTh() { return diemTh; }
    public Double getDiemCk() { return diemCk; }
    public String getDiemHp() { return diemHp; }
    public String getGhiChu() { return ghiChu; }
}