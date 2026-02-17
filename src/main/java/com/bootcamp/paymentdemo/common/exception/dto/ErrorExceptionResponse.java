package com.bootcamp.paymentdemo.common.exception.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorExceptionResponse {
    private final HttpStatus httpStatus;
    private final String message;

    public static ErrorExceptionResponse register(HttpStatus httpStatus, String message) {
        return new ErrorExceptionResponse(httpStatus, message);
    }
}
