package com.thatninjaguyspeaks.stockinvestor.service.impl;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import com.thatninjaguyspeaks.stockinvestor.util.FileProcessorUtil;
//import com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy;
//import com.thatninjaguyspeaks.stockinvestor.util.Strategy;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import com.zerodhatech.models.Profile;
import com.zerodhatech.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KiteApiServiceImpl implements KiteApiService {

    static Logger logger = LogManager.getLogger(KiteApiServiceImpl.class);
    private static final String API_KEY = "96t9owg96o6tek5e";
    private static final String API_SECRET = "ehmzgx0ckqe9wjkcbnj0h2tv66zvpk7v";
    private static final String ZERODHA_USER_ID = "FXL410";
    public static KiteConnect kiteSdk;
    private Map<String, Instrument> companyList = new HashMap<>();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public KiteConnect generateSession(String requestId) throws IOException, KiteException {
        if (kiteSdk != null) {
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
        } catch (IOException | KiteException | JSONException e) {
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
                    .filter(x -> (!x.segment.equals("INDICES")) && (x.exchange.equals("NSE")) && (x.name != null) && (x.instrument_type.equals("EQ")))
                    .collect(Collectors.toMap(Instrument::getTradingsymbol, Function.identity()));
        } catch (KiteException | IOException | JSONException e) {
            logger.error("Failed with epersistDataxception");
            e.printStackTrace();
        }
        logger.info("{} STOCKS IMPORTED", companyList.size());
        FileProcessorUtil.persistData(companyList, "companyList.json");
    }

    @Override
    public void updateHistoricalData(String requestId) {
        logger.info("Importing historical data for stocks");
        String timeFrame = "30minute";
        if(companyList.size() == 0)
            importStocks(requestId);

        companyList.entrySet().stream().distinct().forEach((entry) -> {
            HistoricalData stockHistoricalData = new HistoricalData();
            String instrumentToken = String.valueOf(entry.getValue().instrument_token);
            String instrumentName = entry.getValue().name;
            LocalDate endDay = LocalDate.now(); // Start from today
            LocalDate startDay = endDay.minusDays(60); // Subtract 60 days for the initial batch
            while (!startDay.isBefore(LocalDate.of(1990, 1, 1))) {
                try {
                    String fromString = startDay + " 09:15:00";
                    String toString = endDay + " 15:15:00";
                    Date from = formatter.parse(fromString);
                    Date to = formatter.parse(toString);
                    logger.info("Generating historical data for {} for the period {} to {}", instrumentName, fromString, toString);
                    HistoricalData timeRangeHistoricalData = generateSession(requestId)
                            .getHistoricalData(from, to, instrumentToken, timeFrame, false);
                    if(timeRangeHistoricalData.dataArrayList.isEmpty())
                        break;
                    stockHistoricalData.dataArrayList.addAll(timeRangeHistoricalData.dataArrayList);
                    logger.info("Generated historical data for {}.", instrumentName);
                } catch (KiteException | IOException | ParseException e) {
                    logger.error("Error in generating historical data.");
                    e.printStackTrace();
                    break;
                } catch (Exception e){
                    logger.error("Error in generating historical data.");
                    e.printStackTrace();
                    break;
                }
                endDay = startDay;
                startDay = endDay.minusDays(60);
            }
            FileProcessorUtil.writeStockData(entry.getValue().name, stockHistoricalData);
        });
    }

    @Override
    public void evaluateStrategy(String requestId) {
        if (companyList == null || companyList.size() == 0)
            importStocks(requestId);
//        new Thread(new Strategy(companyList)).start();
    }

    @Override
    public void loadHistoricalDataInternal() {

    }

}
