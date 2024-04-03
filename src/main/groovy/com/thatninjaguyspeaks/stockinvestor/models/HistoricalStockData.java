package com.thatninjaguyspeaks.stockinvestor.models;

import com.zerodhatech.models.HistoricalData;
import com.zerodhatech.models.Instrument;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class HistoricalStockData {
    private Instrument instrument;
    private HistoricalData historicalData;
    private boolean conditionMet;

}
