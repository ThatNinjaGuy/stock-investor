package com.thatninjaguyspeaks.stockinvestor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class JsonFileGenerator {

    private JsonFileGenerator() {}

    private static final Logger logger = LogManager.getLogger(JsonFileGenerator.class);
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
}
