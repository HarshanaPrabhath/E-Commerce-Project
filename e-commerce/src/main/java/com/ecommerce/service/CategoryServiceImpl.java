package com.ecommerce.service;

import com.ecommerce.exceptions.ApiException;
import com.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            throw new ApiException("No Category Created Till Now !!!");
        }
        return categoryRepository.findAll();
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory != null) {
            throw new ApiException(category.getCategoryName(), "Already Exists");
        }
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryId",categoryId));
        categoryRepository.delete(category);
        return "Category " + categoryId + " is deleted successfully";


    }

    @Override
    public void updateCategory(Category category, Long categoryId) {
       Category categoryTOUpdate =  categoryRepository.findById(categoryId)
               .orElseThrow(() ->  new ResourceNotFoundException("category","categoryId",categoryId));

       categoryTOUpdate.setCategoryName(category.getCategoryName());

       categoryRepository.save(categoryTOUpdate);

    }

}
