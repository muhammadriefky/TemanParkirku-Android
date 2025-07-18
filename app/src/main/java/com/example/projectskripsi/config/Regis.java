package com.example.projectskripsi.config;

public class Regis {
    private String nama;
    private String email;
    private String password;
    private String role;
    private String nomor_telepon;

    public Regis(String nama,String email, String password,String role,String nomor_telepon) {
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
        this.nomor_telepon = nomor_telepon;

    }

    // Getters and Setters
    public String getName() {
        return nama;
    }

    public void setName(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return role;
    }

    public void setAddress(String role) { this.role = role;}

    public String getTelp() {
        return nomor_telepon;
    }

    public void setTelp(String nomor_telepon) {
        this.nomor_telepon = nomor_telepon;
    }

}
