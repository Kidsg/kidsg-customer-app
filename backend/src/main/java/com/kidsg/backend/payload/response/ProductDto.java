package com.kidsg.backend.payload.response;

import java.util.HashMap;
import java.util.Map;

public class ProductDto {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String brand;
    private String categoryId;
    private String categoryName;
    private String imageUrl;
    private double price;
    private double mrp;
    private int discountPercent;
    private int stock;
    private String unit;
    private String gradeLevel;
    private boolean isFeatured;
    private boolean isActive;
    private Map<String, String> specs;

    public ProductDto() {
        this.specs = new HashMap<>();
    }

    public ProductDto(String id, String name, String slug, String description, String brand,
                      String categoryId, String categoryName, String imageUrl, double price, double mrp,
                      int discountPercent, int stock, String unit, String gradeLevel, boolean isFeatured,
                      boolean isActive, Map<String, String> specs) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.brand = brand;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.mrp = mrp;
        this.discountPercent = discountPercent;
        this.stock = stock;
        this.unit = unit;
        this.gradeLevel = gradeLevel;
        this.isFeatured = isFeatured;
        this.isActive = isActive;
        this.specs = specs != null ? specs : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Map<String, String> getSpecs() {
        return specs;
    }

    public void setSpecs(Map<String, String> specs) {
        this.specs = specs;
    }
}
