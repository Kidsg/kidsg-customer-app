package com.kidsg.backend.payload.response;

import java.util.ArrayList;
import java.util.List;

public class ProductsResponseDto {
    private List<ProductDto> products;
    private PaginationDto pagination;

    public ProductsResponseDto() {
        this.products = new ArrayList<>();
    }

    public ProductsResponseDto(List<ProductDto> products, PaginationDto pagination) {
        this.products = products;
        this.pagination = pagination;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }

    public static class PaginationDto {
        private int total;
        private int page;
        private int limit;
        private int totalPages;

        public PaginationDto() {
        }

        public PaginationDto(int total, int page, int limit, int totalPages) {
            this.total = total;
            this.page = page;
            this.limit = limit;
            this.totalPages = totalPages;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }
    }
}
