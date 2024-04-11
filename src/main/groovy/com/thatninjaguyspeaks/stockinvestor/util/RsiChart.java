package com.thatninjaguyspeaks.stockinvestor.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.jfree.chart.ChartUtils;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.io.File;
import java.io.IOException;

public class RsiChart {
    private static JFreeChart createChart(Map<String, Double> rsiValues) {
        TimeSeries series = new TimeSeries("RSI");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Create markers for the RSI overbought and oversold lines
        ValueMarker overbought = new ValueMarker(70, Color.red, new BasicStroke(1.0f));
        ValueMarker oversold = new ValueMarker(30, Color.green, new BasicStroke(1.0f));
        // Define the interval to shade
        IntervalMarker intervalMarker = new IntervalMarker(30, 70);
        intervalMarker.setPaint(new GradientPaint(0f, 0f, new Color(222, 222, 255, 128), 0f, 0f, new Color(222, 222, 255, 128)));
        intervalMarker.setAlpha(0.5f);  // Set transparency (optional)
        // Get the plot's renderer
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

// Set the series paint (change the line color here)
        renderer.setSeriesPaint(0, Color.blue);

// Set a smooth, solid line
        renderer.setSeriesStroke(0, new BasicStroke(
                2.0f,                     // Line width
                BasicStroke.CAP_ROUND,    // End cap style
                BasicStroke.JOIN_ROUND)); // Line join style

// Ensure lines are visible and shapes are not
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);

        try {
            rsiValues.forEach((date, rsi) -> {
                try {
                    Date dateObj = dateFormat.parse(date);
                    series.add(new Day(dateObj), rsi);
                } catch (ParseException e) {
                    System.err.println("Error parsing date: " + date);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "RSI Chart",
                "Date",
                "RSI Value",
                dataset,
                false, true, false);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.addRangeMarker(overbought);
        plot.addRangeMarker(oversold);
        plot.addRangeMarker(intervalMarker, Layer.BACKGROUND);
        // Apply the renderer to the plot
        plot.setRenderer(renderer);
        // Adjust the range axis (Y-axis) to display from 0 to 100
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 15));
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        return chart;
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
