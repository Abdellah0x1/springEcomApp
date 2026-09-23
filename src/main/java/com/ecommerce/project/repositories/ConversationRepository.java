package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByCustomerIdOrSellerId(Long customerId, Long sellerId, Pageable pageable);

    Optional<Conversation> findByCustomerIdAndSellerIdAndProductProductId(
            Long customerId, Long sellerId, Long productId
    );
}
