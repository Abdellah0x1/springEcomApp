package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM  Notification n WHERE n.user.userId = ?1")
    List<Notification> getNotificationByUserId(Long userId);
}
