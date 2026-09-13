package com.ecommerce.project.services;

import com.ecommerce.project.enums.NotificationType;
import com.ecommerce.project.model.Notification;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.NotificationRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.utils.AuthUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private AuthUtils authUtils;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private NotificationRepository  notificationRepository;

    @Override
    public List<Notification> getUserNotifications() {
        User user = authUtils.loggedInUser();
        return notificationRepository.getNotificationByUserId(user.getUserId());
    }


    @Override
    public void createNotification(String message, NotificationType type){
        Notification notification = new Notification();
        User user = authUtils.loggedInUser();

        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setType(type);
        notification.setUser(user);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Override
    public void createNotification(String message, NotificationType type, User user){
        Notification notification = new Notification();

        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setType(type);
        notification.setUser(user);
        notification.setIsRead(false);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = authUtils.loggedInUser();
        notificationRepository.markAsRead(user.getUserId());
    }

    
    @Override
    public void deleteNotification(Long id){
        notificationRepository.deleteById(id);
    }

}
