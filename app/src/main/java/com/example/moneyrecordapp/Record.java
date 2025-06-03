package com.example.moneyrecordapp;

public class Record {
    private int id;
    private double amount;
    private String type;
    private String description;
    private String date;
    public Record() {
    }
    public Record(int id, double amount, String type, String description, String date) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.date = date;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
