package com.billing.software.controller;


import com.billing.software.dto.ProductDTO;
import com.billing.software.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class ProductController {



    private final ProductService productService;

    @PostMapping("/create/product")
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO product) {
        ProductDTO productDTO=productService.createProduct(product);
        return new ResponseEntity<>(productDTO, HttpStatus.CREATED);
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable String id, @RequestBody ProductDTO productDTO) {
        ProductDTO productDTO1=productService.updateProduct(id, productDTO);
        return new ResponseEntity<>(productDTO1, HttpStatus.OK);

    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> findAll(@RequestParam(defaultValue = "false") boolean onlyActive) {
        List<ProductDTO> productDTOS=productService.getAllProducts(onlyActive);
        return new ResponseEntity<>(productDTOS, HttpStatus.OK);
    }

    @GetMapping("/product/barcode/{barcode}")
    public ResponseEntity<ProductDTO> findByBarcode(@PathVariable String barcode) {
        ProductDTO productDTO=productService.getByBarcode(barcode);
        return new ResponseEntity<>(productDTO,HttpStatus.OK);

    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable String id) {
        productService.activate(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        productService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stock/add/{qty}")
    public ResponseEntity<ProductDTO> addStock(
            @PathVariable String id,
            @PathVariable Integer qty) {
        return ResponseEntity.ok(productService.addStock(id, qty));
    }
    @PostMapping("/{id}/stock/reduce/{qty}")
    public ResponseEntity<ProductDTO> reduceStock(
            @PathVariable String id,
            @PathVariable Integer qty) {
        return ResponseEntity.ok(productService.reduceStock(id, qty));
    }




}
