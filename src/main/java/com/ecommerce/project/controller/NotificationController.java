package com.ecommerce.project.controller;


import com.ecommerce.project.model.Notification;
import com.ecommerce.project.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(){
        return new ResponseEntity<>(notificationService.getUserNotifications(),HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<Void> markAllAsRead(){
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
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
}
