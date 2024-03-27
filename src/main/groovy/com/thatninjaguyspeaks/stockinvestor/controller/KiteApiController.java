package com.thatninjaguyspeaks.stockinvestor.controller;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stockinvesto̰r/zerodha")
@Tag(name = "Zerodha Kite API Controller", description = "Controller for Zerodha Kite API")
public class KiteApiController {

    @Autowired
    KiteApiService kiteApiService;

    @GetMapping("/{requestId}")
    @Operation(summary = "Get Profile Data", description = "Retrieves profile data for the connected user")
    @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Object> getProfileInfo(@PathVariable String requestId) {
        return ResponseEntity.ok(kiteApiService.getProfileInfo(requestId));
    }

}
