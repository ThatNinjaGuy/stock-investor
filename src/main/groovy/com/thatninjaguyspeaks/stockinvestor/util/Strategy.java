package com.thatninjaguyspeaks.stockinvestor.util;

import com.zerodhatech.models.Instrument;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Strategy implements Runnable {
    private Map<String, Instrument> companyList;
    private String strategyType;
    private String timeFrame;
    private static final Logger logger = Logger.getLogger(Strategy.class.getName());

    public Strategy(Map<String, Instrument> companyList) {
        this.companyList = companyList;
        this.strategyType = "RSI";
        this.timeFrame = "30minute";
    }

    @Override
    public void run() {
        LocalDate fromDay = LocalDate.now().minusDays(60);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fromString = fromDay + " 09:15:00";
        String toString = formatter.format(new Date());
        Date from = null;
        Date to = null;
        try {
            from = formatter.parse(fromString);
            to = formatter.parse(toString);
        } catch (ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        AtomicInteger i= new AtomicInteger(0);
        Date finalFrom = from;
        Date finalTo = to;
        final double totalCompanies = companyList.size();
        companyList.entrySet().stream().forEach((x) -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "Failed while halting", ex);
                Thread.currentThread().interrupt();
            }
            i.getAndIncrement();
            new Thread(new RsiStrategy(finalFrom, finalTo, x.getValue().instrument_token+"",
                    timeFrame, x.getValue().getName(), 30, "GREATER THAN", x.getValue())).start();
            double completionPercentage = (i.get()/totalCompanies) * 100;
            logger.info("Completion: " + completionPercentage + "%");
        });
    }
}


//package com.thatninjaguyspeaks.stockinvestor.util;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.util.Date;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.swing.table.DefaultTableModel;
//
//public class Strategy implements Runnable{
//    DefaultTableModel model;
//
//    @Override
//    public void run() {
//
//        model=(DefaultTableModel)mainFrame.jTable1.getModel();
//        int count=model.getRowCount();
//        for(int i=0;i<count;i++)
//        {
//            model.removeRow(0);
//        }
//        String instrumentToken[][]=mainFrame.instrumentToken;
//        int j=mainFrame.instrumentCount;
//        /** Get historical data dump, requires from and to date, intrument token, interval, continuous (for expired F&O contracts), oi (open interest)
//         * returns historical data object which will have list of historical data inside the object.*/
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String fromString ="";
//        String toString = "";
//
//        LocalDate fromDay = LocalDate.now().minusDays(60);
//        System.out.println(fromDay);
//        fromString = fromDay+" 09:15:00";
//        toString = formatter.format(new Date());
//        Date from = null;
//        Date to = null;
//        try {
//            from = formatter.parse(fromString);
//            to = formatter.parse(toString);
//        } catch (ParseException ex) {
//            Logger.getLogger(mainFrame.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        String timeFrame=mainFrame.jComboBox3.getSelectedItem().toString();
//        mainFrame.jProgressBar1.setMaximum(j);
//        for(int i=0;i<j;i++){
//            if(mainFrame.stopScan)
//            {
//                break;
//            }
//            if(mainFrame.jComboBox1.getSelectedItem().toString().equals("RSI")){
//                new Thread(new rsiStrtegy(from,to,instrumentToken[i][0],timeFrame,model,instrumentToken[i][1])).start();}
//            else
//            {
//                break;
//            }
//            if((i%30==0)&&(i>0))
//            {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(strategy.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            mainFrame.jProgressBar1.setValue(i);
//
//        }
//    }
//
//}