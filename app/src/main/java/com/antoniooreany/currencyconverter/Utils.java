package com.antoniooreany.currencyconverter;

import android.widget.Spinner;

import com.antoniooreany.currencyconverter.Activities.ExchangeRateDatabaseActivity;
import com.antoniooreany.currencyconverter.Currency.CurrencyElement;
import com.antoniooreany.currencyconverter.ExchangeRates.ExchangeRateDatabase;

import java.util.ArrayList;
import static java.lang.String.format;

public class Utils {
    private static final String FORMAT_STRING = "%%.%df";
    private static final int PLACES_AFTER_DECIMAL_POINT = 2;

    public static String getRoundNumber(double doubleResult) {
        return format(format(FORMAT_STRING, PLACES_AFTER_DECIMAL_POINT), doubleResult);
    }

    public static String getSelectedCurrency(Spinner spinner) {
        CurrencyElement selectedItemFrom = (CurrencyElement) (spinner.getSelectedItem());
        return selectedItemFrom.getCurrencyName();
    }

    public static ArrayList<CurrencyElement> getCurrencyElementArrayList(ExchangeRateDatabaseActivity activity) {
        ExchangeRateDatabase exchangeRateDatabase = new ExchangeRateDatabase();
        activity.setExchangeRateDatabase(exchangeRateDatabase);
        String[] currenciesList = exchangeRateDatabase.getCurrencies();

        ArrayList<CurrencyElement> currencyElementArrayList = new ArrayList<>();
        for (String currency : currenciesList) {
            CurrencyElement currencyElement = new CurrencyElement(currency);
            currencyElementArrayList.add(currencyElement);
        }
        //         TODO Try to simplify previous scope somehow like this:
//        ArrayList<CurrencyElement> data = new ArrayList<>(Arrays.asList(new CurrencyElement(currenciesList)));
        return currencyElementArrayList;
    }

//    private class SharedData {
//        private double doubleFrom;
//        private String currencyFrom;
//        private String currencyTo;
//        private String stringResult;
//
//        double getDoubleFrom() { return doubleFrom; }
//        String getCurrencyFrom() { return currencyFrom; }
//        String getCurrencyTo() { return currencyTo; }
//        String getStringResult() { return stringResult; }
//
//        SharedData invoke() {
//            try {
//                doubleFrom = Double.valueOf(editTextInput.getText().toString());
//            } catch (NumberFormatException e) {
//                doubleFrom = 0;
//            }
//            currencyFrom = Utils.getSelectedCurrency(spinnerFrom);
//            currencyTo = Utils.getSelectedCurrency(spinnerTo);
//            double doubleResult = exchangeRateDatabase.convert(doubleFrom, currencyFrom, currencyTo);
//            stringResult = Utils.getRoundNumber(doubleResult);
//            return this;
//        }
//    }
}