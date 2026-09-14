package com.ecommerce.project.services;

import com.ecommerce.project.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseEmitterService {
    ConcurrentHashMap<Long , List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId){
        Long timeout = 30 * 60 * 1000L;
        SseEmitter emitter = new SseEmitter(timeout);


        emitters.computeIfAbsent(userId , k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable removeThis = () -> removeEmitter(userId, emitter);

        emitter.onCompletion(removeThis);

        emitter.onError(e -> {
            log.error("SseEmitterService error ", e);
            removeEmitter(userId, emitter);
        });

//        remove the sse emitter
        emitter.onTimeout(removeThis);

        try {
            emitter.send(SseEmitter.event()
                    .name("Connected")
                    .data("Sse connection established "));

        }catch (IOException e){
            log.error("Failed to send initial SSE event for user {} ", userId);
        }

        log.info("User {} subscribed to SSE (active emitters : {})  ", userId, emitters.get(userId).size() );

        return emitter;
    }



    public  void sendToUser(Long userId, Notification notification) throws IOException {
        List<SseEmitter> userEmitters = emitters.get(userId);

        if(userEmitters == null  || userEmitters.isEmpty()){
            log.debug("No active sse emitter for user {} , skipping push ", userId);
            return;
        }

        for( SseEmitter emitter: userEmitters ){
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));
            }catch (IOException e){
                log.warn("Failed to send SSE to user {}, removing emitter", userId);
                emitter.completeWithError(e);
            }
        }
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
        log.debug("Removed SSE emitter for user {}", userId);
    }

}
