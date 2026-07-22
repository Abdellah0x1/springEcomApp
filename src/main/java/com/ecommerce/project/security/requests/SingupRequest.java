package com.ecommerce.project.security.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;


@Data
public class SingupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email(message =  "Invalid Email Format")
    private String email;
    private Set<String> roles;
    @Size(min = 6, max = 40, message = "Password size must be between 6 and 40 characters")
    private String password;

}
