package com.host.SpringBootAutomationProduction.exceptions;

import com.host.SpringBootAutomationProduction.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({AuthenticationException.class}) /* отвечает за авторизацию */
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, HttpServletRequest request, HttpServletResponse response) {

        ErrorResponse error = new ErrorResponse(ex.getMessage());
        if (response.getHeader("error") != null)
            error.setMessage(response.getHeader("error"));

        return ResponseEntity.status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, WebRequest request) {
        ErrorResponse response = new ErrorResponse("AccessDeniedException; " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(DataSourceNotRequestedException.class)
    public ResponseEntity<ErrorResponse> handleDataSourceNotRequested(DataSourceNotRequestedException ex, WebRequest request) {
        log.error("Handler DataSourceNotRequested, {}", request.getDescription(true), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, WebRequest request) {
        log.error("Handler NotFoundException, {}", request.getDescription(true), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReportTemplateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReportTemplateNotFoundException(ReportTemplateNotFoundException ex, WebRequest request) {
        log.error("Handler ReportTemplateNotFoundException, {}", request.getDescription(true), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Handler RuntimeException, {}", request.getDescription(true), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        log.error("Handler Exception, {}", request.getDescription(true), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Unexpected error occurred"));
    }

}