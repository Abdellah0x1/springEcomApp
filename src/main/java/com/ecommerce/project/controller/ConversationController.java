package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ConversationDTO;
import com.ecommerce.project.payload.CreateConversationRequest;
import com.ecommerce.project.payload.MessageDTO;
import com.ecommerce.project.services.ConversationService;
import com.ecommerce.project.services.MessageService;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private MessageService messageService;


    @GetMapping()
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@RequestParam(name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
                                                   @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                   @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                   @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder) {
        List<ConversationDTO> conversations =  conversationService.getUserConversations(authUtils.loggedInUserId(),pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(conversations, HttpStatus.OK);
    }




    @PostMapping()
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody CreateConversationRequest createConversationRequest) {
        ConversationDTO conversationDTO = conversationService.createConversation(createConversationRequest.getProductId());
        return new ResponseEntity<>(conversationDTO, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDTO> getUserConversation(@PathVariable Long conversationId){
        return new ResponseEntity<>(conversationService.getConversationById(conversationId),HttpStatus.OK);
    }


    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(@PathVariable Long id, @RequestParam(name="page" , required = false, defaultValue = AppConstants.PAGE_NUMER) Integer pageNumber,
                                                                    @RequestParam (name="size" , required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                    @RequestParam (name="sortBy" , required = false, defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                    @RequestParam (name="sortOrder" , required = false, defaultValue = AppConstants.SORT_ORDER) String sortOrder){
        List<MessageDTO> messageDTOS = messageService.getMessages(id,  pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(messageDTOS, HttpStatus.OK);
    }


}
