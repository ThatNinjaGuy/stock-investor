package com.thatninjaguyspeaks.stockinvestor.service.impl;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import com.thatninjaguyspeaks.stockinvestor.util.JsonFileGenerator;
import com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy;
import com.thatninjaguyspeaks.stockinvestor.util.Strategy;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KiteApiServiceImpl implements KiteApiService {

    static Logger logger = LogManager.getLogger(KiteApiServiceImpl.class);
    private static final String API_KEY ="96t9owg96o6tek5e";
    private static final String API_SECRET ="ehmzgx0ckqe9wjkcbnj0h2tv66zvpk7v";
    private static final String ZERODHA_USER_ID ="FXL410";
    public static KiteConnect kiteSdk;
    private Map<String, Instrument> companyList = new HashMap<>();

    @Override
    public KiteConnect generateSession(String requestId) throws IOException, KiteException {
        if(kiteSdk!=null) {
            logger.info("Using pre-existing session");
            return kiteSdk;
        }
        return establishSession(requestId);
    }

    public static KiteConnect establishSession(String requestId) throws IOException, KiteException {
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

    @Override
    public Object getProfileInfo(String requestId) {
        logger.info("Getting profile");
        Profile profile = null;
        try {
            generateSession(requestId);
            profile = kiteSdk.getProfile();
            logger.info("Profile obtained with short name {}, userName {}, email {}, products {}, exchanges {}, orderTypes {}", profile.userShortname, profile.userName, profile.email, profile.products, profile.exchanges, profile.orderTypes);
        } catch (IOException | KiteException e) {
            logger.error("Getting profile failed with exception {}", e.getMessage());
            e.printStackTrace();
        }
        return profile;
    }

    @Override
    public void importStocks(String requestId) {
        logger.info("Importing Stocks");
        try {
            List<Instrument> instruments = generateSession(requestId).getInstruments();
            companyList = instruments.parallelStream()
                    .filter(x -> (!x.segment.equals("INDICES"))&&(x.exchange.equals("NSE"))&&(x.name!=null)&&(x.instrument_type.equals("EQ")))
                    .collect(Collectors.toMap(Instrument::getTradingsymbol, Function.identity()));
        } catch (KiteException | IOException | JSONException e) {
            logger.error("Failed with exception");
            e.printStackTrace();
        }
        logger.info("{} STOCKS IMPORTED", companyList.size());
        JsonFileGenerator.persistData(companyList, "companyList.json");
    }

    @Override
    public void evaluateStrategy(String requestId) {
        if(companyList==null || companyList.size()==0)
            importStocks(requestId);
        new Thread(new Strategy(companyList)).start();
    }

}
