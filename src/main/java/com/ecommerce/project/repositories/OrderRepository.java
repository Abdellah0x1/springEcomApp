package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query("""
        SELECT DISTINCT o
        FROM Order o 
        JOIN o.orderItems oi 
        JOIN oi.product p 
        WHERE p.user.userId = ?1 
        ORDER BY o.orderDate DESC 
    """)
    List<Order> findOrdersBySellerId(Long id);

}
