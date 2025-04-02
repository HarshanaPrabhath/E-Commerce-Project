package com.ecommerce.controller;

import com.ecommerce.model.Category;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.CategoryServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CategoryController {

    CategoryService categories = new CategoryServiceImpl();

    @GetMapping("/api/public/categories")
    public List<Category> getAllCategories(){
        return categories.getCategories();

    }

    @PostMapping("/api/public/categories")
    public void setCategories(@RequestBody Category category){
        categories.createCategory(category);
    }
}
