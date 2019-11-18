package com.jasonrharris.controllers;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jasonrharris.orders.Order;
import com.jasonrharris.orders.OrderItem;
import com.jasonrharris.products.Price;
import com.jasonrharris.products.Product;
import com.jasonrharris.repositories.OrderItemRepository;
import com.jasonrharris.repositories.OrderRepository;
import com.jasonrharris.repositories.PriceRepository;
import com.jasonrharris.repositories.ProductRepository;
import org.hamcrest.number.BigDecimalCloseTo;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    private Product initialProductWithPrice;

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
    public void shouldBeAbleToUpdateCurrentPriceWithoutChangingExistingOrdersAmounts() throws Exception {

        Price initialPrice = initialProductWithPrice.getCurrentPrice().orElseThrow();
        OrderItem orderItem = new OrderItem(0L, initialPrice, initialProductWithPrice, 2, null);
        OrderItem savedOrderItems = orderItemRepository.save(orderItem);

        LocalDateTime orderDateTime = LocalDateTime.of(2018, 12, 5, 2, 30);

        Order order = new Order(0L, orderDateTime, "buyer@gamil.com", Collections.singleton(savedOrderItems));

        Order savedOrder = orderRepository.save(order);

        BigDecimal prePriceChangeAmount = orderRepository.findById(savedOrder.getId()).orElseThrow().getTotalAmount();

        BigDecimal newPriceAmount = initialPrice.getAmount().add(new BigDecimal("2.0"));
        Price newPrice = Price.createPrice(initialProductWithPrice, newPriceAmount, initialPrice.getCurrency());
        Product productWithNewPrice = new Product(initialProductWithPrice.getId(), initialProductWithPrice.getName(), newPrice);

        String prodJson = getProductAsJson(productWithNewPrice);

        this.mockMvc.perform(put("/products/" + initialProductWithPrice.getId()).contentType(APPLICATION_JSON)
                .content(prodJson)).andDo(print()).
                andExpect(status().isOk());

        Assert.assertEquals(prePriceChangeAmount, orderRepository.findById(order.getId()).orElseThrow().getTotalAmount());

    }

    @Test
    public void shouldUpdateAnExistingProductWhenNoPriceIsSuppliedAndNotChangeExistingPrice() throws Exception {

        String newProdName = "New Product Name";

        Product product = new Product(newProdName);

        String prodJson = getProductAsJSONWithNoPrice(product);

        //noinspection OptionalGetWithoutIsPresent - as checked in a test
        BigDecimal expectedPriceAmount = initialProductWithPrice.getCurrentPrice().get().getAmount();

        this.mockMvc.perform(put("/products/" + initialProductWithPrice.getId()).contentType(APPLICATION_JSON)
                .content(prodJson)).andDo(print()).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name").value(newProdName)).
                andExpect(jsonPath("$.id").value(initialProductWithPrice.getId())).
                andExpect(jsonPath("$.currentPrice.amount").value(new BigDecimalCloseTo(expectedPriceAmount, new BigDecimal("0.001")), BigDecimal.class));
    }

    private String getProductAsJson(Product productWithNewPrice) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(productWithNewPrice);
    }

    private String getProductAsJSONWithNoPrice(Product product) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Price.class, TempMixInToIgnorePrice.class);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(product);
    }
}
