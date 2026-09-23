package com.ecommerce.project.payload;


import com.ecommerce.project.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private Long conversationId;

    private Long sellerId;

    private String sellerName;

    private  Long customerId;

    private String customerName;

    private Long productId;

    private String productName;

    private Instant createdAt;

    private ConversationStatus status;

    private int unreadCount;

    private MessageDTO lastMessage;
}
