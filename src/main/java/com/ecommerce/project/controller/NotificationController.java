package com.ecommerce.project.controller;


import com.ecommerce.project.model.Notification;
import com.ecommerce.project.model.User;
import com.ecommerce.project.services.NotificationService;
import com.ecommerce.project.services.SseEmitterService;
import com.ecommerce.project.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(){
        return new ResponseEntity<>(notificationService.getUserNotifications(),HttpStatus.OK);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id){
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAllNotifications(){
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    //subscribe to sse
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(){
        User user = authUtils.loggedInUser();
       return  sseEmitterService.subscribe(user.getUserId());
    }
}
