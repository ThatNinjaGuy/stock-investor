package com.thatninjaguyspeaks.stockinvestor.service;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public interface KiteApiService {
    public KiteConnect generateSession(String requestId) throws IOException, KiteException;
    public Object getProfileInfo(String requestId);
    public void importStocks(String requestId);
}
