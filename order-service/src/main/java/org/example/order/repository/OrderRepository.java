package org.example.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.order.entity.OrderEntity;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
