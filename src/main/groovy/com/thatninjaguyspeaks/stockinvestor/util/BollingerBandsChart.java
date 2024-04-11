package com.thatninjaguyspeaks.stockinvestor.util;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.List;
import org.jfree.chart.ChartUtils;

public class BollingerBandsChart {

    private static JFreeChart createChart(Map<String, List<Double>> bandValues) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Creating time series for each band and the close prices
        TimeSeries upperBandSeries = new TimeSeries("Upper Band");
        TimeSeries middleBandSeries = new TimeSeries("SMA");
        TimeSeries lowerBandSeries = new TimeSeries("Lower Band");
        TimeSeries closePriceSeries = new TimeSeries("Close Price");

        bandValues.forEach((date, values) -> {
            try {
                Date dateObj = dateFormat.parse(date);
                Day day = new Day(dateObj);
                double upperBand = values.get(1);
                double middleBand = values.get(0);
                double lowerBand = values.get(2);
                double closePrice = values.get(3); // Ensure close price is being passed in values.get(3)
                upperBandSeries.add(day, upperBand);
                middleBandSeries.add(day, middleBand);
                lowerBandSeries.add(day, lowerBand);
                closePriceSeries.add(day, closePrice);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + date);
                e.printStackTrace();
            }
        });

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(upperBandSeries);
        dataset.addSeries(middleBandSeries);
        dataset.addSeries(lowerBandSeries);
        dataset.addSeries(closePriceSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Bollinger Bands Chart",
                "Date", "Value",
                dataset,
                true, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Setting renderer properties for each series
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, new Color(150/(i+1), 0, 150/(i+1)));
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 15));
        axis.setDateFormatOverride(new SimpleDateFormat("MMM dd, yyyy"));

        return chart;
    }

    public static void saveBollingerChartAsPNG(String fileName, Map<String, List<Double>> bandValues, int width, int height) {
        JFreeChart chart = createChart(bandValues);
        File imageFile = new File(fileName);
        try {
            ChartUtils.saveChartAsPNG(imageFile, chart, width, height);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
            e.printStackTrace();
        }
    }
}
