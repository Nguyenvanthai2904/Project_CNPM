package com.example.moneymate;

public class Expense {
    private String date;
    private String note;
    private int amount;
    private String category;

    // Constructor
    public Expense(String date, String note, int amount, String category) {
        this.date = date;
        this.note = note;
        this.amount = amount;
        this.category = category;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
