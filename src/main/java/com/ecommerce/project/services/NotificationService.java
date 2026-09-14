package com.ecommerce.project.services;

import com.ecommerce.project.enums.NotificationType;
import com.ecommerce.project.model.Notification;
import com.ecommerce.project.model.User;

import java.io.IOException;
import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications();

    void createNotification(String message, NotificationType type) throws IOException;

    void createNotification(String message, NotificationType type, User user) throws IOException;

    void markAllAsRead();

    void deleteNotification(Long id);
}
