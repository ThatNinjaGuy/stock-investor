# Stock Investor Application

Welcome to the Stock Investor Application, a sophisticated tool designed to facilitate advanced stock market analysis through the Kite API. This application enables users to retrieve historical stock data, calculate Relative Strength Index (RSI) and Bollinger Bands, and visually analyze these indicators through generated charts.

## Features

- **Kite API Integration**: Connects with the Kite API to fetch real-time and historical stock data.
- **Historical Data Importation**: Imports stocks and their historical data for comprehensive analysis.
- **RSI Calculation**: Calculates the RSI for stocks to identify overbought and oversold conditions.
- **Bollinger Bands Calculation**: Utilizes Bollinger Bands to provide insights into market volatility and price levels relative to moving averages.
- **Data Visualization**: Generates charts for both RSI and Bollinger Bands to visually interpret the stock market behavior.
- **Robust Error Handling**: Implements detailed logging and error management to ensure reliable operation.

## Installation

To set up the Stock Investor Application, follow these steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/thatninjaguyspeaks/stock-investor
   cd stock-investor

2. **Build the project** (assuming Maven is used):

3. **Set up API credentials**:
- Update the `API_KEY` and `API_SECRET` in the `KiteApiServiceImpl` class with your own credentials from Kite.

4. **Run the application**:
- Ensure Java 11 or higher is installed.
- Run the main class to start the application.

## Usage

### Importing Stock Data
To import stock data:
```
// Import stocks
kiteApiService.importStocks("your-request-id");

// Update historical data
kiteApiService.updateHistoricalData("your-request-id");

// Evaluate RSI strategy
Map<String, List<String>> rsiResults = kiteApiService.evaluateStrategy("stock-symbol", 14, 30, 70);

// Evaluate Bollinger Bands strategy
Map<String, List<String>> bbResults = kiteApiService.evaluateStrategy("stock-symbol", 20, 30, 70);
```

## Configuration
Configure your API details and other settings in KiteApiServiceImpl.java as per your requirements.

## Contributing
Contributions are welcome! Please fork the repository and submit pull requests with your suggested changes.

## License
This project is licensed under the MIT License - see the LICENSE file for details.