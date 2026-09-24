package com.ecommerce.project.Websockets;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ExecutorChannelInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        if(StompCommand.CONNECT.equals(accessor.getCommand())){

            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            if(authHeaders == null || authHeaders.isEmpty()) {
                throw new APIException("Missing Authorization header");
            }

            String token = authHeaders.getFirst();
            String jwt = token.substring(7);

            if(jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                accessor.setUser(authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            // For all non-CONNECT frames (SEND, SUBSCRIBE, etc.), propagate
            // the STOMP session principal into the SecurityContext so that
            // downstream services using AuthUtils / SecurityContextHolder
            // can resolve the authenticated user on the message-handling thread.
            Principal principal = accessor.getUser();
            if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        // Clear the SecurityContext after message handling to prevent leaking
        // authentication to other threads from the executor pool.
        SecurityContextHolder.clearContext();
    }
}
