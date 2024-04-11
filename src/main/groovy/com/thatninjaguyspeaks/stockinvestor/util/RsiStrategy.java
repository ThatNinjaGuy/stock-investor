package com.thatninjaguyspeaks.stockinvestor.util;

import com.zerodhatech.models.HistoricalData;

import java.util.*;

public class RsiStrategy {

    public static Map<String, Double> calculateDailyRSI(List<HistoricalData> intradayData, int rsiPeriod) {
        Map<String, Double> dailyClosingPrices = new LinkedHashMap<>();

        // Aggregate data by date to get daily close values
        for (HistoricalData data : intradayData) {
            String date = data.timeStamp.split("T")[0]; // Assuming timestamp is in ISO format
            dailyClosingPrices.put(date, data.close); // This assumes the last entry is the closing price, adjust logic as needed
        }

        List<Double> closePrices = new ArrayList<>(dailyClosingPrices.values());
        List<Double> rsiValues = calculateRSI(closePrices, rsiPeriod);

        // Creating a map to return date and corresponding RSI value
        Map<String, Double> dateRsiMap = new LinkedHashMap<>();
        List<String> dates = new ArrayList<>(dailyClosingPrices.keySet());
        for (int i = 0; i < rsiValues.size(); i++) {
            dateRsiMap.put(dates.get(i + rsiPeriod - 1), rsiValues.get(i)); // Match dates with their corresponding RSI values
        }

        return dateRsiMap;
    }

    public static List<Double> calculateRSI(List<Double> closePrices, int rsiPeriod) {
        List<Double> rsiValues = new ArrayList<>();
        if (closePrices.size() < rsiPeriod) {
            return rsiValues; // Not enough data to calculate RSI
        }

        double averageGain = 0, averageLoss = 0;

        // First calculate initial averages of gains and losses
        for (int i = 1; i <= rsiPeriod; i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0) {
                averageGain += change;
            } else {
                averageLoss += Math.abs(change);
            }
        }

        averageGain /= rsiPeriod;
        averageLoss /= rsiPeriod;

        double rs = averageLoss == 0 ? Double.POSITIVE_INFINITY : averageGain / averageLoss;
        rsiValues.add(100 - 100 / (1 + rs));

        // Calculate RSI for the rest
        for (int i = rsiPeriod + 1; i < closePrices.size(); i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? Math.abs(change) : 0;

            averageGain = (averageGain * (rsiPeriod - 1) + gain) / rsiPeriod;
            averageLoss = (averageLoss * (rsiPeriod - 1) + loss) / rsiPeriod;

            rs = averageLoss == 0 ? Double.POSITIVE_INFINITY : averageGain / averageLoss;
            rsiValues.add(100 - 100 / (1 + rs));
        }

        return rsiValues;
    }
}
