package code.model;

import java.util.HashMap;

public class Product{
    private String productName;
    private double price;

    public Product(String productName, double price) {
        this.productName = productName;
        this.price = price;
    }

    // Getters and setters if needed
    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }
}

