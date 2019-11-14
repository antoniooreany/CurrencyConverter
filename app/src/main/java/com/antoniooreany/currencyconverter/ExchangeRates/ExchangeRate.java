package com.antoniooreany.currencyconverter.ExchangeRates;

public class ExchangeRate {
    private String currencyName;
    private double rateForOneEuro;
    private String capital;

    public ExchangeRate(String currencyName, String capital, double rateForOneEuro) {
        this.currencyName = currencyName;
        this.rateForOneEuro = rateForOneEuro;
        this.capital = capital;
    }

    public String getCurrencyName() {
        return currencyName;
    }
    public String getCapital() { return capital; }
    public double getRateForOneEuro() {
        return rateForOneEuro;
    }
}
