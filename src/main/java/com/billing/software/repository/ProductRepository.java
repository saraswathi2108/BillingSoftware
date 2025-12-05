package com.billing.software.repository;


import com.billing.software.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    boolean existsByBarcode(String barcode);

    Optional<Product> findByBarcode(String barcode);


}
