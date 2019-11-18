package com.jasonrharris.controllers;

import com.jasonrharris.orders.Order;
import com.jasonrharris.orders.OrderItem;
import com.jasonrharris.products.Price;
import com.jasonrharris.repositories.OrderItemRepository;
import com.jasonrharris.repositories.OrderRepository;
import com.jasonrharris.repositories.PriceRepository;
import com.jasonrharris.repositories.ProductRepository;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@SwaggerDefinition(
        info = @Info(
                description = "Used to create and retrieve Orders",
                version = "1.0.0-SNAPSHOT",
                title = "The Order Manager"
        ),
        consumes = {"application/json"},
        produces = {"application/json"},
        schemes = {SwaggerDefinition.Scheme.HTTP}
)
public class OrderController {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PriceRepository priceRepository;

    public OrderController(@Autowired OrderRepository orderRepository, @Autowired OrderItemRepository orderItemRepository, @Autowired PriceRepository priceRepository, @Autowired ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.priceRepository = priceRepository;
    }

    /**
     * GET a list of all orders between 2 dates
     *
     * @return all products are returned in a list
     */
    @ApiOperation(value = "Displays a list of all order made between the two dates", response = List.class)
    @GetMapping("/orders")
    public List<Order> getProducts(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before) {
        return orderRepository.findAllByCreationDateTimeAfterAndCreationDateTimeBefore(after, before);
    }

    @ApiOperation(value = "Adds a new Order", response = Order.class)
    @ApiImplicitParams(
            @ApiImplicitParam(
                    name = "newOrder",
                    dataType = "OrderBodyType"
            )
    )
    @PostMapping("/orders")
    public Order addOrder(@RequestBody Order newOrder) {
        Order savedOrder = orderRepository.save(new Order(0L, LocalDateTime.now(), newOrder.getBuyersEmail(), new HashSet<>()));

        List<OrderItem> unsetOrderItems = newOrder.getOrderItems().stream().filter(OrderItem::isUnset).collect(Collectors.toList());

        Map<Long, Price> retrievedPrices = priceRepository.findAllById(unsetOrderItems.stream().map(orderItem -> orderItem.getPrice().getId()).collect(Collectors.toList())).stream().collect(Collectors.toMap(Price::getId, price -> price));

        List<OrderItem> newOrderItems = newOrder.getOrderItems().stream().map(orderItem -> new OrderItem(
                orderItem.getPrice().isUnset() ? retrievedPrices.get(orderItem.getPrice().getId()) : orderItem.getPrice(),
                orderItem.getProduct(),
                orderItem.getQuantity(), savedOrder)).collect(Collectors.toList());

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(newOrderItems);

        return new Order(savedOrder.getId(), savedOrder.getCreationDateTime(), savedOrder.getBuyersEmail(), new HashSet<>(savedOrderItems));
    }

    /*
    Required by Swagger to document to `addOrder` POST
     */
    @SuppressWarnings("unused") // getters and setters needed for Swagger
    public static class OrderBodyType {

        @ApiModelProperty(example = "buyer@customer.com")
        private String buyersEmail;

        @ApiModelProperty(dataType = "OrderItemType", name = "orderItems", example = "[{\"price\":{\"id\":5},\"product\":{\"id\":3},\"quantity\":1}]")
        private List<OrderItem> orderItems;

        public String getBuyersEmail() {
            return buyersEmail;
        }

        public void setBuyersEmail(String buyersEmail) {
            this.buyersEmail = buyersEmail;
        }

        public List<OrderItem> getOrderItems() {
            return orderItems;
        }

        public void setOrderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
        }
    }
}
