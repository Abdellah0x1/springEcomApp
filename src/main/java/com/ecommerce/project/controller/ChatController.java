package com.ecommerce.project.controller;


import com.ecommerce.project.payload.ConversationDTO;
import com.ecommerce.project.payload.MessageDTO;
import com.ecommerce.project.payload.SendMessageRequest;
import com.ecommerce.project.services.ConversationService;
import com.ecommerce.project.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {
    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;


    @Autowired
    private SimpMessagingTemplate simpMessageTemplate;


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request){
        MessageDTO messageDTO = messageService.sendMessage(request.getConversationId(), request.getContent());

        ConversationDTO conversationDTO = conversationService.getConversationById(request.getConversationId());

        String senderName = messageDTO.getSenderName();
        String recipientName = messageDTO.getSenderName().equals(conversationDTO.getSellerName()) ? conversationDTO.getCustomerName() :conversationDTO.getSellerName() ;


//        push to receipient on a private queue

        simpMessageTemplate.convertAndSendToUser(recipientName, "/queue/messages", messageDTO);


//        push back to send
        simpMessageTemplate.convertAndSendToUser(senderName, "/queue/messages", messageDTO);


    }

    @MessageMapping("/chat.read")
    public void markRead(@Payload Map<String, Long> payload){
        Long conversationId = payload.get("conversationId");
        messageService.markAsRead(conversationId);
    }




}
