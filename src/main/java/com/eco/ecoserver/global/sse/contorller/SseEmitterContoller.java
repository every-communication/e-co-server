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

    public SseEmitterContoller(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    // SSE 구독
    @GetMapping("/subscribe")
    public SseEmitter subscribe(HttpServletRequest request) throws IOException {
        return sseEmitterService.subscribe(request);
    }

    @GetMapping(value ="/send-test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendTest(HttpServletRequest request) throws IOException {
        return sseEmitterService.sendNotification(request, "요청 테스트");
    }

    // SSE 구독 취소
    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(HttpServletRequest request) throws IOException {
        sseEmitterService.cancelSubscription(request);
        return ResponseEntity.ok("구독이 취소되었습니다.");
    }
}
