package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @NotBlank
    @Size(min= 5, message = "Street name must contain at least 5 characters")
    private String street;



    @NotBlank
    @Size(min= 3, message = "city name must contain at least 3 characters")
    private String city;

    @NotBlank
    @Size(min= 2, message = "State name must contain at least 2 characters")
    private String state;

    @NotBlank
    @Size(min= 2, message = "Country name must contain at least 2 characters")
    private String country;

    @NotBlank
    @Size(min= 5, message = "Zipcode name must contain at least 5 characters")
    private String zipcode;


    @ManyToOne()
    @JoinColumn(name="user_id")
    private User user;

    public Address( String city, String country, String state, String street, User users) {
        this.city = city;
        this.country = country;
        this.state = state;
        this.street = street;
        this.user = user;
    }

}