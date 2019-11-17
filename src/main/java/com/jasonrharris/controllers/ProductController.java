package com.jasonrharris.controllers;

import com.jasonrharris.products.Price;
import com.jasonrharris.products.PriceManagement;
import com.jasonrharris.products.Product;

import com.jasonrharris.repositories.ProductRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@SwaggerDefinition(
        info = @Info(
                description = "Used to manage Available Products",
                version = "1.0.0-SNAPSHOT",
                title = "The Product Manager"
        ),
        consumes = {"application/json"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP}
)

public class ProductController {

    private final ProductRepository productRepository;
    private final PriceManagement priceManagement;

    public ProductController(@Autowired ProductRepository productRepository, @Autowired PriceManagement priceManagement) {
        this.productRepository = productRepository;
        this.priceManagement = priceManagement;
    }

    /**
     * GET a list of all products
     *
     * @return all products are returned in a list
     */
    @ApiOperation(value = "Displays a list of all available products", response = List.class)
    @GetMapping("/products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    /**
     * Create a new product
     */
    @ApiOperation(value = "Adds a new Product", response = List.class)
    @PostMapping("/products")
    public Product addProduct(@RequestBody Product newProduct) {
        Optional<Price> newPrice = newProduct.getCurrentPrice();
        Product savedProduct = productRepository.save(new Product(newProduct.getName()));
        Optional<Price> price = priceManagement.saveNewProductPrice(new Product(savedProduct.getId(), savedProduct.getName(), newPrice.orElse(null)));
        if (price.isEmpty()) {
            return savedProduct;
        } else {
            return new Product(savedProduct.getId(), savedProduct.getName(), price.get());
        }
    }

    /**
     * Updates an existing product
     */
    @ApiOperation(value = "Updates an existing product", response = List.class)
    @PutMapping("/products/{id}")
    public Product updateProduct(@RequestBody Product updatedProduct, @ApiParam(value = "The ID of the product to be updated", required = true, example = "1") @PathVariable long id) {
        Product matchingProduct = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No product has Id " + id));

        final Price priceToAddToProduct = priceManagement.getExistingOrSaveUpdatedPrice(updatedProduct, matchingProduct);

        return productRepository.save(new Product(id, updatedProduct.getName(), priceToAddToProduct));
    }

}
