package com.ecommerce.service;

import com.ecommerce.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getCategories();
    void createCategory(Category category);
    String deleteCategory(Long categoryId);
    void updateCategory(Category category,Long categoryId);


}
