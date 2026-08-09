package com.ecommerce.project.services;

import com.ecommerce.project.enums.NotificationType;
import com.ecommerce.project.model.Notification;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.NotificationRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.utils.AuthUtils;
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
        List<Notification> userNotifications = notificationRepository.getNotificationByUserId(user.getUserId());
        return userNotifications;
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
    public void markAllAsRead() {
        User user = authUtils.loggedInUser();
        List<Notification> userNotifications = notificationRepository.getNotificationByUserId(user.getUserId());

        userNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(userNotifications);
    }

    @Override
    public void deleteNotification(Long id){
        notificationRepository.deleteById(id);
    }

}
