package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM  Notification n WHERE n.user.userId = ?1")
    List<Notification> getNotificationByUserId(Long userId);


    @Modifying @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = ?1")
    void markAsRead(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.id = ?1 AND n.user.userId = ?2")
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
