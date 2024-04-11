package com.thatninjaguyspeaks.stockinvestor.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public interface KiteApiService {
    KiteConnect generateSession(String requestId) throws IOException, KiteException;
    Object getProfileInfo(String requestId);
    void importStocks(String requestId);

    void updateHistoricalData(String requestId);

    Map<String, List<String>> evaluateStrategy(String instrumentName, int rsiPeriod, double lowerThreshold, double upperThreshold);

    void loadHistoricalDataInternal();
}
