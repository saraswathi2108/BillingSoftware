package com.billing.software.service;

import com.billing.software.dto.ProductDTO;
import com.billing.software.entity.DiscountType;
import com.billing.software.entity.Product;
import com.billing.software.repository.CategoryRepository;
import com.billing.software.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;


    public ProductDTO createProduct(ProductDTO dto) {

        if (productRepository.existsById(dto.getId()))
            throw new RuntimeException("Product ID already exists: " + dto.getId());

        if (productRepository.existsByBarcode(dto.getBarcode()))
            throw new RuntimeException("Barcode already used: " + dto.getBarcode());

        Product product = modelMapper.map(dto, Product.class);

        if (dto.getCategoryId() != null) {
            var category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategoryId()));

            product.setCategory(category);

            product.setHsnCode(category.getDefaultHsn());
            product.setGstPercent(category.getDefaultGst());
        }

        if (product.getDiscountType() == null)
            product.setDiscountType(DiscountType.NONE);

        if (product.getDiscountValue() == null)
            product.setDiscountValue(BigDecimal.ZERO);

        if (product.getActive() == null)
            product.setActive(true);

        if (product.getStockQty() == null)
            product.setStockQty(0);

        Product saved = productRepository.save(product);
        return modelMapper.map(saved, ProductDTO.class);
    }


    public ProductDTO updateProduct(String id, ProductDTO dto) {

        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getBarcode() != null) existing.setBarcode(dto.getBarcode());
        if (dto.getPrice() != null) existing.setPrice(dto.getPrice());

        if (dto.getCategoryId() != null) {
            var category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + dto.getCategoryId()));
            existing.setCategory(category);

            existing.setHsnCode(category.getDefaultHsn());
            existing.setGstPercent(category.getDefaultGst());
        }

        if (dto.getDiscountType() != null) existing.setDiscountType(dto.getDiscountType());
        if (dto.getDiscountValue() != null) existing.setDiscountValue(dto.getDiscountValue());

        if (dto.getStockQty() != null) existing.setStockQty(dto.getStockQty());
        if (dto.getActive() != null) existing.setActive(dto.getActive());

        Product saved = productRepository.save(existing);
        return modelMapper.map(saved, ProductDTO.class);
    }

    public ProductDTO getByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + barcode));
        return modelMapper.map(product, ProductDTO.class);
    }


    public void activate(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (Boolean.TRUE.equals(product.getActive()))
            throw new RuntimeException("Product is already active");

        product.setActive(true);
        productRepository.save(product);
    }

    public void deactivate(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (Boolean.FALSE.equals(product.getActive()))
            throw new RuntimeException("Product is already inactive");

        product.setActive(false);
        productRepository.save(product);
    }


    public ProductDTO addStock(String id, Integer qty) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        product.setStockQty(product.getStockQty() + qty);
        if (product.getStockQty() > 0) {
            product.setActive(true);
        }

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }

    public ProductDTO reduceStock(String id, Integer qty) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if (product.getStockQty() < qty)
            throw new RuntimeException("Insufficient stock!");
        product.setStockQty(product.getStockQty() - qty);
        if (product.getStockQty() == 0) {
            product.setActive(true);
        }

        return modelMapper.map(productRepository.save(product), ProductDTO.class);
    }


    public List<ProductDTO> getAllProducts(boolean onlyActive) {
        return productRepository.findAll().stream()
                .filter(p -> !onlyActive || Boolean.TRUE.equals(p.getActive()))
                .map(p -> modelMapper.map(p, ProductDTO.class))
                .collect(Collectors.toList());
    }
}
