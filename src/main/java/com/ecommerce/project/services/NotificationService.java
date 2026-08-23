package com.ecommerce.project.services;

import com.ecommerce.project.enums.NotificationType;
import com.ecommerce.project.model.Notification;
import com.ecommerce.project.model.User;

import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications();

    void createNotification(String message, NotificationType type);

    void createNotification(String message, NotificationType type, User user);

    void markAllAsRead();

    void deleteNotification(Long id);
}
