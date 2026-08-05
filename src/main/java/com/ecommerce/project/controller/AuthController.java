package com.ecommerce.project.controller;

import com.ecommerce.project.enums.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.requests.LoginRequest;
import com.ecommerce.project.security.requests.SingupRequest;
import com.ecommerce.project.security.responses.MessageResponse;
import com.ecommerce.project.security.responses.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> Login(@RequestBody @Valid LoginRequest loginRequest){
        Authentication authentication ;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        }catch (AuthenticationException e){
            Map<String , Object> body = new HashMap<>();
            body.put("message","BAD Credentials");
            body.put("status",  false);
            return new ResponseEntity<Object>(body, HttpStatus.NOT_FOUND);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);


        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority()).toList();
        UserInfoResponse reponse = new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),userDetails.getEmail(),roles);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(reponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> SingUp(@Valid @RequestBody SingupRequest signupRequest){
        if(userRepository.existsByUserName(signupRequest.getUsername())){
            return ResponseEntity.badRequest().body(new MessageResponse("Username already exists"));
        }
        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
        }
        User user = new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                encoder.encode(signupRequest.getPassword())
        );

        Set<String> roleStr= signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        if(roleStr == null){
            //setting default role to USER
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: role is not found"));
            roles.add(userRole);
        }else {
            //admin -> ROLE_ADMIN
            // seller -> ROLE_SELLER
            roleStr.forEach(role -> {
                switch (role){
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(()-> new RuntimeException("Error: role is not found"));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).orElseThrow(()-> new RuntimeException("Error: role is not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).orElseThrow(()-> new RuntimeException("Error: role is not found"));
                        roles.add(userRole);
                        break;
                }
            });
        }
            user.setRoles(roles);
            userRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signoutUser(){
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new MessageResponse("You haven signed out"));
    }


    @GetMapping("/username")
    public String currentUserName(Authentication authentication){
        if(authentication != null) return authentication.getName();
        else return "NULL";
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> currentUser(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()  
                .map(item -> item.getAuthority()).toList();
        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(),userDetails.getEmail(),roles);
        return ResponseEntity.ok().body(response);
    }
}
