package com.thatninjaguyspeaks.stockinvestor.service.impl;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KiteApiServiceImpl implements KiteApiService {

    Logger logger = LogManager.getLogger(KiteApiServiceImpl.class);
    private static final String API_KEY ="96t9owg96o6tek5e";
    private static final String API_SECRET ="ehmzgx0ckqe9wjkcbnj0h2tv66zvpk7v";
    private static final String ZERODHA_USER_ID ="FXL410";
    private static KiteConnect kiteSdk;


    @Override
    public Object getProfileInfo(String requestId) {
        logger.info("Getting profile");
        Profile profile = null;
        try {
            kiteSdk = generateSession(requestId);
            profile = kiteSdk.getProfile();
            logger.info("Profile obtained with short name {}, userName {}, email {}, products {}, exchanges {}, orderTypes {}", profile.userShortname, profile.userName, profile.email, profile.products, profile.exchanges, profile.orderTypes);
        } catch (IOException | KiteException e) {
            logger.error("Getting profile failed with exception {}", e.getMessage());
            e.printStackTrace();
        }
        return profile;
    }

    @Override
    public KiteConnect generateSession(String requestId) throws IOException, KiteException {
        if(kiteSdk!=null) {
            logger.info("Using pre-existing session");
            return kiteSdk;
        }
        kiteSdk = new KiteConnect(API_KEY);
        kiteSdk.setUserId(ZERODHA_USER_ID);
        User user = null;
        try {
            user = kiteSdk.generateSession(requestId, API_SECRET);
            kiteSdk.setAccessToken(user.accessToken);
            kiteSdk.setPublicToken(user.publicToken);
            logger.info("Session creation succeeded with public token: {}", user.publicToken);
        }  catch (IOException | KiteException | JSONException e) {
            logger.error("Error occurred in generating session with exception {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return kiteSdk;
    }
}
