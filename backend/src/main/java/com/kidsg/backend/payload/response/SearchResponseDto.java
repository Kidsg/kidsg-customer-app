package com.kidsg.backend.payload.response;

import java.util.ArrayList;
import java.util.List;

public class SearchResponseDto {
    private String query;
    private List<ProductDto> products;
    private List<CategoryDto> categories;

    public SearchResponseDto() {
        this.products = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    public SearchResponseDto(String query, List<ProductDto> products, List<CategoryDto> categories) {
        this.query = query;
        this.products = products != null ? products : new ArrayList<>();
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }
}
