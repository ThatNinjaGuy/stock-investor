package com.thatninjaguyspeaks.stockinvestor.util;

import com.zerodhatech.models.HistoricalData;

import java.util.*;

public class BollingerBandsStrategy {

    public static Map<String, List<Double>> calculateDailyBollingerBands(List<HistoricalData> intradayData, int bbPeriod) {
        Map<String, Double> dailyClosingPrices = new LinkedHashMap<>();

        // Aggregate data by date to get daily close values
        for (HistoricalData data : intradayData) {
            String date = data.timeStamp.split("T")[0];
            dailyClosingPrices.put(date, data.close);
        }

        List<Double> closePrices = new ArrayList<>(dailyClosingPrices.values());
        List<String> dates = new ArrayList<>(dailyClosingPrices.keySet());

        Map<String, List<Double>> dateBollingerMap = new LinkedHashMap<>();
        for (int i = bbPeriod - 1; i < closePrices.size(); i++) {
            List<Double> lastPeriodPrices = closePrices.subList(i - bbPeriod + 1, i + 1);
            double sma = calculateSMA(lastPeriodPrices);
            double standardDeviation = calculateStandardDeviation(lastPeriodPrices, sma);
            List<Double> bands = Arrays.asList(sma, sma + 2 * standardDeviation, sma - 2 * standardDeviation, closePrices.get(i));
            dateBollingerMap.put(dates.get(i), bands);
        }

        return dateBollingerMap;
    }

    private static double calculateSMA(List<Double> prices) {
        double sum = 0.0;
        for (double price : prices) {
            sum += price;
        }
        return sum / prices.size();
    }

    private static double calculateStandardDeviation(List<Double> prices, double mean) {
        double sum = 0.0;
        for (double price : prices) {
            sum += Math.pow(price - mean, 2);
        }
        return Math.sqrt(sum / prices.size());
    }
}
