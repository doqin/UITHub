package com.example.uithub.models;

import com.google.gson.annotations.SerializedName;

public class StudentProfile {
    @SerializedName("ho_ten")
    private String hoTen;
    
    @SerializedName("mssv")
    private String mssv;
    
    @SerializedName("ngay_sinh")
    private String ngaySinh;
    
    @SerializedName("gioi_tinh")
    private String gioiTinh;
    
    @SerializedName("lop_sinh_hoat")
    private String lopSinhHoat;
    
    @SerializedName("khoa")
    private String khoa;
    
    @SerializedName("bac_dao_tao")
    private String bacDaoTao;
    
    @SerializedName("he_dao_tao")
    private String heDaoTao;
    
    @SerializedName("nganh")
    private String nganh;

    public String getHoTen() { return hoTen; }
    public String getMssv() { return mssv; }
    public String getNgaySinh() { return ngaySinh; }
    public String getGioiTinh() { return gioiTinh; }
    public String getLopSinhHoat() { return lopSinhHoat; }
    public String getKhoa() { return khoa; }
    public String getBacDaoTao() { return bacDaoTao; }
    public String getHeDaoTao() { return heDaoTao; }
    public String getNganh() { return nganh; }
}