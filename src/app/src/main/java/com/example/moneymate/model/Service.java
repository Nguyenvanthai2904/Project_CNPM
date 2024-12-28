package com.example.moneymate.model;

public class Service {
    private int image;
    private String name;
    private boolean selected; // Thêm thuộc tính selected

    public Service(int image, String name) {
        this.image = image;
        this.name = name;
        this.selected = false; // Mặc định là false
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Thêm method setSelected
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    // Thêm method isSelected (nếu cần thiết)
    public boolean isSelected() {
        return selected;
    }
}