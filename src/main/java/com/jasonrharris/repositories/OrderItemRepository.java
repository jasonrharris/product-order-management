package com.jasonrharris.repositories;

import com.jasonrharris.orders.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
