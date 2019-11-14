package com.antoniooreany.currencyconverter.Currency;

import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;

public class CurrencyElement {

    private String currencyName;
    private double rateForOneEuro;

    public CurrencyElement(String currencyName) {
        this.currencyName = currencyName;
        this.rateForOneEuro = new ExchangeRateDatabase().getExchangeRate(currencyName);
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public double getRateForOneEuro() {
        return rateForOneEuro;
    }
}
