package com.ecommerce.service;

import com.ecommerce.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getCategories();
    void createCategory(Category category);

}
