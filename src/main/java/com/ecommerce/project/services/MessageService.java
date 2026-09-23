package com.ecommerce.project.services;

import com.ecommerce.project.payload.MessageDTO;

import java.util.List;

public interface MessageService {
    MessageDTO sendMessage(Long conversationId, String content);

    void markAsRead(Long conversationId);

    List<MessageDTO> getMessages(Long conversationId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
