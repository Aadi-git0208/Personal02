package com.medicurex.pharmacy.dto;

import java.util.List;

public class CategoryListResponse {

    private List<String> categories;

    public CategoryListResponse(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getCategories() {
        return categories;
    }
}
