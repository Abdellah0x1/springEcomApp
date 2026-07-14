package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.services.CategoryService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CategoryController {
    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<@NonNull CategoryResponse> getAllCategories(@RequestParam(name = "page", required = false, defaultValue = AppConstants.PAGE_NUMER) Integer page, @RequestParam(name="pageSize", required = false, defaultValue=AppConstants.PAGE_SIZE) Integer pageSize
    , @RequestParam(name="sortBy", required = false, defaultValue = AppConstants.SORT_BY) String sortBy, @RequestParam(name="sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder) {

        CategoryResponse categories  = categoryService.getAllCategories(page,pageSize,sortBy,sortOrder);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<String> createCategory(@Valid @RequestBody CategoryDTO category) {

        categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body("Category created successfully");
    }



    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
        CategoryDTO deletedCategoryDTO = categoryService.deleteCategory(categoryId);
        return  ResponseEntity.ok(deletedCategoryDTO);
    }


    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO category,@PathVariable Long categoryId) {
            CategoryDTO savedCategoryDTO = categoryService.updateCategory(category, categoryId);
            return  ResponseEntity.ok(savedCategoryDTO);

    }
}
