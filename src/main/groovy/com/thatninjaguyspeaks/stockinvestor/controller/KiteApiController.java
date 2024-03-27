package com.thatninjaguyspeaks.stockinvestor.controller;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/stockinvesto̰r/zerodha")
@Tag(name = "Zerodha Kite API Controller", description = "Controller for Zerodha Kite API")
public class KiteApiController {

    @Autowired
    KiteApiService kiteApiService;

    @GetMapping("/session/{requestId}")
    @Operation(summary = "Generate Zerodha session", description = "Generate session for Zerodha Kite API")
    @ApiResponse(responseCode = "200", description = "Session generated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Boolean> generateSession(@PathVariable String requestId) throws IOException, KiteException {
        boolean response = false;
        response = kiteApiService.generateSession(requestId) != null;
        return ResponseEntity.ok(response);
    }
    @GetMapping("/profile/{requestId}")
    @Operation(summary = "Get Profile Data", description = "Retrieves profile data for the connected user")
    @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Object> getProfileInfo(@PathVariable String requestId) {
        return ResponseEntity.ok(kiteApiService.getProfileInfo(requestId));
    }

    @GetMapping("/stocks/import/{requestId}")
    @Operation(summary = "Import stock market data", description = "Get live market data")
    @ApiResponse(responseCode = "200", description = "Data retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Object.class)))
    public ResponseEntity<Object> importStocks(@PathVariable String requestId) {
        kiteApiService.importStocks(requestId);
        return ResponseEntity.ok("Process executed");
    }

}
