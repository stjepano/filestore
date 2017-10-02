package com.stjepano.objectstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stjepano.objectstore.service.BucketAlreadyExistsException;
import com.stjepano.objectstore.service.BucketDoesNotExistException;
import com.stjepano.objectstore.service.FileAlreadyExistException;
import com.stjepano.objectstore.service.FileDoesNotExistException;
import com.stjepano.objectstore.service.InvalidBucketIdException;
import com.stjepano.objectstore.service.InvalidFileIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Exception handler for this application
 */
@ControllerAdvice
public class ApplicationExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(value = {BucketDoesNotExistException.class, FileDoesNotExistException.class})
    protected ResponseEntity<Object> handleDoesNotExistException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        String bodyOfResponse = null;
        try {
            bodyOfResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            bodyOfResponse = "";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex,
                bodyOfResponse,
                httpHeaders,
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(value = {BucketAlreadyExistsException.class, FileAlreadyExistException.class})
    protected ResponseEntity<Object> handleAlreadyExistException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        String bodyOfResponse = null;
        try {
            bodyOfResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            bodyOfResponse = "";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex,
                bodyOfResponse,
                httpHeaders,
                HttpStatus.CONFLICT,
                request
        );
    }

    @ExceptionHandler(value = {InvalidBucketIdException.class, InvalidFileIdException.class})
    protected ResponseEntity<Object> handleInvalidNames(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        String bodyOfResponse = null;
        try {
            bodyOfResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            bodyOfResponse = "";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex,
                bodyOfResponse,
                httpHeaders,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        String bodyOfResponse = null;
        try {
            bodyOfResponse = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            bodyOfResponse = "";
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return handleExceptionInternal(ex,
                bodyOfResponse,
                httpHeaders,
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
}
