package com.ecommerce.project.services;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Conversation;
import com.ecommerce.project.model.Message;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ConversationDTO;
import com.ecommerce.project.payload.MessageDTO;
import com.ecommerce.project.repositories.ConversationRepository;
import com.ecommerce.project.repositories.MessageRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private MessageRepository messageRepository;



    private MessageDTO mapToMessageDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getUserId());
        dto.setSenderName(message.getSender().getUserName());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setRead(message.isRead());
        return dto;
    }


    private ConversationDTO mapToConversationDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversation.getId());
        dto.setSellerId(conversation.getSeller().getUserId());
        dto.setSellerName(conversation.getSeller().getUserName());
        dto.setCustomerId(conversation.getCustomer().getUserId());
        dto.setCustomerName(conversation.getCustomer().getUserName());
        dto.setProductId(conversation.getProduct().getProductId());
        dto.setProductName(conversation.getProduct().getProductName());
        dto.setStatus(conversation.getStatus());

        dto.setCreatedAt(conversation.getCreatedAt());


        Message lastMsg = messageRepository.findTopByConversationIdOrderByCreatedAtDesc(conversation.getId());
        if (lastMsg != null) {
            dto.setLastMessage(mapToMessageDTO(lastMsg));
        }
        int unread = messageRepository.countByConversationIdAndSenderUserIdNotAndReadFalse(
                conversation.getId(), currentUserId
        );
        dto.setUnreadCount(unread);
        return dto;
    }



    public ConversationDTO createConversation(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        User customer = userRepository.findById(authUtils.loggedInUserId()).orElseThrow(()-> new ResourceNotFoundException("User", "id", authUtils.loggedInUserId()));

        Optional<Conversation> conversationDB = conversationRepository.findByCustomerUserIdAndSellerUserIdAndProductProductId(customer.getUserId(), product.getUser().getUserId(),productId);

        if(conversationDB.isPresent()){
            throw new APIException("Conversation already exists");
        }

        Conversation conversation = new Conversation();
        conversation.setProduct(product);
        conversation.setCustomer(customer);
        conversation.setSeller(product.getUser());

        Conversation savedConversation = conversationRepository.save(conversation);
        return mapToConversationDTO(savedConversation, authUtils.loggedInUserId());
    }

    public ConversationDTO getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Conversation", "id", id));

        if(!conversation.getCustomer().getUserId().equals(authUtils.loggedInUserId()) && !conversation.getSeller().getUserId().equals(authUtils.loggedInUserId())){
            throw new APIException("You're not a participant in this conversation");
        }

        return mapToConversationDTO(conversation, authUtils.loggedInUserId());

    }

    public List<ConversationDTO> getUserConversations(Long userId, Integer pageNumber, Integer pageSize, String sortBy,String sortOrder){
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Conversation>  conversationsPafge = conversationRepository.findByCustomerUserIdOrSellerUserId(userId, userId,pageable);
        List<Conversation> conversations = conversationsPafge.getContent();

        if(conversations.isEmpty()){
            throw new APIException("No conversations found");
        }
        return conversations.stream().map(conversation -> mapToConversationDTO(conversation,userId)).toList();
    }

    
}
