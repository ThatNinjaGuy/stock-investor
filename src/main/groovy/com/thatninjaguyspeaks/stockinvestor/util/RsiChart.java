package com.thatninjaguyspeaks.stockinvestor.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;
import org.jfree.chart.ChartUtils;

import java.io.File;
import java.io.IOException;

public class RsiChart {

    private static JFreeChart createChart(Map<String, Double> rsiValues) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        rsiValues.forEach((date, rsi) ->
                dataset.addValue(rsi, "RSI", date)
        );

        return ChartFactory.createLineChart(
                "RSI Chart",
                "Date", "RSI Value",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    public static void saveChartAsPNG(String fileName, Map<String, Double> rsiValues, int width, int height) {
        JFreeChart chart = createChart(rsiValues);
        File imageFile = new File(fileName);
        try {
            ChartUtils.saveChartAsPNG(imageFile, chart, width, height);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
            e.printStackTrace();
        }
    }
}
