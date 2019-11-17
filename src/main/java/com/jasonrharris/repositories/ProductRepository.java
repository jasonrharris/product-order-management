package com.jasonrharris.repositories;

import com.jasonrharris.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
