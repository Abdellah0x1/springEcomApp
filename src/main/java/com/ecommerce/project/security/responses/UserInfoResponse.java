package com.ecommerce.project.security.responses;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UserInfoResponse {
    private Long id;
//    private String jwtToken;
    private String username;

    @Email
    private String email;
    private List<String> roles;


//    public UserInfoResponse(Long id, String jwtToken, String username, List<String> roles) {
//        this.id = id;
//        this.jwtToken = jwtToken;
//        this.roles =roles;
//        this.jwtToken = jwtToken;
//    }

    public UserInfoResponse(Long id, String username,String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
        this.email = email;
    }


}
