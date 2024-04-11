package com.thatninjaguyspeaks.stockinvestor.service.impl;

import com.thatninjaguyspeaks.stockinvestor.service.KiteApiService;
import com.thatninjaguyspeaks.stockinvestor.util.FileProcessorUtil;
//import com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy;
//import com.thatninjaguyspeaks.stockinvestor.util.Strategy;
import com.thatninjaguyspeaks.stockinvestor.util.RsiChart;
import com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.thatninjaguyspeaks.stockinvestor.util.BollingerBandsChart.saveBollingerChartAsPNG;
import static com.thatninjaguyspeaks.stockinvestor.util.BollingerBandsStrategy.calculateDailyBollingerBands;
import static com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy.calculateDailyRSI;
import static com.thatninjaguyspeaks.stockinvestor.util.RsiStrategy.calculateRSI;

@Component
public class KiteApiServiceImpl implements KiteApiService {

    static Logger logger = LogManager.getLogger(KiteApiServiceImpl.class);
    private static final String API_KEY = "96t9owg96o6tek5e";
    private static final String API_SECRET = "ehmzgx0ckqe9wjkcbnj0h2tv66zvpk7v";
    private static final String ZERODHA_USER_ID = "FXL410";
    public static KiteConnect kiteSdk;
    private Map<String, Instrument> companyList = new HashMap<>();
    private Map<String, HistoricalData> allStockHistory;
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
    public Object evaluateStrategy(String instrumentName, int rsiPeriod, double lowerThreshold, double upperThreshold) {
        if(allStockHistory==null || allStockHistory.size()==0)
            loadHistoricalDataInternal();
//        return runRsiStrategy(instrumentName, rsiPeriod, lowerThreshold, upperThreshold);
        return runBollingerBandsStrategy(instrumentName, rsiPeriod, lowerThreshold, upperThreshold);

    }

    @Override
    public void loadHistoricalDataInternal() {
        allStockHistory = FileProcessorUtil.readAllStockData();
        logger.info("Loaded historical data for {} stocks", allStockHistory.size());
    }

    private Map<String, List<String>> runRsiStrategy(String instrumentName, int rsiPeriod, double lowerThreshold, double upperThreshold) {
        Map<String, Double> rsiValues = calculateDailyRSI(allStockHistory.get(instrumentName.toLowerCase()).dataArrayList, rsiPeriod);
        logger.info("Calculated rsi for {} over {} days", instrumentName, rsiValues.size());

        Map<String, List<String>> indicatorResults = new HashMap<>();
        List<String> overbought = new ArrayList<>();
        List<String> oversold = new ArrayList<>();
        List<String> normal = new ArrayList<>();

        rsiValues.forEach((time, rsi) -> {
            if (rsi > upperThreshold) {
                overbought.add(String.format("%1s %2s - OVERBOUGHT", time, rsi));
            } else if (rsi < lowerThreshold) {
                oversold.add(String.format("%1s %2s - OVERSOLD", time, rsi));
            } else{
                normal.add(String.format("%1s %2s - NORMAL", time, rsi));
            }
        });
        logger.info("Completed analysis for {}", instrumentName);
        overbought.sort(Comparator.reverseOrder());
        oversold.sort(Comparator.reverseOrder());
        normal.sort(Comparator.reverseOrder());
        indicatorResults.put("OVERBOUGHT", overbought);
        indicatorResults.put("OVERSOLD", oversold);
        indicatorResults.put("NORMAL", normal);
        RsiChart.saveChartAsPNG("src/main/resources/rsi-result.png",rsiValues, rsiValues.size()*20, 800);
        return indicatorResults;
    }

    private Map<String, List<String>> runBollingerBandsStrategy(String instrumentName, int bbPeriod, double lowerBound, double upperBound) {
        Map<String, List<Double>> bandValues = calculateDailyBollingerBands(allStockHistory.get(instrumentName.toLowerCase()).dataArrayList, bbPeriod);
        logger.info("Calculated Bollinger Bands for {} over {} days", instrumentName, bandValues.size());

        Map<String, List<String>> indicatorResults = new HashMap<>();
        List<String> overbought = new ArrayList<>();
        List<String> oversold = new ArrayList<>();
        List<String> normal = new ArrayList<>();

        bandValues.forEach((date, values) -> {
            double sma = values.get(0);       // Simple Moving Average (Middle Band)
            double upperBand = values.get(1); // Upper Bollinger Band
            double lowerBand = values.get(2); // Lower Bollinger Band
            double closePrice = values.get(3); // Assuming close price is also passed in the list

            if (closePrice > upperBand) {
                overbought.add(String.format("%s %f %f %f %f - OVERBOUGHT", date, closePrice, lowerBand, sma, upperBand));
            } else if (closePrice < lowerBand) {
                oversold.add(String.format("%s %f %f %f %f - OVERSOLD", date, closePrice, lowerBand, sma, upperBand));
            } else {
                normal.add(String.format("%s %f %f %f %f - NORMAL", date, closePrice, lowerBand, sma, upperBand));
            }
        });

        logger.info("Completed analysis for {}", instrumentName);
        overbought.sort(Comparator.reverseOrder());
        oversold.sort(Comparator.reverseOrder());
        normal.sort(Comparator.reverseOrder());

        indicatorResults.put("OVERBOUGHT", overbought);
        indicatorResults.put("OVERSOLD", oversold);
        indicatorResults.put("NORMAL", normal);

        // Consider saving chart with Bollinger Bands plotted if needed
        saveBollingerChartAsPNG("src/main/resources/bollinger-band-result.png", bandValues, bandValues.size()*20, 1800);
        return indicatorResults;
    }
}
