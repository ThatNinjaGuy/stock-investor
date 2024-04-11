//package com.thatninjaguyspeaks.stockinvestor.util;
//
//import com.thatninjaguyspeaks.stockinvestor.models.HistoricalStockData;
//import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
//import com.zerodhatech.models.HistoricalData;
//
//import java.io.IOException;
//import java.util.Date;
//
//import com.zerodhatech.models.Instrument;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.json.JSONException;
//
//import static com.thatninjaguyspeaks.stockinvestor.service.impl.KiteApiServiceImpl.kiteSdk;
//
//public class RsiStrategy implements Runnable {
//    private static final Logger logger = LogManager.getLogger(RsiStrategy.class);
//    private Date from;
//    private Date to;
//    private String instrument;
//    private String timeFrame;
//    private String stockName;
//    private double rsiThreshold;
//    private String comparisonType;
//    private Instrument companyInstrument;
//
//    public RsiStrategy(Date from, Date to, String instrument, String timeFrame, String stockName, double rsiThreshold, String comparisonType, Instrument companyInstrument) {
//        this.from = from;
//        this.to = to;
//        this.instrument = instrument;
//        this.timeFrame = timeFrame;
//        this.stockName = stockName;
//        this.rsiThreshold = rsiThreshold;
//        this.comparisonType = comparisonType;
//        this.companyInstrument = companyInstrument;
//    }
//
//    @Override
//    public void run() {
//        HistoricalData historicalData = null;
//        try {
//            historicalData = kiteSdk.getHistoricalData(from, to, instrument, timeFrame, false);
//            int size = historicalData.dataArrayList.size() - 1;
//            double gain = 0;
//            double loss = 0;
//            for (int i = 0; i < 14; i++) {
//                if (historicalData.dataArrayList.get(i + 1).close > historicalData.dataArrayList.get(i).close) {
//                    gain += ((historicalData.dataArrayList.get(i + 1).close - historicalData.dataArrayList.get(i).close) / historicalData.dataArrayList.get(i).close) * 100;
//                } else {
//                    loss += ((historicalData.dataArrayList.get(i).close - historicalData.dataArrayList.get(i + 1).close) / historicalData.dataArrayList.get(i + 1).close) * 100;
//                }
//            }
//            gain = gain / 14;
//            loss = loss / 14;
//            double rsi = 100 - (100 / (1 + (gain / loss)));
//            // Simplified RSI calculation for the initial 14 days
//            // Consider using a more complex approach for a rolling RSI calculation
//            // Decision making based on RSI value
//            boolean conditionMet = false;
//            if (("GREATER THAN".equals(comparisonType) && rsi > rsiThreshold)
//                    || ("LESS THAN".equals(comparisonType) && rsi < rsiThreshold)) {
//                conditionMet = true;
//                logger.info("Condition met for stock: " + stockName + " with RSI: " + rsi + " and closing price: " + historicalData.dataArrayList.get(size).close);
//            JsonFileGenerator.persistData(HistoricalStockData.builder().instrument(companyInstrument).historicalData(historicalData).conditionMet(conditionMet).build(), stockName.replaceAll(" ","") .toLowerCase()+ ".json");
//        } catch (KiteException | IOException | JSONException ex) {
//            logger.error("Eeror occurred during RSI calculation", ex);
//            ex.printStackTrace();
//        }
//    }
//}
//
//
////package com.thatninjaguyspeaks.stockinvestor.util;
////
////import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
////import com.zerodhatech.models.HistoricalData;
////import java.io.IOException;
////import java.util.Date;
////import javax.swing.table.DefaultTableModel;
////
////import org.apache.logging.log4j.LogManager;
////import org.apache.logging.log4j.Logger;
////import org.json.JSONException;
////
////import static com.thatninjaguyspeaks.stockinvestor.service.impl.KiteApiServiceImpl.kiteSdk;
////
////public class RsiStrategy implements Runnable{
////    private static final Logger logger = LogManager.getLogger(RsiStrategy.class);
////    DefaultTableModel model;
////    Date from;
////    Date to;
////    String instrument;
////    String timeFrame;
////    String stockName;
////    RsiStrategy(Date from,Date to,String instrument,String timeFrame,DefaultTableModel model,String stockName)
////    {
////        this.from=from;
////        this.to=to;
////        this.instrument=instrument;
////        this.timeFrame=timeFrame;
////        this.model=model;
////        this.stockName=stockName;
////    }
////
////    @Override
////    public void run() {
////
////        HistoricalData historicalData = null;
////        try {
////            historicalData = kiteSdk.getHistoricalData(from, to, instrument, timeFrame, false, false);
////            int size=historicalData.dataArrayList.size()-1;
////            double gain=0;double loss=0;
////            for(int i=0;i<14;i++)
////            {
////
////                if(historicalData.dataArrayList.get(i+1).close>historicalData.dataArrayList.get(i).close)
////                {
////                    gain+=((historicalData.dataArrayList.get(i+1).close-historicalData.dataArrayList.get(i).close)/historicalData.dataArrayList.get(i).close)*100;
////
////                }
////                else
////                {
////                    loss+=((historicalData.dataArrayList.get(i).close-historicalData.dataArrayList.get(i+1).close)/historicalData.dataArrayList.get(i+1).close)*100;
////                }
////
////
////            }
////            gain=gain/14;
////            loss=loss/14;
////            double rsi=100-(100/(1+(gain/loss)));
////            for(int i=14;i<size;i++)
////            {
////
////                if(historicalData.dataArrayList.get(i+1).close>historicalData.dataArrayList.get(i).close)
////                {
////                    gain=(gain*13+((historicalData.dataArrayList.get(i+1).close-historicalData.dataArrayList.get(i).close)/historicalData.dataArrayList.get(i).close)*100)/14;
////                    loss=(loss*13)/14;
////                }
////                else
////                {
////                    loss=(loss*13+((historicalData.dataArrayList.get(i).close-historicalData.dataArrayList.get(i+1).close)/historicalData.dataArrayList.get(i+1).close)*100)/14;
////                    gain=(gain*13)/14;
////                }
////                rsi=100-100/(1+(gain/loss));
////
////            }
////            if(mainFrame.jComboBox2.getSelectedItem().toString().equals("GREATER THAN")){
////                if((rsi>Double.parseDouble(mainFrame.jTextField1.getText())))
////                {
////                    model.addRow(new Object[]{stockName,(int)(rsi),historicalData.dataArrayList.get(size).close});
////                }
////            } else if(rsi<Double.parseDouble(mainFrame.jTextField1.getText()))
////            {
////                model.addRow(new Object[]{stockName,(int)(rsi),historicalData.dataArrayList.get(size).close});
////            }
////
////        } catch (KiteException | IOException | JSONException ex) {
////            logger.error("Error occurred");
////            ex.printStackTrace();
////        }
////    }
////
////}