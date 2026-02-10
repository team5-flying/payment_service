package com.bootcamp.paymentdemo.common.exception;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.bootcamp.paymentdemo.common.Constants.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceErrorException.class)
    public ResponseEntity<BaseResponse<Void>> handleServiceErrorException(ServiceErrorException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(e.getHttpStatus()).body(BaseResponse.fail(String.valueOf(e.getHttpStatus().value()), e.getMessage(), null));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<Void>> IllegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("요청 값 유효성 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), MSG_NOT_VALID_VALUE, null));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> MethodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.error("데이터 유효성 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), e.getAllErrors().get(0).getDefaultMessage(), null));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<Void>> HttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("데이터 JSON 변환 에러 발생 : ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), MSG_NOT_VALID_VALUE, null));
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Void>> DataIntegrityViolationExceptionHandler(DataIntegrityViolationException e) {
        log.error("데이터 등록 실패 발생 : ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), MSG_DATA_INSERT_FAIL, null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthException(AuthenticationException e) {
        log.error("인증 실패 : ",  e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(BaseResponse.fail(String.valueOf(HttpStatus.UNAUTHORIZED), e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleCriticalErrorException(Exception e) {
        log.error("서버 에러 발생", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponse.fail(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), MSG_SERVER_ERROR_OCCUR, null));
    }
}
