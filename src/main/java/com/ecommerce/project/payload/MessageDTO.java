package com.ecommerce.project.payload;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long messageId;

    private Long conversationId;

    private Long senderId;

    private String senderName;

    private String content;

    private boolean read;

    private Instant createdAt;

}
