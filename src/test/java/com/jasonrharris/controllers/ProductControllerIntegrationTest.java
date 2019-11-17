package com.jasonrharris.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jasonrharris.products.Price;
import com.jasonrharris.products.Product;
import com.jasonrharris.repositories.PriceRepository;
import com.jasonrharris.repositories.ProductRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTest {

    private static final String INITIAL_PRODUCT_NAME = "InitialProduct";

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PriceRepository priceRepository;

    private Product initialProductWithPrice;

    @Autowired
    private MockMvc mockMvc;

    @org.junit.Before
    public void setUp() {
        Product initialProduct = productRepository.saveAndFlush(new Product(INITIAL_PRODUCT_NAME));
        priceRepository.save(Price.createPrice(initialProduct, "20.20", "GBP"));
        initialProductWithPrice = productRepository.findById(initialProduct.getId()).orElseThrow(() -> new AssertionError("Initial Product ought to be persisted"));
    }

    @Test
    public void shouldDetermineThatInitialProductHasAPrice() {
        Assert.assertTrue(initialProductWithPrice.getCurrentPrice().isPresent());
    }

    @Test
    public void shouldReturnExistingProducts() throws Exception {
        //as order of tests can't be controlled, the expected number of products is variable
        List<Product> all = productRepository.findAll();
        int expectedNumberOfProducts = all.size();

        ResultActions resultActions = this.mockMvc.perform(get("/products")).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(expectedNumberOfProducts));

        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

        List<Product> newProduct = mapper.readValue(contentAsString, new TypeReference<>() {
        });

        Optional<Product> optInitialProduct = newProduct.stream().filter(product -> product.getId() == this.initialProductWithPrice.getId()).findFirst();

        if (optInitialProduct.isEmpty()) {
            throw new AssertionError("Initial Product should be in list");
        } else {
            Product initialProductFromList = optInitialProduct.get();
            Assert.assertEquals(initialProductFromList, initialProductWithPrice);
        }
    }

    @Test
    public void shouldCreateANewProduct() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String newProdName = "New Product";
        String prodJson = ow.writeValueAsString(new Product(newProdName));

        ResultActions resultActions = this.mockMvc.perform(post("/products").contentType(APPLICATION_JSON)
                .content(prodJson)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newProdName));

        MvcResult result = resultActions.andReturn();
        String contentAsString = result.getResponse().getContentAsString();

        Product newProduct = mapper.readValue(contentAsString, Product.class);

        Assert.assertTrue("New products Id cannot be zero", newProduct.getId() != 0L);

        try {
            productRepository.getOne(newProduct.getId());
        } catch (EntityNotFoundException nfe) {
            throw new AssertionError(newProduct + " has not be persisted");
        }

    }

    @JsonIgnoreType
    private static class TempMixInToIgnorePrice {
    }

    @Test
    public void shouldUpdateAnExistingProductWithANewPrice() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String newProdName = "New Product Name";
        String prodJson = ow.writeValueAsString(new Product(newProdName));

        this.mockMvc.perform(put("/products/" + initialProductWithPrice.getId()).contentType(APPLICATION_JSON)
                .content(prodJson)).andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name").value(newProdName)).
                andExpect(jsonPath("$.id").value(initialProductWithPrice.getId()));
    }

    @Test
    public void shouldUpdateAnExistingProductWhenNoPriceIsSupplied() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Price.class, TempMixInToIgnorePrice.class);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String newProdName = "New Product Name";
        String prodJson = ow.writeValueAsString(new Product(newProdName));

        this.mockMvc.perform(put("/products/" + initialProductWithPrice.getId()).contentType(APPLICATION_JSON)
                .content(prodJson)).andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name").value(newProdName)).
                andExpect(jsonPath("$.id").value(initialProductWithPrice.getId()));
    }
}
