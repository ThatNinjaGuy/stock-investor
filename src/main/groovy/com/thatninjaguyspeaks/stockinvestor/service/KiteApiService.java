package com.thatninjaguyspeaks.stockinvestor.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public interface KiteApiService {
    KiteConnect generateSession(String requestId) throws IOException, KiteException;
    Object getProfileInfo(String requestId);
    void importStocks(String requestId);
    void evaluateStrategy(String requestId);
}
