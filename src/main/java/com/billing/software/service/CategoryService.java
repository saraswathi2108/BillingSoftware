package com.billing.software.service;

import com.billing.software.dto.CategoryDTO;
import com.billing.software.entity.Category;
import com.billing.software.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final ModelMapper modelMapper;

    public CategoryDTO createCategory(CategoryDTO dto) {

        if (categoryRepo.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Category already exists: " + dto.getName());
        }

        Category category = modelMapper.map(dto, Category.class);

        if (category.getActive() == null)
            category.setActive(true);

        Category saved = categoryRepo.save(category);
        return modelMapper.map(saved, CategoryDTO.class);
    }


    public List<CategoryDTO> getAllCategories(boolean onlyActive) {
        return categoryRepo.findAll().stream()
                .filter(c -> !onlyActive || Boolean.TRUE.equals(c.getActive()))
                .map(c -> modelMapper.map(c, CategoryDTO.class))
                .toList();
    }


    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {

        Category existing = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        if (dto.getName() != null)
            existing.setName(dto.getName());

        if (dto.getDefaultHsn() != null)
            existing.setDefaultHsn(dto.getDefaultHsn());

        if (dto.getDefaultGst() != null)
            existing.setDefaultGst(dto.getDefaultGst());

        if (dto.getActive() != null)
            existing.setActive(dto.getActive());

        Category updated = categoryRepo.save(existing);
        return modelMapper.map(updated, CategoryDTO.class);
    }
}
