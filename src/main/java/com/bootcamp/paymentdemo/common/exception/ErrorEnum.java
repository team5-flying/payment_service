package com.bootcamp.paymentdemo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.bootcamp.paymentdemo.common.Constants.*;
import static com.bootcamp.paymentdemo.common.Constants.MSG_ALREADY_CANCELLED;
import static com.bootcamp.paymentdemo.common.Constants.MSG_AUTH_FAIL;
import static com.bootcamp.paymentdemo.common.Constants.MSG_AUTH_WRONG;
import static com.bootcamp.paymentdemo.common.Constants.MSG_FAIL_REFUND;
import static com.bootcamp.paymentdemo.common.Constants.MSG_NOT_FOUND_ORDER_PRODUCT;
import static com.bootcamp.paymentdemo.common.Constants.MSG_NOT_MATCH_LOGIN;
import static com.bootcamp.paymentdemo.common.Constants.MSG_TOKEN_EMPTY;
import static com.bootcamp.paymentdemo.common.Constants.MSG_TOKEN_EXPIRE;
import static com.bootcamp.paymentdemo.common.Constants.MSG_WEBHOOK_INVALID_SIGNATURE;
import static com.bootcamp.paymentdemo.common.Constants.MSG_WEBHOOK_NOT_FOUND_PAYMENT;

@Getter
public enum ErrorEnum {
    // region 인증 관련
    ERR_TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, MSG_TOKEN_EMPTY),
    ERR_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, MSG_AUTH_WRONG),
    ERR_TOKEN_EXPIRE(HttpStatus.UNAUTHORIZED, MSG_TOKEN_EXPIRE),
    ERR_AUTH_FAIL(HttpStatus.UNAUTHORIZED, MSG_AUTH_FAIL),
    // endregion

    // region 회원 관련
    ERR_NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_MEMBER),
    ERR_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, MSG_DUPLICATE_EMAIL),
    ERR_NOT_MATCH_LOGIN(HttpStatus.UNAUTHORIZED, MSG_NOT_MATCH_LOGIN),
    ERR_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, MSG_INVALID_TOKEN),
    // endregion

    // region 상품 관련
    ERR_NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_PRODUCT),
    // endregion

    // region 주문 관련
    ERR_NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_ORDER),
    ERR_NOT_FOUND_ORDER_PRODUCT(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_ORDER_PRODUCT),
    // endregion

    // region 결제 관련
    ERR_NOT_ENOUGH_STOCK(HttpStatus.CONFLICT, MSG_NOT_ENOUGH_STOCK),
    ERR_NOT_ENOUGH_POINT(HttpStatus.CONFLICT, MSG_NOT_ENOUGH_POINT),
    ERR_NOT_SUCCESS_PAYMENT(HttpStatus.CONFLICT, MSG_NOT_SUCCESS_PAYMENT),
    // endregion

    // region 결제 취소 및 환불 관련
    ERR_NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, MSG_NOT_FOUND_PAYMENT),
    ERR_INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST, MSG_INVALID_REFUND_STATUS),
    ERR_FAIL_REFUND(HttpStatus.BAD_REQUEST, MSG_FAIL_REFUND),
    // endregion

    // region 서버 관련
    ERR_SAVED_DATA_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, MSG_DATA_INSERT_FAIL),
    // endregion

    // region 웹훅 관련
    ERR_WEBHOOK_NOT_FOUND_PAYMENT(HttpStatus.NOT_FOUND, MSG_WEBHOOK_NOT_FOUND_PAYMENT),
    ERR_WEBHOOK_INVALID_SIGNATURE(HttpStatus.BAD_REQUEST, MSG_WEBHOOK_INVALID_SIGNATURE);
    // endregion

    private final HttpStatus status;
    private final String message;

    ErrorEnum(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
