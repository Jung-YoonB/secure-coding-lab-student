package edu.secure.lab.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

/** 안전 버전의 오류 처리: 사용자에게는 일반 메시지, 상세는 내부 로그에만 기록 */
@RestControllerAdvice(assignableTypes = {SafeController.class, SafePageController.class})
public class SafeExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(SafeExceptionAdvice.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", "요청을 처리할 수 없습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "잘못된 요청입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception e) {
        String errorId = UUID.randomUUID().toString();
        log.error("처리 중 오류 errorId={}", errorId, e);            // 상세는 서버 로그에만
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "요청을 처리할 수 없습니다.", "errorId", errorId));
    }
}
