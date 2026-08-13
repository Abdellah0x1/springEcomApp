package com.ecommerce.project.payload;

import com.ecommerce.project.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long addressId;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private List<User> users = new ArrayList<>();

    public AddressDTO( String city, String country, String state, String street, List<User> users) {
        this.city = city;
        this.country     = country;
        this.state = state;
        this.street = street;
        this.users = users;
    }
}
