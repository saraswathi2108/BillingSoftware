package com.billing.software.controller;

import com.billing.software.dto.CategoryDTO;
import com.billing.software.entity.Category;
import com.billing.software.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/billing/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<CategoryDTO> create(@RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> all(
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(categoryService.getAllCategories(onlyActive));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(
            @PathVariable Long id,
            @RequestBody CategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }
}

