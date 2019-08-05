package com.dj.travelmantics;

class Database {
    private String name;
    private String city;
    private String amount;
    private String place;
    private String desc;
    private String _id;
    private String role;
    private String photo_url;


    public Database() {
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String get_id() {
        return _id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public Database(String city, String amount, String place, String desc, String photo_url) {
        this.city = city;
        this.amount = amount;
        this.place = place;
        this.desc = desc;
        this.photo_url = photo_url;
    }

    public Database(String name, String _id, String role) {
        this.name = name;
        this._id = _id;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
