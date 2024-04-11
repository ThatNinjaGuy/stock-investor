package com.thatninjaguyspeaks.stockinvestor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerodhatech.models.HistoricalData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FileProcessorUtil {
    private static final String CSV_DELIMITER = ", ";
    private FileProcessorUtil() {}

    private static final Logger logger = LogManager.getLogger(FileProcessorUtil.class);
    public static void persistData(Object deserializedDataToPersist, String filePath){
        ObjectMapper mapper = new ObjectMapper();
        try{
            OutputStream outputStream = new FileOutputStream("src/main/resources/" + filePath);
            mapper.writeValue(outputStream, deserializedDataToPersist);
        }catch (IOException e){
            logger.error("Error writing instrument to file");
            e.printStackTrace();
        }

    }

    public static void writeStockData(String instrument, HistoricalData data) {
        String fileName = "src/main/resources/history/" + instrument.replaceAll(" ", "_") + ".csv";
        Path filePath = Paths.get(fileName);
        boolean fileExists = Files.exists(filePath);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            if (!fileExists) {
                out.println("Timestamp, Date, Open, Close, Low, High, Volume");  // Write headers only if file is new
            }
            for (HistoricalData hd : data.dataArrayList) {
                String formattedTimestamp = formatTimestamp(hd.timeStamp);
                out.println(String.join(CSV_DELIMITER,
                        hd.timeStamp,
                        formattedTimestamp,
                        Double.toString(hd.open),
                        Double.toString(hd.close),
                        Double.toString(hd.low),
                        Double.toString(hd.high),
                        Double.toString(hd.volume)));
            }
        } catch (IOException e) {
            logger.error("Failed to write stock data for the stock {} to file: {}", instrument, fileName);
            e.printStackTrace();
        }
    }

    public static Map<String, HistoricalData> readAllStockData() {
        String directoryPath = "src/main/resources/history/";
        Map<String, HistoricalData> historicalDataMap = new HashMap<>();

        try(Stream<Path> files = Files.list(Paths.get(directoryPath))) {
            files.filter(path -> path.toString().endsWith(".csv"))
                    .forEach(path -> {
                        String instrument = path.getFileName().toString().replaceAll("_", " ").replace(".csv", "");
                        HistoricalData data = new HistoricalData();
                        try {
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines.subList(1, lines.size())) {  // Skip header
                                String[] values = line.split(CSV_DELIMITER);
                                if (values.length == 7) {
                                    HistoricalData dataPoint = new HistoricalData();
                                    dataPoint.timeStamp = values[0]; // Raw timestamp
                                    dataPoint.open = Double.parseDouble(values[2]);
                                    dataPoint.close = Double.parseDouble(values[3]);
                                    dataPoint.low = Double.parseDouble(values[4]);
                                    dataPoint.high = Double.parseDouble(values[5]);
                                    dataPoint.volume = Long.parseLong(values[6]);
                                    data.dataArrayList.add(dataPoint);
                                }
                            }
                        } catch (IOException e) {
                            logger.error("Failed to read stock data from file: {}", path);
                            e.printStackTrace();
                        }
                        historicalDataMap.put(instrument, data);
                    });
        } catch (IOException e) {
            logger.error("Failed to read stock data directory: {}", directoryPath);
            e.printStackTrace();
        }

        return historicalDataMap;
    }

    // Optional: If you need to extract the instrument name from the filename
    public static String extractInstrumentName(String fileName) {
        Path path = Paths.get(fileName);
        String file = path.getFileName().toString();
        return file.substring(0, file.lastIndexOf('.')).replaceAll("_", " ");
    }

    private static String formatTimestamp(String isoDateTime) {
        DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendOffset("+HHMM", "Z")  // Handle the timezone offset without a colon
                .toFormatter();

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("EEEE-dd-MMM-yyyy HH:mm:ss");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateTime, inputFormatter);
        return zonedDateTime.format(outputFormatter);
    }
}
