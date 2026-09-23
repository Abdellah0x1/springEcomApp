package com.ecommerce.project.model;


import com.ecommerce.project.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Conversation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User seller;

    @ManyToOne
    private User customer;

    @ManyToOne
    private Product product;

    @Enumerated(EnumType.STRING)
    private ConversationStatus status =  ConversationStatus.OPEN;


    private Instant createdAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
