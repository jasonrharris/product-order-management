package com.jasonrharris.repositories;

import com.jasonrharris.products.Price;
import com.jasonrharris.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<Price, Long> {
    Price findTopByProductOrderByCreationDateTime(Product product);
}

