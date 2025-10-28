package com.prevengos.plug.hubbackend.controller;

import com.prevengos.plug.hubbackend.service.exception.SyncConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SynchronizationController.class)
public class SyncErrorHandler {

    @ExceptionHandler(SyncConflictException.class)
    public ResponseEntity<SyncErrorResponse> handleSyncConflict(SyncConflictException exception) {
        SyncErrorResponse body = new SyncErrorResponse("SYNC_CONFLICT", exception.getMessage(), exception.getDetails());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    public record SyncErrorResponse(String code,
                                    String message,
                                    SyncConflictException.SyncConflictDetails conflict) {
    }
}
