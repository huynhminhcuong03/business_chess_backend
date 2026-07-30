package com.businesschess.common;

import org.springframework.http.HttpStatus;

public class ApiResponse {

    private int code;

    private String status;

    private final String message;

    private final Object data;

    private ApiResponse(Builder builder) {
        this.code = builder.code;
        this.status = builder.status;
        this.message = builder.message;
        this.data = builder.data;
    }

    public static ApiResponse ok(String message) {
        return new ApiResponse(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), message, null);
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), message, data);
    }

    public static ApiResponse created(String message) {
        return new ApiResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(), message, null);
    }

    public static ApiResponse created(String message, Object data) {
        return new ApiResponse(HttpStatus.CREATED.value(), HttpStatus.CREATED.getReasonPhrase(), message, data);
    }

    public ApiResponse(int code, String status, String message, Object data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public static class Builder {

        private int code;
        private String status;
        private String message;
        private Object data;

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public ApiResponse build() {
            return new ApiResponse(this);
        }
    }
}
