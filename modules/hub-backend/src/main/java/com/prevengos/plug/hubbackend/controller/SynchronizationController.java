package com.prevengos.plug.hubbackend.controller;

import com.prevengos.plug.hubbackend.service.SynchronizationService;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sincronizacion")
public class SynchronizationController {

    private final SynchronizationService synchronizationService;

    public SynchronizationController(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    @PostMapping
    public ResponseEntity<SyncPushResponse> push(@Valid @RequestBody SyncPushRequest request) {
        return ResponseEntity.ok(synchronizationService.push(request));
    }

    @GetMapping
    public ResponseEntity<SyncPullResponse> pull(@RequestParam(name = "afterToken", defaultValue = "0") long afterToken,
                                                 @RequestParam(name = "limit", defaultValue = "100") int limit) {
        return ResponseEntity.ok(synchronizationService.pull(afterToken, limit));
    }
}
