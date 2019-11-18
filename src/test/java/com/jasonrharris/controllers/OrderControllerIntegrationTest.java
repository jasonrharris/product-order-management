package com.jasonrharris.controllers;

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
import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private MockMvc mockMvc;

    private Product savedProduct1;
    private Price savedPrice1;
    private Product savedProduct2;
    private Price savedPrice2;
    private ObjectMapper mapper;

    private BigDecimalCloseTo amountMatcher(BigDecimal expectedTotalAmount) {
        return new BigDecimalCloseTo(expectedTotalAmount, new BigDecimal("0.001"));
    }

    ;

    @Before
    public void setUp() {
        Product product1 = new Product("Product 1");
        savedProduct1 = productRepository.saveAndFlush(product1);
        Price price1 = Price.createPrice(product1, "20.50", "GBP");
        savedPrice1 = priceRepository.saveAndFlush(price1);

        Product product2 = new Product("Product 2");
        savedProduct2 = productRepository.saveAndFlush(product2);
        Price price2 = Price.createPrice(savedProduct2, "40.50", "GBP");
        savedPrice2 = priceRepository.saveAndFlush(price2);
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    }

    @Test
    public void shouldGetOrdersBetweenDates() throws Exception {

        LocalDateTime beforeEarliestOrderDateTime = LocalDateTime.of(2018, 10, 5, 2, 30);
        Order order0 = new Order(0L, beforeEarliestOrderDateTime, "buyer@gamil.com", Collections.emptySet());

        LocalDateTime earliestOrderDateTime = LocalDateTime.of(2018, 12, 5, 2, 30);
        Order order1 = new Order(0L, earliestOrderDateTime, "buyer@gamil.com", Collections.emptySet());

        LocalDateTime latestOrderDateTime = LocalDateTime.of(2019, 2, 5, 2, 30);
        Order order2 = new Order(0L, latestOrderDateTime, "buyer@gamil.com", Collections.emptySet());

        LocalDateTime afterLatestOrderDateTime = LocalDateTime.of(2019, 3, 5, 2, 30);
        Order order3 = new Order(0L, afterLatestOrderDateTime, "buyer@gamil.com", Collections.emptySet());

        List<Order> savedOrders = orderRepository.saveAll(Arrays.asList(order0, order1, order2, order3));

        String endDate = latestOrderDateTime.plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE_TIME);

        MvcResult afterEarlyDateOrdersResult = this.mockMvc.perform(get("/orders").
                param("after", earliestOrderDateTime.plus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE_TIME)).
                param("before", endDate)).
                andDo(print()).andExpect(status().isOk()).andReturn();

        List<Order> afterEarliestDateOrders = mapper.readValue(afterEarlyDateOrdersResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        Assert.assertFalse(afterEarliestDateOrders.isEmpty());

        for(Order order : afterEarliestDateOrders){
            if (order.getCreationDateTime().isEqual(earliestOrderDateTime) || order.getCreationDateTime().isBefore(earliestOrderDateTime)){
                throw new AssertionError("An Order with a date that is too early is in the search results");
            }
            checkForDateThatIsTooLate(latestOrderDateTime, afterLatestOrderDateTime, order);
        }

        MvcResult beforeEarlyDateOrdersResult = this.mockMvc.perform(get("/orders").
                param("after", earliestOrderDateTime.minus(1, ChronoUnit.DAYS).format(DateTimeFormatter.ISO_DATE_TIME)).
                param("before", endDate)).
                andDo(print()).andExpect(status().isOk()).andReturn();


        List<Order> beforeEarliestDateOrders = mapper.readValue(beforeEarlyDateOrdersResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        Assert.assertTrue(beforeEarliestDateOrders.size() > afterEarliestDateOrders.size());

        boolean earliestDateFound = false;
        for(Order order : beforeEarliestDateOrders){
            if (order.getCreationDateTime().isEqual(earliestOrderDateTime)){
                earliestDateFound = true;
            }
            checkForDateThatIsTooLate(latestOrderDateTime, afterLatestOrderDateTime, order);
        }

        Assert.assertTrue(earliestDateFound);
    }

    @Test
    public void shouldCreateOrder() throws Exception {

        int quantityOfItem1 = 2;
        int quantityOfItem2 = 1;
        OrderItem orderItem = new OrderItem(0L, savedPrice1, savedProduct1, quantityOfItem1, null);
        OrderItem orderItem2 = new OrderItem(0L, savedPrice2, savedProduct2, quantityOfItem2, null);

        Set<OrderItem> orderItems1 = new HashSet<>();
        orderItems1.add(orderItem);
        orderItems1.add(orderItem2);

        String buyersEmail = "buyer@gamil.com";
        Order order1 = new Order(0L, LocalDateTime.now(), buyersEmail,
                orderItems1);

        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String orderJson = ow.writeValueAsString(order1);

        BigDecimal orderItem1Amount = savedPrice1.getAmount().multiply(new BigDecimal(quantityOfItem1));
        BigDecimal orderItem2Amount = savedPrice2.getAmount().multiply(new BigDecimal(quantityOfItem2));

        BigDecimal expectedTotalAmount = orderItem1Amount.add(orderItem2Amount);

        this.mockMvc.perform(post("/orders").contentType(APPLICATION_JSON)
                .content(orderJson)).andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$.buyersEmail").value(buyersEmail))
                .andExpect(jsonPath("$.orderItems").isArray()).andExpect(jsonPath("$.orderItems.length()").value(2))
                .andExpect(jsonPath("$.totalAmount").value(amountMatcher(expectedTotalAmount), BigDecimal.class));
    }

    @Test
    public void shouldCreateOrderFromMinimalJSONAndReturnPopulatedOrderData() throws Exception {
        String newOrderJSON = "{\n" +
                "  \"buyersEmail\": \"by@now.com\",\n" +
                "  \"orderItems\": [\n" +
                "    {\n" +
                "      \"price\": {\"id\":" + savedPrice1.getId() + "},\n" +
                "      \"product\": {\"id\": " + savedProduct1.getId() + "},\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        MvcResult mvcResult = this.mockMvc.perform(post("/orders").contentType(APPLICATION_JSON)
                .content(newOrderJSON)).andDo(print()).andExpect(status().isOk()).andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        Order savedOrder = mapper.readValue(contentAsString, Order.class);

        Assert.assertThat(savedOrder.getTotalAmount(), amountMatcher(savedPrice1.getAmount()));

        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());

        Assert.assertThat(retrievedOrder.orElseThrow().getTotalAmount(), amountMatcher(savedPrice1.getAmount()));


    }

    private void checkForDateThatIsTooLate(LocalDateTime latestOrderDateTime, LocalDateTime afterLatestOrderDateTime, Order order) {
        if (order.getCreationDateTime().isEqual(afterLatestOrderDateTime) || order.getCreationDateTime().isAfter(latestOrderDateTime)){
            throw new AssertionError("An Order with a date that is too late is in the search results");
        }
    }
}
