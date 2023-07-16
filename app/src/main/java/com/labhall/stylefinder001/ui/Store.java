package com.labhall.stylefinder001.ui;

public class Store {
    private String name;
    private String style;
    private int price;
    private String address;
    private int score;

    public Store(String name, String style, int price, String address) {
        this.name = name;
        this.style = style;
        this.price = price;
        this.address = address;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
