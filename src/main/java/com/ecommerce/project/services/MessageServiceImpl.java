package com.ecommerce.project.services;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Conversation;
import com.ecommerce.project.model.Message;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.MessageDTO;
import com.ecommerce.project.repositories.ConversationRepository;
import com.ecommerce.project.repositories.MessageRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private UserRepository userRepository;



    private MessageDTO mapToMessageDTO(Message message){
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setMessageId(message.getId());
        messageDTO.setRead(message.isRead());
        messageDTO.setCreatedAt(message.getCreatedAt());
        messageDTO.setContent(message.getContent());
        messageDTO.setSenderId(message.getSender().getUserId());
        messageDTO.setSenderName(message.getSender().getUserName());
        messageDTO.setConversationId(message.getConversation().getId());

        return  messageDTO;
    }

    @Override
    public List<MessageDTO> getMessages(Long conversationId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Message> messagesPage = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId,pageable);

        if(messagesPage.getContent().isEmpty()){
            throw new APIException("No messages found");
        }

        List<Message> messages = messagesPage.getContent();
        return messages.stream().map(this::mapToMessageDTO).toList();
    }


    @Override
    public void markAsRead(Long conversationId) {
        messageRepository.markMessagesAsRead(conversationId, authUtils.loggedInUserId());
    }


    @Override
    public MessageDTO sendMessage(Long conversationId, String content){
        User sender = userRepository.findById(authUtils.loggedInUserId()).orElseThrow(()-> new ResourceNotFoundException("User", "id", authUtils.loggedInUserId()));

        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(()-> new ResourceNotFoundException("Conversation", "id", conversationId));

        if(!conversation.getSeller().getUserId().equals(sender.getUserId()) && !conversation.getCustomer().getUserId().equals(sender.getUserId())){
            throw new APIException("You are not a participant in this conversation");
        }
        
        Message message = new Message();

        message.setSender(sender);
        message.setContent(content);
        message.setRead(false);

        message.setConversation(conversation);

        messageRepository.save(message);

        return  mapToMessageDTO(message);
    }



}
