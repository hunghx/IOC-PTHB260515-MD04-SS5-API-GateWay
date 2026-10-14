package com.example.gateway.exception;

import com.example.gateway.model.ApiResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;

@Component
@Order(-2)
public class GatewayErrorExceptionHandler
        implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayErrorExceptionHandler(
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex) {

        // 1. Xác định HTTP Status
        HttpStatus status;
        String message;

        if (isConnectionError(ex)) {

            status = HttpStatus.SERVICE_UNAVAILABLE;

            message =
                    "Cổng Gateway không thể kết nối tới dịch vụ đích";

        } else if (ex instanceof ResponseStatusException responseEx
                && responseEx.getStatusCode().value() == 503) {

            status = HttpStatus.SERVICE_UNAVAILABLE;

            message =
                    "Cổng Gateway không thể kết nối tới dịch vụ đích";

        } else if (ex instanceof ResponseStatusException responseEx
                && responseEx.getStatusCode().value() == 404) {

            status = HttpStatus.NOT_FOUND;

            message = "Đường dẫn API không tồn tại";

        } else {

            status = HttpStatus.INTERNAL_SERVER_ERROR;

            message = "Lỗi hệ thống tại API Gateway";
        }

        // 2. Tạo ApiResponseError
        ApiResponseError error = new ApiResponseError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        // 3. Thiết lập HTTP Response
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(status);

        response.getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        // 4. Chuyển DTO sang JSON và trả về Client
        try {

            byte[] body = objectMapper
                    .writeValueAsBytes(error);

            return response.writeWith(
                    Mono.just(
                            response.bufferFactory().wrap(body)
                    )
            );

        } catch (Exception e) {

            return Mono.error(e);
        }
    }

    // Kiểm tra lỗi kết nối Service
    private boolean isConnectionError(Throwable ex) {

        Throwable cause = ex;

        while (cause != null) {

            if (cause instanceof ConnectException
                    || cause instanceof UnknownHostException
                    || cause instanceof SocketTimeoutException
                    || cause instanceof io.netty.channel.ConnectTimeoutException
                    || cause instanceof reactor.netty.http.client.PrematureCloseException) {

                return true;
            }

            cause = cause.getCause();
        }

        return false;
    }
}