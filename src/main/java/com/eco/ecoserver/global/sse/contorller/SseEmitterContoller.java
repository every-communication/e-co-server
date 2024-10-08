package com.eco.ecoserver.global.sse.contorller;

import com.eco.ecoserver.domain.user.User;
import com.eco.ecoserver.domain.user.repository.UserRepository;
import com.eco.ecoserver.domain.user.service.UserService;
import com.eco.ecoserver.global.jwt.service.JwtService;
import com.eco.ecoserver.global.sse.service.SseEmitterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseEmitterContoller {

    private final SseEmitterService sseEmitterService;
    private final JwtService jwtService;
    private final UserRepository userService;

    public SseEmitterContoller(SseEmitterService sseEmitterService, JwtService jwtService, UserRepository userService) {
        this.sseEmitterService = sseEmitterService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    // SSE 구독
    @GetMapping("/subscribe")
    public SseEmitter subscribe(HttpServletRequest request) throws IOException {
        return sseEmitterService.subscribe(request);
    }

    @GetMapping(value ="/send-test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void sendTest(HttpServletRequest request) throws IOException {
        Optional<String> emailOpt = jwtService.extractEmailFromToken(request);
        if(emailOpt.isEmpty()) {
            throw new IOException("이메일 없음");
        }
        Optional<User> userOpt = userService.findByEmail(emailOpt.get());
        if(userOpt.isEmpty()) {
            throw new IOException("유저 없음");
        }
        sseEmitterService.sendNotification(userOpt.get().getId(), "name:send-test", "msg:send-test");
    }

    // SSE 구독 취소
    @PostMapping("/unsubscribe")
    public void unsubscribe(HttpServletRequest request) throws IOException {
        sseEmitterService.cancelSubscription(request);
    }
}
