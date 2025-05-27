package com.AITemplate.exception;

/**
 * 404 Not Found 상황에서 사용 예외
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
